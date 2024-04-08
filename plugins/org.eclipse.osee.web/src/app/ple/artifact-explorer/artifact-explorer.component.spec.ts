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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ArtifactExplorerComponent } from './artifact-explorer.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ArtifactHeirarchyMockComponent } from './lib/components/hierarchy/artifact-hierarchy-panel/artifact-heirarchy-panel.component.mock';
import { ArtifactHierarchyPanelComponent } from './lib/components/hierarchy/artifact-hierarchy-panel/artifact-hierarchy-panel.component';
import { ArtifactExplorerPreferencesHttpService } from './lib/services/artifact-explorer-preferences-http.service';
import { artifactExplorerPreferencesHttpServiceMock } from './lib/testing/artifact-explorer-preferences-http.service.mock';
import { UserDataAccountService } from '@osee/auth';
import { userDataAccountServiceMock } from '@osee/auth/testing';

describe('ArtifactExplorerComponent', () => {
	let component: ArtifactExplorerComponent;
	let fixture: ComponentFixture<ArtifactExplorerComponent>;

	beforeEach(() => {
		TestBed.overrideComponent(ArtifactExplorerComponent, {
			add: {
				imports: [ArtifactHeirarchyMockComponent],
			},
			remove: {
				imports: [ArtifactHierarchyPanelComponent],
			},
		}).configureTestingModule({
			imports: [ArtifactExplorerComponent, NoopAnimationsModule],
			providers: [
				{
					provide: ArtifactExplorerPreferencesHttpService,
					useValue: artifactExplorerPreferencesHttpServiceMock,
				},
				{
					provide: UserDataAccountService,
					useValue: userDataAccountServiceMock,
				},
			],
		});
		fixture = TestBed.createComponent(ArtifactExplorerComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
