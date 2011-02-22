/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.dsl.integration.internal;

import java.util.Collection;
import java.util.HashSet;
import org.eclipse.osee.framework.core.data.AccessContextId;
import org.eclipse.osee.framework.core.dsl.integration.AccessModelInterpreter;
import org.eclipse.osee.framework.core.dsl.integration.ArtifactDataProvider;
import org.eclipse.osee.framework.core.dsl.integration.ArtifactDataProvider.ArtifactProxy;
import org.eclipse.osee.framework.core.dsl.integration.RestrictionHandler;
import org.eclipse.osee.framework.core.dsl.oseeDsl.AccessContext;
import org.eclipse.osee.framework.core.dsl.oseeDsl.HierarchyRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.ObjectRestriction;
import org.eclipse.osee.framework.core.dsl.oseeDsl.XArtifactMatcher;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.access.AccessDetailCollector;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Strings;

/**
 * @author Roberto E. Escobar
 */
public class AccessModelInterpreterImpl implements AccessModelInterpreter {

   private final ArtifactDataProvider provider;
   private final ArtifactMatchInterpreter matcher;
   private final Collection<RestrictionHandler<?>> restrictionHandlers;

   public AccessModelInterpreterImpl(ArtifactDataProvider provider, ArtifactMatchInterpreter matcher, RestrictionHandler<?>... restricitionHandlers) {
      this.provider = provider;
      this.matcher = matcher;
      this.restrictionHandlers = new HashSet<RestrictionHandler<?>>();
      for (RestrictionHandler<?> handler : restricitionHandlers) {
         restrictionHandlers.add(handler);
      }
   }

   @Override
   public AccessContext getContext(Collection<AccessContext> contexts, AccessContextId contextId) throws OseeCoreException {
      Conditions.checkNotNull(contexts, "accessContext collection");
      Conditions.checkNotNull(contextId, "accessContextId");
      AccessContext toReturn = null;
      for (AccessContext accessContext : contexts) {
         if (contextId.getGuid().equals(Strings.unquote(accessContext.getGuid()))) {
            toReturn = accessContext;
         }
      }
      return toReturn;
   }

   @Override
   public void computeAccessDetails(AccessDetailCollector collector, AccessContext context, Object objectToCheck) throws OseeCoreException {
      Conditions.checkNotNull(collector, "accessDetailCollector");
      Conditions.checkNotNull(context, "accessContext");
      Conditions.checkNotNull(objectToCheck, "objectToCheck");

      if (provider.isApplicable(objectToCheck)) {
         ArtifactProxy data = provider.asCastedObject(objectToCheck);
         Conditions.checkNotNull(data, "artifactData",
            "artifact data provider returned null - provider has an isApplicable error");

         collectApplicable(collector, context, data);
      }
   }

   private void collectApplicable(AccessDetailCollector collector, AccessContext context, ArtifactProxy artifactData) throws OseeCoreException {
      processContext(collector, context, artifactData);
      for (AccessContext superContext : context.getSuperAccessContexts()) {
         collectApplicable(collector, superContext, artifactData);
      }
   }

   private void processContext(AccessDetailCollector collector, AccessContext context, ArtifactProxy artifactData) throws OseeCoreException {
      collectRestrictions(collector, artifactData, context.getAccessRules());
      Collection<HierarchyRestriction> restrictions = context.getHierarchyRestrictions();

      Collection<ArtifactProxy> proxyHierarchy = artifactData.getHierarchy();

      for (HierarchyRestriction hierarchy : restrictions) {
         XArtifactMatcher artifactRef = hierarchy.getArtifactMatcherRef();
         if (matcher.matches(artifactRef, proxyHierarchy)) {
            collectRestrictions(collector, artifactData, hierarchy.getAccessRules());
         }
      }
   }

   private void collectRestrictions(AccessDetailCollector collector, ArtifactProxy artifactData, Collection<ObjectRestriction> restrictions) throws OseeCoreException {
      for (ObjectRestriction objectRestriction : restrictions) {
         for (RestrictionHandler<?> restrictionHandler : restrictionHandlers) {
            restrictionHandler.process(objectRestriction, artifactData, collector);
         }
      }
   }

}
