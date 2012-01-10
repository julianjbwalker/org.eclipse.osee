/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.coverage.event.CoverageChange;
import org.eclipse.osee.coverage.event.CoverageEventType;
import org.eclipse.osee.coverage.event.CoveragePackageEvent;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.coverage.model.CoverageItem;
import org.eclipse.osee.coverage.model.CoverageOptionManager;
import org.eclipse.osee.coverage.model.CoverageOptionManagerDefault;
import org.eclipse.osee.coverage.model.CoverageUnit;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.model.ITestUnitProvider;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IBasicUser;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.type.Pair;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.utility.UsersByIds;

/**
 * @author Donald G. Dunne
 */
public class OseeCoverageUnitStore extends OseeCoverageStore {

   private final CoverageUnit coverageUnit;
   private static ITestUnitProvider testUnitProvider;

   public OseeCoverageUnitStore(ICoverage parent, Artifact artifact, CoverageOptionManager coverageOptionManager) throws OseeCoreException {
      super(null, artifact.getArtifactType(), artifact.getBranch());
      this.artifact = artifact;
      this.coverageUnit =
         new CoverageUnit(artifact.getGuid(), parent, artifact.getName(), "",
            OseeCoverageUnitFileContentsProvider.getInstance(branch));
      load(coverageOptionManager);
   }

   public OseeCoverageUnitStore(CoverageUnit coverageUnit, IOseeBranch branch) {
      super(coverageUnit,
         coverageUnit.isFolder() ? CoverageArtifactTypes.CoverageFolder : CoverageArtifactTypes.CoverageUnit, branch);
      this.coverageUnit = coverageUnit;
   }

   public static CoverageUnit get(ICoverage parent, Artifact artifact, CoverageOptionManager coverageOptionManager) throws OseeCoreException {
      OseeCoverageUnitStore unitStore = new OseeCoverageUnitStore(parent, artifact, coverageOptionManager);
      return unitStore.getCoverageUnit();
   }

   public static OseeCoverageUnitStore get(CoverageUnit coverageUnit, IOseeBranch branch) {
      return new OseeCoverageUnitStore(coverageUnit, branch);
   }

   @Override
   public void delete(SkynetTransaction transaction, CoveragePackageEvent coverageEvent, boolean purge) throws OseeCoreException {
      if (getArtifact(false) != null) {
         if (purge) {
            getArtifact(false).purgeFromBranch();
         } else {
            getArtifact(false).deleteAndPersist(transaction);
         }
      }
      coverageEvent.getCoverages().add(new CoverageChange(coverageUnit, CoverageEventType.Deleted));
      for (CoverageUnit childCoverageUnit : coverageUnit.getCoverageUnits()) {
         new OseeCoverageUnitStore(childCoverageUnit, branch).delete(transaction, coverageEvent, purge);
      }
   }

   public void load(CoverageOptionManager coverageOptionManager) throws OseeCoreException {
      coverageUnit.clearCoverageUnits();
      coverageUnit.clearCoverageItems();
      Artifact artifact = getArtifact(false);
      if (artifact != null) {
         for (String value : artifact.getAttributesToStringList(CoverageAttributeTypes.Item)) {
            CoverageItem item =
               CoverageItem.createCoverageItem(coverageUnit, value, coverageOptionManager, getTestUnitProvider());
            coverageUnit.addCoverageItem(item);
         }
         // Don't load file contents until needed
         coverageUnit.setFileContentsProvider(OseeCoverageUnitFileContentsProvider.getInstance(branch));
         coverageUnit.setNotes(artifact.getSoleAttributeValueAsString(CoverageAttributeTypes.Notes, ""));
         coverageUnit.setFolder(artifact.isOfType(CoverageArtifactTypes.CoverageFolder));
         coverageUnit.setAssignees(artifact.getSoleAttributeValueAsString(CoverageAttributeTypes.Assignees, ""));
         coverageUnit.setWorkProductTaskGuid(artifact.getSoleAttributeValueAsString(
            CoverageAttributeTypes.WorkProductTaskGuid, ""));
         coverageUnit.setNamespace(artifact.getSoleAttributeValueAsString(CoverageAttributeTypes.Namespace, ""));
         coverageUnit.setOrderNumber(artifact.getSoleAttributeValueAsString(CoverageAttributeTypes.Order, ""));
         coverageUnit.setLocation(artifact.getSoleAttributeValueAsString(CoverageAttributeTypes.Location, ""));
         for (Artifact childArt : artifact.getChildren()) {
            if (childArt.isOfType(CoverageArtifactTypes.CoverageUnit, CoverageArtifactTypes.CoverageFolder)) {
               coverageUnit.addCoverageUnit(OseeCoverageUnitStore.get(coverageUnit, childArt, coverageOptionManager));
            }
         }
      }
   }

