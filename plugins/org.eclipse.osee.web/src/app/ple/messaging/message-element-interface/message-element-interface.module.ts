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

import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import {MatListModule} from '@angular/material/list';

import { MessageElementInterfaceRoutingModule } from './message-element-interface-routing.module';
import { MessageElementInterfaceComponent } from './message-element-interface.component';
import { PleSharedMaterialModule } from '../../ple-shared-material/ple-shared-material.module';
import { OseeStringUtilsPipesModule } from '../../../osee-utils/osee-string-utils/osee-string-utils-pipes/osee-string-utils-pipes.module';
import { SubElementTableComponent } from './components/sub-element-table/sub-element-table.component';
import { OseeStringUtilsDirectivesModule } from 'src/app/osee-utils/osee-string-utils/osee-string-utils-directives/osee-string-utils-directives.module';
import { MatDialogModule } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { SharedMessagingModule } from '../shared/shared-messaging.module';
import { EditStructureFieldComponent } from './components/edit-structure-field/edit-structure-field.component';
import { AddStructureDialogComponent } from './components/add-structure-dialog/add-structure-dialog.component';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { AddElementDialogComponent } from './components/add-element-dialog/add-element-dialog.component';
import { EditElementFieldComponent } from './components/sub-element-table/edit-element-field/edit-element-field.component';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatMenuModule } from '@angular/material/menu';
import {MatAutocompleteModule} from '@angular/material/autocomplete';


@NgModule({
  declarations: [MessageElementInterfaceComponent, SubElementTableComponent, EditStructureFieldComponent, AddStructureDialogComponent, AddElementDialogComponent, EditElementFieldComponent],
  imports: [
    CommonModule,
    MatTableModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatTooltipModule,
    MatStepperModule,
    MatDialogModule,
    MatMenuModule,
    MatListModule,
    MatSlideToggleModule,
    FormsModule,
    MatAutocompleteModule,
    PleSharedMaterialModule,
    OseeStringUtilsPipesModule,
    OseeStringUtilsDirectivesModule,
    MessageElementInterfaceRoutingModule,
    SharedMessagingModule
  ]
})
export class MessageElementInterfaceModule { }
