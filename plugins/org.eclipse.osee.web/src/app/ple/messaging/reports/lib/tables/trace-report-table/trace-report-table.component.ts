/*********************************************************************
 * Copyright (c) 2023 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
import {
	AfterViewInit,
	ChangeDetectionStrategy,
	Component,
	Input,
	ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import type { NodeTraceReportItem } from '@osee/messaging/shared/types';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { HighlightFilteredTextDirective } from '@osee/shared/utils';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { HeaderService } from '@osee/shared/services';
import { nodeTraceReportHeaderDetails } from '../../table-headers/trace-report-table-headers';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';

@Component({
	selector: 'osee-trace-report-table',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [
		CommonModule,
		FormsModule,
		HighlightFilteredTextDirective,
		MatFormFieldModule,
		MatIconModule,
		MatInputModule,
		MatSortModule,
		MatTableModule,
		MatPaginatorModule,
	],
	templateUrl: './trace-report-table.component.html',
})
export class TraceReportTableComponent implements AfterViewInit {
	@Input() data: NodeTraceReportItem[] = [];

	@ViewChild(MatSort) sort!: MatSort;
	@ViewChild(MatPaginator) paginator!: MatPaginator;

	dataSource: MatTableDataSource<NodeTraceReportItem>;

	pageSize = 200;

	constructor(private headerService: HeaderService) {
		this.dataSource = new MatTableDataSource(this.data);
	}

	filterPredicate(data: NodeTraceReportItem, filter: string) {
		const filterLower = filter.toLowerCase();
		if (
			data.name.toLowerCase().includes(filterLower) ||
			data.artifactType.toLowerCase().includes(filterLower)
		) {
			return true;
		}
		for (const rel of data.relatedItems) {
			if (
				rel.name.toLowerCase().includes(filterLower) ||
				rel.artifactType.toLowerCase().includes(filterLower)
			) {
				return true;
			}
		}
		return false;
	}

	applyFilter(event: Event) {
		const filterValue = (event.target as HTMLInputElement).value;
		this.dataSource.filterPredicate = this.filterPredicate;
		this.dataSource.filter = filterValue;
	}

	ngAfterViewInit() {
		this.dataSource = new MatTableDataSource(this.data);
		this.dataSource.sort = this.sort;
		this.dataSource.paginator = this.paginator;
	}

	getTableHeaderByName(header: keyof NodeTraceReportItem) {
		return this.headerService.getHeaderByName(
			nodeTraceReportHeaderDetails,
			header
		);
	}
	trackRows(index: number, item: NodeTraceReportItem) {
		return item.id;
	}

	headers: (keyof NodeTraceReportItem)[] = [
		'name',
		'artifactType',
		'relatedItems',
	];
}
