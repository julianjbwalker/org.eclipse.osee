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
import { OseeStringUtilsPipesModule } from './osee-string-utils-pipes/osee-string-utils-pipes.module';
import { OseeStringUtilsDirectivesModule } from './osee-string-utils-directives/osee-string-utils-directives.module';



@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    OseeStringUtilsPipesModule,
    OseeStringUtilsDirectivesModule
  ],
  exports: [OseeStringUtilsModule, OseeStringUtilsDirectivesModule]
})
export class OseeStringUtilsModule { }
