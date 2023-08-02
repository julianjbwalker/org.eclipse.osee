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
import { Component, Inject, Input } from '@angular/core';
import {
	take,
	switchMap,
	iif,
	of,
	filter,
	combineLatest,
	map,
	OperatorFunction,
	tap,
} from 'rxjs';
import { applic } from '@osee/shared/types/applicability';
import { difference } from '@osee/shared/types/change-report';
import { AddElementDialogComponent } from '../../dialogs/add-element-dialog/add-element-dialog.component';
import { DefaultAddElementDialog } from '../../dialogs/add-element-dialog/add-element-dialog.default';
import { RemoveElementDialogData } from '../../dialogs/remove-element-dialog/remove-element-dialog';
import { RemoveElementDialogComponent } from '../../dialogs/remove-element-dialog/remove-element-dialog.component';
import { EditElementDialogComponent } from '../../dialogs/edit-element-dialog/edit-element-dialog.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Router, RouterLink } from '@angular/router';
import { UiService } from '@osee/shared/services';
import { AsyncPipe, NgIf } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { STRUCTURE_SERVICE_TOKEN } from '@osee/messaging/shared/tokens';
import type {
	element,
	structure,
	enumerationSet,
	PlatformType,
	EditViewFreeTextDialog,
	elementWithChanges,
	ElementDialog,
} from '@osee/messaging/shared/types';
import { EditEnumSetDialogComponent } from '@osee/messaging/shared/dialogs';
import {
	CurrentStructureService,
	EnumerationUIService,
	HeaderService,
	WarningDialogService,
} from '@osee/messaging/shared/services';
import { EditViewFreeTextFieldDialogComponent } from '@osee/messaging/shared/dialogs/free-text';
import { PlatformTypeSentinel } from '@osee/messaging/shared/enumerations';
import {
	createArtifact,
	modifyArtifact,
	modifyRelation,
	relation,
} from '@osee/shared/types';
import { MatDividerModule } from '@angular/material/divider';

/**
 * Required attributes:
 * element
 * structure
 * header
 * branchId
 * branchType
 * editMode
 */
@Component({
	selector:
		'osee-sub-element-table-dropdown[element][structure][header][branchId][branchType][editMode]',
	standalone: true,
	imports: [
		NgIf,
		AsyncPipe,
		RouterLink,
		MatMenuModule,
		MatIconModule,
		MatDialogModule,
		MatFormFieldModule,
		MatDividerModule,
	],
	templateUrl: './sub-element-table-dropdown.component.html',
	styleUrls: ['./sub-element-table-dropdown.component.sass'],
})
export class SubElementTableDropdownComponent {
	@Input() element: element = {
		id: '-1',
		name: '',
		description: '',
		notes: '',
		interfaceElementIndexEnd: 0,
		interfaceElementIndexStart: 0,
		interfaceElementAlterable: false,
		interfaceDefaultValue: '',
		platformType: new PlatformTypeSentinel(),
		units: '',
		enumLiteral: '',
		autogenerated: true,
	};

	@Input() structure: structure = {
		id: '-1',
		name: '',
		nameAbbrev: '',
		description: '',
		interfaceMaxSimultaneity: '',
		interfaceMinSimultaneity: '',
		interfaceTaskFileType: 0,
		interfaceStructureCategory: '',
	};

	@Input() header!: keyof element;
	@Input() field?: string | number | boolean | PlatformType | applic;

	@Input('branchId') _branchId: string = '';
	@Input('branchType') _branchType: string = '';

	@Input() editMode: boolean = false;

