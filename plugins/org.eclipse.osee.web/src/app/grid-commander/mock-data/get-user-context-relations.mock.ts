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
import { Observable, of } from 'rxjs';
import { gridCommanderUser } from '../types/grid-commander-types/gc-user-and-contexts-relationships';
import { GetUserContextRelations } from '../services/fetch-data-services/get-user-context-relations.service';

export const GetUserContextRelationsMock: Partial<GetUserContextRelations> = {
	getResponseUserContextData: function (): Observable<gridCommanderUser> {
		return of();
	},
};
