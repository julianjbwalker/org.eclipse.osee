/*
 * Created on Jun 28, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.access.internal;

import java.util.Collection;

import org.eclipse.osee.framework.access.IAccessProvider;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.AccessData;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public class ObjectAccessProvider implements IAccessProvider {

   private final AccessControlService accessService;

   public ObjectAccessProvider(AccessControlService accessService) {
      this.accessService = accessService;
   }

   @Override
   public void computeAccess(IBasicArtifact<?> userArtifact, Collection<?> objToCheck, AccessData accessData) throws OseeCoreException {
      PermissionEnum userPermission = null;
      PermissionEnum branchPermission = null;
      Branch branch = null;

      for (Object object : objToCheck) {


         if (object instanceof Artifact) {
            Artifact artifact = (Artifact) object;
            branch = artifact.getBranch();
            userPermission = accessService.getArtifactPermission(userArtifact, (Artifact) object);
         } else if (object instanceof Branch) {
            branch = (Branch) object;
         } else {
            throw new IllegalStateException("Unhandled object type for access control - " + object);
         }
         branchPermission = accessService.getBranchPermission(userArtifact, branch);

         if (branchPermission == PermissionEnum.DENY || userPermission == null) {
            userPermission = branchPermission;
         }
         accessData.add(object, userPermission);
      }
   }
}