	constructor(
		private _ui: UiService,
		private router: Router,
		public dialog: MatDialog,
		@Inject(STRUCTURE_SERVICE_TOKEN)
		private structureService: CurrentStructureService,
		private headerService: HeaderService,
		private enumSetService: EnumerationUIService,
		private warningDialogService: WarningDialogService
	) {}
	removeElement(element: element, structure: structure) {
		const dialogData: RemoveElementDialogData = {
			elementId: element.id,
			structureId: structure.id,
			elementName: element.name,
		};
		this.dialog
			.open(RemoveElementDialogComponent, {
				data: dialogData,
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((dialogResult: string) =>
					iif(
						() => dialogResult === 'ok',
						this.structureService.removeElementFromStructure(
							element,
							structure
						),
						of()
					)
				)
			)
			.subscribe();
	}
	deleteElement(element: element) {
		//open dialog, yes/no if yes -> this.structures.deleteElement()
		const dialogData: RemoveElementDialogData = {
			elementId: element.id,
			structureId: '',
			elementName: element.name,
		};
		this.dialog
			.open(RemoveElementDialogComponent, {
				data: dialogData,
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((dialogResult: string) =>
					iif(
						() => dialogResult === 'ok',
						this.structureService.deleteElement(element),
						of()
					)
				)
			)
			.subscribe();
	}
	openAddElementDialog(
		structure: structure,
		afterElement?: string,
		element?: element
	) {
		const dialogData = new DefaultAddElementDialog(
			structure?.id || '',
			structure?.name || '',
			JSON.parse(JSON.stringify(element)) //make a copy
		);
		let dialogRef = this.dialog.open(AddElementDialogComponent, {
			data: dialogData,
		});
		let createElement = dialogRef.afterClosed().pipe(
			take(1),
			filter(
				(val) =>
					(val !== undefined || val !== null) &&
					val?.element !== undefined
			),
			switchMap((value: ElementDialog) =>
				iif(
					() =>
						value.element.id !== undefined &&
						value.element.id !== '-1' &&
						value.element.id.length > 0,
					this.structureService
						.relateElement(
							structure.id,
							value.element.id !== undefined
								? value.element.id
								: '-1',
							afterElement || 'end'
						)
						.pipe(
							switchMap((transaction) =>
								combineLatest([
									this._ui.isLoading,
									of(transaction),
								]).pipe(
									filter(
										([loading, transaction]) =>
											loading !== 'false'
									),
									take(1),
									map(([loading, transaction]) => {
										this.router.navigate([], {
											fragment: 'a' + value.element.id,
										});
									})
								)
							)
						),
					this.structureService
						.createNewElement(
							value.element,
							structure.id,
							value.type.id as string,
							afterElement || 'end'
						)
						.pipe(
							switchMap((transaction) =>
								combineLatest([
									this._ui.isLoading,
									of(transaction),
								]).pipe(
									filter(
										([loading, transaction]) =>
											loading !== 'false'
									),
									take(1),
									map(([loading, transaction]) => {
										this.router.navigate([], {
											fragment:
												'a' +
												(transaction.results.ids[0] ||
													afterElement ||
													''),
										});
									})
								)
							)
						)
				)
			)
		);
		createElement.subscribe();
	}
	openEditElementDialog(element: element) {
		const dialogData: ElementDialog = {
			id: '',
			name: '',
			element: element,
			type: element.platformType,
		};
		let dialogRef = this.dialog.open(EditElementDialogComponent, {
			data: dialogData,
		});
		dialogRef
			.afterClosed()
			.pipe(
				take(1),
				filter(
					(val) =>
						(val !== undefined || val !== null) &&
						val?.element !== undefined &&
						val.type !== undefined
				),
				switchMap((val) =>
					this.structureService.changeElementFromDialog(val)
				),
				tap((v) => console.log(v))
			)
			.subscribe();
	}
	openEnumDialog(id: string) {
		/**
		 * If create artifacts does not contain the enum set key(should be last or 2nd last object in modifiedArtifacts),
		 * Display a warning for the following:
		 * Each modified enum
		 * The modified enum set
		 * The modified platform type(s)
		 */
		this.dialog
			.open(EditEnumSetDialogComponent, {
				data: {
					id: id,
					isOnEditablePage: this.editMode,
				},
			})
			.afterClosed()
			.pipe(
				filter((x) => x !== undefined) as OperatorFunction<
					| {
							createArtifacts: createArtifact[];
							modifyArtifacts: modifyArtifact[];
							deleteRelations: modifyRelation[];
					  }
					| undefined,
					{
						createArtifacts: createArtifact[];
						modifyArtifacts: modifyArtifact[];
						deleteRelations: modifyRelation[];
					}
				>,
				take(1),
				switchMap((tx) =>
					iif(
						() => this.editMode,
						this.warningDialogService
							.openEnumsDialogs(
								tx.modifyArtifacts
									.slice(0, -1)
									.map((v) => v.id),
								[
									...tx.createArtifacts
										.flatMap((v) => v.relations)
										.filter(
											(v): v is relation =>
												v !== undefined
										)
										.map((v) => v.sideA)
										.filter(
											(v): v is string | string[] =>
												v !== undefined
										)
										.flatMap((v) => v),
									...tx.deleteRelations
										.flatMap((v) => v.aArtId)
										.filter(
											(v): v is string => v !== undefined
										),
								]
							)
							.pipe(
								switchMap((_) =>
									this.enumSetService.changeEnumSet(tx)
								)
							),
						of()
					)
				)
			)
			.subscribe();
	}

	openDescriptionDialog(
		description: string,
		elementId: string,
		structureId: string
	) {
		this.dialog
			.open(EditViewFreeTextFieldDialogComponent, {
				data: {
					original: JSON.parse(JSON.stringify(description)) as string,
					type: 'Description',
					return: description,
				},
				minHeight: '60%',
				minWidth: '60%',
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((response: EditViewFreeTextDialog | string) =>
					iif(
						() =>
							response === 'ok' ||
							response === 'cancel' ||
							response === undefined,
						//do nothing
						of(),
						//change description
						this.structureService.partialUpdateElement(
							{
								id: elementId,
								description: (
									response as EditViewFreeTextDialog
								).return,
							},
							this.structure.id
						)
					)
				)
			)
			.subscribe();
	}

	/**
   * 
   Need to verify if type is required
   */
	openEnumLiteralDialog(enumLiteral: string, elementId: string) {
		this.dialog
			.open(EditViewFreeTextFieldDialogComponent, {
				data: {
					original: JSON.parse(JSON.stringify(enumLiteral)) as string,
					type: 'Enum Literal',
					return: enumLiteral,
				},
				minHeight: '60%',
				minWidth: '60%',
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((response: EditViewFreeTextDialog | string) =>
					iif(
						() =>
							response === 'ok' ||
							response === 'cancel' ||
							response === undefined,
						//do nothing
						of(),
						//change description
						this.structureService.partialUpdateElement(
							{
								id: elementId,
								enumLiteral: (
									response as EditViewFreeTextDialog
								).return,
							},
							this.structure.id
						)
					)
				)
			)
			.subscribe();
	}

	openNotesDialog(notes: string, elementId: string, structureId: string) {
		this.dialog
			.open(EditViewFreeTextFieldDialogComponent, {
				data: {
					original: JSON.parse(JSON.stringify(notes)) as string,
					type: 'Notes',
					return: notes,
				},
				minHeight: '60%',
				minWidth: '60%',
			})
			.afterClosed()
			.pipe(
				take(1),
				switchMap((response: EditViewFreeTextDialog | string) =>
					iif(
						() =>
							response === 'ok' ||
							response === 'cancel' ||
							response === undefined,
						//do nothing
						of(),
						//change notes
						this.structureService.partialUpdateElement(
							{
								id: elementId,
								notes: (response as EditViewFreeTextDialog)
									.return,
							},
							this.structure.id
						)
					)
				)
			)
			.subscribe();
	}

	getHeaderByName(value: string) {
		return this.headerService.getHeaderByName(value, 'element');
	}

	viewDiff<T>(value: difference<T> | undefined, header: string) {
		if (value !== undefined) {
			this.structureService.sideNav = {
				opened: true,
				field: header,
				currentValue: value.currentValue as string | number | applic,
				previousValue: value.previousValue as
					| string
					| number
					| applic
					| undefined,
				transaction: value.transactionToken,
			};
		}
	}
	hasChanges(v: element | elementWithChanges): v is elementWithChanges {
		return (
			(v as any).changes !== undefined ||
			(v as any).added !== undefined ||
			(v as any).deleted !== undefined
		);
	}
}
