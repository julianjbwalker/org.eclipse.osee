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
import { CommonModule } from '@angular/common';
import { Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { take } from 'rxjs';
import { createArtifact } from '@osee/shared/transactions';
import { ParameterTypesModule } from '../../parameter-types/parameter-types.module';
import { CreateCommandWithParameterArtifactService } from '../../services/create-command-form-services/create-command-with-parameter-artifact.service';
import { CreateCommandService } from '../../services/create-command-form-services/create-command.service';
import { ParameterDataService } from '../../services/data-services/selected-command-data/parameter-data/parameter-data.service';
import { OpenUrlFormComponent } from './command-actions/open-url-form/open-url-form.component';

@Component({
	selector: 'osee-create-command-form',
	standalone: true,
	imports: [
		CommonModule,
		FormsModule,
		MatFormFieldModule,
		MatInputModule,
		MatSelectModule,
		MatGridListModule,
		MatCardModule,
		MatButtonModule,
		MatIconModule,
		ParameterTypesModule,
		OpenUrlFormComponent,
	],
	templateUrl: './create-command-form.component.html',
	styleUrls: ['./create-command-form.component.sass'],
})
export class CreateCommandFormComponent implements OnDestroy {
	//Will eventually be the default value of the Create New Command Parameter
	commandActionOptions = this.parameterDataService.parameterDefaultValue$;

	commandAction: string = '';

	constructor(
		private createCommandService: CreateCommandService,
		private parameterDataService: ParameterDataService,
		private createCommandWithParameterArtifactService: CreateCommandWithParameterArtifactService
	) {}

	ngOnDestroy(): void {
		this.createCommandService.doneFx = '';
		this.createCommandWithParameterArtifactService.doneFx = '';
	}
	onSubmitHandler(e: {
		command: Partial<createArtifact>;
		parameter: Partial<createArtifact>;
	}) {
		this.createCommandWithParameterArtifactService
			.createCommandWithParameter(e.command, e.parameter)
			.pipe(take(1))
			.subscribe();
	}

	onSelectionChange(e: { selectedOption: string }) {
		this.commandAction = e.selectedOption;
	}
}
