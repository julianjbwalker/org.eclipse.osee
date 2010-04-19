/*
 * Created on Apr 9, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.model;

import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.ArtifactDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.skynet.core.access.AccessControlManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.GeneralData;
import org.eclipse.osee.framework.skynet.core.artifact.KeyValueArtifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.util.Result;

/**
 * @author Donald G. Dunne
 */
public class CoveragePreferences {

   private static String ARTIFACT_NAME = "Coverage Preferences";
   private final Branch branch;
   private Artifact artifact;

   public CoveragePreferences(Branch branch) {
      this.branch = branch;
   }

   private Artifact getArtifact() throws OseeCoreException {
      if (artifact == null) {
         try {
            artifact = ArtifactQuery.getArtifactFromTypeAndName(CoreArtifactTypes.GeneralData, ARTIFACT_NAME, branch);
         } catch (ArtifactDoesNotExist ex) {
            // do nothing
         }
      }
      if (artifact == null) {
         artifact =
               ArtifactTypeManager.addArtifact(CoreArtifactTypes.GeneralData, BranchManager.getCommonBranch(),
                     ARTIFACT_NAME);
         artifact.persist("Coverage Preferences - creation");
      }
      return artifact;
   }

   public Result isSaveable() throws OseeCoreException {
      if (!AccessControlManager.hasPermission(getArtifact(), PermissionEnum.WRITE)) {
         return new Result(String.format("You do not have permissions to change Coverage Preferences"));
      }
      return Result.TrueResult;
   }

   /**
    * Return global CoverageOptions or null if none available
    */
   public String getCoverageOptions() throws OseeCoreException {
      if (getArtifact() == null) return null;
      KeyValueArtifact keyValueArt =
            new KeyValueArtifact(getArtifact(), GeneralData.GENERAL_STRING_ATTRIBUTE_TYPE_NAME);
      return keyValueArt.getValue("CoverageOptions");
   }

   public void setCoverageOptions(String options) throws OseeCoreException {
      KeyValueArtifact keyValueArt =
            new KeyValueArtifact(getArtifact(), GeneralData.GENERAL_STRING_ATTRIBUTE_TYPE_NAME);
      keyValueArt.setValue("CoverageOptions", options);
      keyValueArt.save();
      getArtifact().persist("Coverage Preferences - save");
   }
}
