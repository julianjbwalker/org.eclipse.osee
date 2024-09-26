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
import { Routes } from '@angular/router';

export const routes: Routes = [
	{
		path: '',
		title: 'Zenith',
		loadComponent: () => import('./all-scripts.component'),
	},
	{
		path: ':branchType',
		title: 'Zenith',
		loadComponent: () => import('./all-scripts.component'),
	},
	{
		path: ':branchType/:branchId',
		title: 'Zenith',
		loadComponent: () => import('./all-scripts.component'),
	},
	{
		path: ':branchType/:branchId/:ciSet',
		title: 'Zenith',
		loadComponent: () => import('./all-scripts.component'),
	},
];

export default routes;