   private ITestUnitProvider getTestUnitProvider() {
      if (testUnitProvider == null) {
         testUnitProvider = new TestUnitCache(new ArtifactTestUnitStore());
      }
      return testUnitProvider;
   }

   public void reloadItem(CoverageEventType eventType, CoverageItem currentCoverageItem, CoverageChange change, CoverageOptionManager coverageOptionManager) throws OseeCoreException {
      Artifact artifact = getArtifact(false);

      if (artifact == null) {
         return;
      }
      if (eventType == CoverageEventType.Modified) {
         for (String value : artifact.getAttributesToStringList(CoverageAttributeTypes.Item)) {
            CoverageItem dbChangedItem =
               CoverageItem.createCoverageItem(coverageUnit, value, coverageOptionManager, getTestUnitProvider());
            if (currentCoverageItem.getGuid().equals(dbChangedItem.getGuid())) {
               currentCoverageItem.copy(currentCoverageItem, dbChangedItem);
            }
         }
      } else if (eventType == CoverageEventType.Deleted) {
         coverageUnit.removeCoverageItem(currentCoverageItem);
      } else if (eventType == CoverageEventType.Added) {
         // do nothing; full coverage unit needs reload
      }
   }

   @Override
   public Result save(SkynetTransaction transaction, CoveragePackageEvent coverageEvent) throws OseeCoreException {
      Artifact artifact = getArtifact(true);
      artifact.setName(coverageUnit.getName());

      List<String> items = new ArrayList<String>();
      for (CoverageItem coverageItem : coverageUnit.getCoverageItems()) {
         // Get test names from coverageItem
         Collection<String> testUnitNames = coverageItem.getTestUnits();
         // Set provider to db provider
         coverageItem.setTestUnitProvider(getTestUnitProvider());
         // store off testUnitNames; this will add to db and replace names with db nameId
         coverageItem.setTestUnits(testUnitNames);
         items.add(coverageItem.toXml());
      }
      artifact.setAttributeValues(CoverageAttributeTypes.Item, items);
      // Determine which items have changed and log for event
      for (Attribute<Object> attr : artifact.getAttributes(CoverageAttributeTypes.Item)) {
         if (attr.isDirty()) {
            try {
               Pair<String, String> nameGuid = CoverageItem.getNameGuidFromStore((String) attr.getValue());
               CoverageChange change =
                  new CoverageChange(nameGuid.getFirst(), nameGuid.getSecond(), CoverageEventType.Modified);
               if (attr.getModificationType() == ModificationType.NEW || attr.getModificationType() == ModificationType.UNDELETED || attr.getModificationType() == ModificationType.INTRODUCED) {
                  change.setEventType(CoverageEventType.Added);
               } else if (attr.getModificationType() == ModificationType.DELETED) {
                  change.setEventType(CoverageEventType.Deleted);
               }
               coverageEvent.getCoverages().add(change);
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      }
      if (Strings.isValid(coverageUnit.getNotes())) {
         artifact.setSoleAttributeFromString(CoverageAttributeTypes.Notes, coverageUnit.getNotes());
      } else {
         artifact.deleteAttributes(CoverageAttributeTypes.Notes);
      }
      if (Strings.isValid(coverageUnit.getNamespace())) {
         artifact.setSoleAttributeFromString(CoverageAttributeTypes.Namespace, coverageUnit.getNamespace());
      } else {
         artifact.deleteAttributes(CoverageAttributeTypes.Namespace);
      }
      if (Strings.isValid(coverageUnit.getWorkProductTaskGuid())) {
         artifact.setSoleAttributeFromString(CoverageAttributeTypes.WorkProductTaskGuid,
            coverageUnit.getWorkProductTaskGuid());
      } else {
         artifact.deleteAttributes(CoverageAttributeTypes.WorkProductTaskGuid);
      }
      if (coverageUnit.getFileContentsProvider() != null && coverageUnit.getFileContentsProvider() != OseeCoverageUnitFileContentsProvider.getInstance(branch)) {
         String fileContents = coverageUnit.getFileContents();
         if (Strings.isValid(fileContents)) {
            coverageUnit.setFileContentsProvider(OseeCoverageUnitFileContentsProvider.getInstance(branch));
            coverageUnit.setFileContents(fileContents);
         }
      }
      if (Strings.isValid(coverageUnit.getOrderNumber())) {
         artifact.setSoleAttributeFromString(CoverageAttributeTypes.Order, coverageUnit.getOrderNumber());
      } else {
         artifact.deleteAttributes(CoverageAttributeTypes.Order);
      }
      if (Strings.isValid(coverageUnit.getAssignees())) {
         artifact.setSoleAttributeFromString(CoverageAttributeTypes.Assignees, coverageUnit.getAssignees());
      } else {
         artifact.deleteAttributes(CoverageAttributeTypes.Assignees);
      }
      if (Strings.isValid(coverageUnit.getLocation())) {
         artifact.setSoleAttributeFromString(CoverageAttributeTypes.Location, coverageUnit.getLocation());
      } else {
         artifact.deleteAttributes(CoverageAttributeTypes.Location);
      }
      if (coverageUnit.getParent() != null) {
         Artifact parentArt = ArtifactQuery.getArtifactFromId(coverageUnit.getParent().getGuid(), branch);
         if (artifact.getParent() == null && !parentArt.getChildren().contains(artifact)) {
            parentArt.addChild(artifact);
         }
      }
      // Save current/new coverage items
      for (CoverageUnit childCoverageUnit : coverageUnit.getCoverageUnits()) {
         new OseeCoverageUnitStore(childCoverageUnit, branch).save(transaction, coverageEvent);
      }
      // Delete removed coverage units and folders
      for (Artifact childArt : artifact.getChildren()) {
         if (childArt.isOfType(CoverageArtifactTypes.CoverageUnit, CoverageArtifactTypes.CoverageFolder)) {
            boolean found = false;
            for (CoverageUnit childCoverageUnit : coverageUnit.getCoverageUnits()) {
               if (childCoverageUnit.getGuid().equals(childArt.getGuid())) {
                  found = true;
                  break;
               }
            }
            if (!found) {
               new OseeCoverageUnitStore(coverageUnit, childArt, CoverageOptionManagerDefault.instance()).delete(
                  transaction, coverageEvent, false);
            }
         }
      }

      artifact.persist(transaction);
      if (artifact.isDirty()) {
         CoverageChange change = new CoverageChange(coverageUnit, CoverageEventType.Modified);
         if (artifact.getModType() == ModificationType.NEW) {
            change.setEventType(CoverageEventType.Modified);
         }
         coverageEvent.getCoverages().add(change);
      }
      getTestUnitProvider().save(transaction);
      return Result.TrueResult;
   }

   public CoverageUnit getCoverageUnit() {
      return coverageUnit;
   }

   public static void setAssignees(CoverageUnit coverageUnit, IBasicUser user) throws OseeCoreException {
      setAssignees(coverageUnit, Collections.singleton(user));
   }

   public static void setAssignees(CoverageUnit coverageUnit, Collection<IBasicUser> users) throws OseeCoreException {
      coverageUnit.setAssignees(getAssigneesToString(users));
   }

   private static String getAssigneesToString(Collection<IBasicUser> users) throws OseeCoreException {
      return UsersByIds.getStorageString(users);
   }

   public static Collection<IBasicUser> getAssignees(CoverageUnit coverageUnit) {
      return getAssigneesFromString(coverageUnit.getAssignees());
   }

   private static Collection<IBasicUser> getAssigneesFromString(String string) {
      if (!Strings.isValid(string)) {
         return Collections.emptyList();
      }
      return UsersByIds.getUsers(string);
   }

   @Override
   public CoveragePackageEvent getBaseCoveragePackageEvent(CoverageEventType coverageEventType) {
      throw new IllegalArgumentException("Should never be called");
   }
}
