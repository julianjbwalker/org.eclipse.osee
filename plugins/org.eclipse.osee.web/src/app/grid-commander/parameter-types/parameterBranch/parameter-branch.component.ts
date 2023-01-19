/*********************************************************************
 * Copyright (c) 2022 Boeing
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
import { Component } from '@angular/core';
import { map } from 'rxjs/operators';
import { CommandGroupOptionsService } from '../../services/data-services/command-group-options.service';

@Component({
	selector: 'osee-parameter-branch',
	templateUrl: './parameter-branch.component.html',
	styleUrls: ['./parameter-branch.component.sass'],
})
export class ParameterBranchComponent {
	parameter$ = this.commandGroupOptService.commandsParameter;
	paramString = '';
	userPrompt$ = this.parameter$.pipe(
		map((param) => param?.attributes.description)
	);

	constructor(private commandGroupOptService: CommandGroupOptionsService) {}
}
