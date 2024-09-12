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
import { Component, input } from '@angular/core';
import { workType } from '@osee/shared/types/configuration-management';

@Component({
	selector: 'osee-branch-picker',
	template: '<div>Dummy</div>',
	standalone: true,
})
// eslint-disable-next-line @angular-eslint/component-class-suffix
export class BranchPickerStub {
	category = input<`${number}`>('-1');
	excludeCategory = input<`${number}`>('-1');
	workType = input<workType>('None');
}
