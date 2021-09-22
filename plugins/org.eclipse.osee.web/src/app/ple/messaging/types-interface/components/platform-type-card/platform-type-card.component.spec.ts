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
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { HarnessLoader } from '@angular/cdk/testing';
import { MatCardHarness } from '@angular/material/card/testing';
import { MatButtonHarness } from '@angular/material/button/testing';
import {TestbedHarnessEnvironment} from '@angular/cdk/testing/testbed';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { PlatformTypeCardComponent } from './platform-type-card.component';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { FormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { EditTypeDialogComponent } from '../edit-type-dialog/edit-type-dialog.component';
import { editPlatformTypeDialogDataMode } from '../../types/EditPlatformTypeDialogDataMode.enum';
import { CurrentTypesService } from '../../services/current-types.service';
import { of } from 'rxjs';
import { enumerationSet } from '../../types/enum';
import { editPlatformTypeDialogData } from '../../types/editPlatformTypeDialogData';
import { currentTypesServiceMock } from '../../mocks/services/current.types.service.mock';

let loader: HarnessLoader;

describe('PlatformTypeCardComponent', () => {
  let component: PlatformTypeCardComponent;
  let fixture: ComponentFixture<PlatformTypeCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports:[MatCardModule, MatDialogModule,MatButtonModule, MatFormFieldModule,MatSlideToggleModule,FormsModule,MatInputModule, NoopAnimationsModule],
      declarations: [PlatformTypeCardComponent, EditTypeDialogComponent],
      providers: [{
        provide: CurrentTypesService, useValue: currentTypesServiceMock}
        // {
        //   provide: MatDialog, useValue: {
        //     open() {
        //     },
        //     afterClosed(): Observable<object> {
        //       return of({
        //         data: {
        //           mode: Math.random()<0.5? 'copy':'edit' 
        //         }
        //       })
        //     }
        //   }
        // },
      ],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PlatformTypeCardComponent);
    loader = TestbedHarnessEnvironment.loader(fixture);
    component = fixture.componentInstance;
    const expectedData={
      id: "0",
      name: "Random enumeration",
      interfaceLogicalType: "enumeration",
      interfacePlatformTypeMinval: "0",
      interfacePlatformTypeMaxval: "1",
      interfacePlatformTypeBitSize: "8",
      interfacePlatformTypeDefaultValue: "0",
      interfacePlatformTypeMsbValue: "0",
      interfacePlatformTypeBitsResolution: "0",
      interfacePlatformTypeCompRate: "0",
      interfacePlatformTypeAnalogAccuracy: "0",
      interfacePlatform2sComplement: false,
      interfacePlatformTypeEnumLiteral: "A string",
      interfacePlatformTypeUnits: "N/A",
      interfacePlatformTypeValidRangeDescription:"N/A"
    }
    component.typeData = expectedData;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create a header of class mat-card-header-text with text of Random Boolean',async () => {
    fixture.detectChanges();
    const card = await loader.getHarness(MatCardHarness);
    expect(await card.getTitleText()).toEqual("Random enumeration");
  });
  it('should create a subtitle with text of Boolean',async () => {
    fixture.detectChanges();
    const card = await loader.getHarness(MatCardHarness);
    expect(await card.getSubtitleText()).toEqual("enumeration");
  });

  it('should contain text that has minimum value, maximum value, byte size, default value, msb value, resolution, comp rate, analog accuracy, edit and Create New Type From Base',async () => {
    fixture.detectChanges();
    const card = await loader.getHarness(MatCardHarness);
    expect(await card.getText()).toEqual("Random enumeration  enumeration  Minimum Value: 0  Maximum Value: 1  Bit Size: 8  Default Value: 0  MSB Value: 0  Resolution: 0  Comp Rate: 0  Analog Accuracy: 0  Edit  Create New Type From Base Edit Related Enumeration Set Attributes");
  });

  it('should open dialog and create an edit of an existing type', async() => {
    const openDialog = spyOn(component, 'openDialog').and.callThrough();
    let dialogRefSpy = jasmine.createSpyObj({ afterClosed: of<editPlatformTypeDialogData>({mode:editPlatformTypeDialogDataMode.edit,type:{name:'',interfaceLogicalType:'',interfacePlatform2sComplement:false,interfacePlatformTypeAnalogAccuracy:'',interfacePlatformTypeBitSize:'0',interfacePlatformTypeBitsResolution:'',interfacePlatformTypeCompRate:'',interfacePlatformTypeDefaultValue:'0',interfacePlatformTypeEnumLiteral:'',interfacePlatformTypeMaxval:'',interfacePlatformTypeMinval:'',interfacePlatformTypeMsbValue:'',interfacePlatformTypeUnits:'',interfacePlatformTypeValidRangeDescription:''}}), close: null });
    let dialogSpy = spyOn(TestBed.inject(MatDialog), 'open').and.returnValue(dialogRefSpy)
    const button = await (await loader.getHarness(MatCardHarness)).getHarness(MatButtonHarness.with({ text: "Edit" }));
    await button.click();
    expect(openDialog).toHaveBeenCalledWith(editPlatformTypeDialogDataMode.edit);
  })

  it('should open dialog and create a copy of an existing type', async() => {
    const openDialog = spyOn(component, 'openDialog').and.callThrough();
    let dialogRefSpy = jasmine.createSpyObj({ afterClosed: of<editPlatformTypeDialogData>({mode:editPlatformTypeDialogDataMode.copy,type:{name:'',interfaceLogicalType:'',interfacePlatform2sComplement:false,interfacePlatformTypeAnalogAccuracy:'',interfacePlatformTypeBitSize:'0',interfacePlatformTypeBitsResolution:'',interfacePlatformTypeCompRate:'',interfacePlatformTypeDefaultValue:'0',interfacePlatformTypeEnumLiteral:'',interfacePlatformTypeMaxval:'',interfacePlatformTypeMinval:'',interfacePlatformTypeMsbValue:'',interfacePlatformTypeUnits:'',interfacePlatformTypeValidRangeDescription:''}}), close: null });
    let dialogSpy = spyOn(TestBed.inject(MatDialog), 'open').and.returnValue(dialogRefSpy)
    const button = await (await loader.getHarness(MatCardHarness)).getHarness(MatButtonHarness.with({ text: "Create New Type From Base" }));
    await button.click();
    expect(openDialog).toHaveBeenCalledWith(editPlatformTypeDialogDataMode.copy);
  })

  it('should call openEnumDialog()',async () => {
    const openEnumDialog = spyOn(component, 'openEnumDialog').and.callThrough();
    let dialogRefSpy = jasmine.createSpyObj({ afterClosed: of<enumerationSet>({name:'',description:'',applicability:{id:'1',name:'Base'}}), close: null });
    let dialogSpy = spyOn(TestBed.inject(MatDialog),'open').and.returnValue(dialogRefSpy)
    const button = await (await loader.getHarness(MatCardHarness)).getHarness(MatButtonHarness.with({ text: "Edit Related Enumeration Set Attributes" }));
    await button.click();
    expect(openEnumDialog).toHaveBeenCalled();
  })
  //Don't know how to do this test yet
  // it('should open a dialog subscription',async () => {
  //   fixture.detectChanges();
  //   component.openDialog('edit');
  //   component.dialog.
  // });

});
