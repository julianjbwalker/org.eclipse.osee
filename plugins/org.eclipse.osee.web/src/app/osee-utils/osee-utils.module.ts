/*********************************************************************
 * Copyright (c) 2021 Boeing
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
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OseeStringUtilsModule } from './osee-string-utils/osee-string-utils.module';



@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    OseeStringUtilsModule
  ],
  exports:[OseeStringUtilsModule]
})
export class OseeUtilsModule { }
