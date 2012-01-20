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

package org.eclipse.osee.framework.ui.skynet.Import;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.CompositeOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.NullOperationLogger;
import org.eclipse.osee.framework.core.operation.OperationLogger;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifact;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifactKind;
import org.eclipse.osee.framework.skynet.core.importing.operations.CompleteArtifactImportOperation;
import org.eclipse.osee.framework.skynet.core.importing.operations.FilterArtifactTypesByAttributeTypes;
import org.eclipse.osee.framework.skynet.core.importing.operations.RoughArtifactCollector;
import org.eclipse.osee.framework.skynet.core.importing.operations.RoughToRealArtifactOperation;
import org.eclipse.osee.framework.skynet.core.importing.operations.SourceToRoughArtifactOperation;
import org.eclipse.osee.framework.skynet.core.importing.parsers.IArtifactExtractor;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.IArtifactImportResolver;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.ui.skynet.ArtifactValidationCheckOperation;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;

/**
 * @author Robert A. Fisher
 * @author Ryan D. Brooks
 */
public final class ArtifactImportOperationFactory {

   private ArtifactImportOperationFactory() {
      super();
   }

   /**
    * <p>
    * Create a CompositeOperation, full import sequence.<br/>
    * <ol>
    * <li>SourceToRoughArtifactOperation</li>
    * <li>RoughToRealArtifactOperation</li>
    * <li>ArtifactValidationCheckOperation</li>
    * <li>CompleteArtifactImportOperation</li>
    * </ol>
    * </p>
    * <br/>
    *
    * @param param
    * @return
    * @throws OseeCoreException
    */
   public static IOperation completeOperation(ArtifactImportOperationParameter param) throws OseeCoreException {
      return completeOperation(param.getSourceFile(), param.getDestinationArtifact(), param.getLogger(),
         param.getExtractor(), param.getResolver(), param.isStopOnError(), param.getGoverningTransaction(),
         param.isExecuteTransaction());
   }

   public static IOperation completeOperation(File sourceFile, Artifact destinationArtifact, OperationLogger logger, IArtifactExtractor extractor, IArtifactImportResolver resolver, boolean stopOnError, SkynetTransaction governingTransaction, boolean executeTransaction) throws OseeCoreException {
      CheckAndThrow(sourceFile, destinationArtifact, extractor, resolver);

      RoughArtifactCollector collector = new RoughArtifactCollector(new RoughArtifact(RoughArtifactKind.PRIMARY));

      if (logger == null) {
         logger = NullOperationLogger.getSingleton();
      }

      SkynetTransaction transaction = governingTransaction;
      if (transaction == null) {
         executeTransaction = true;
         transaction =
            TransactionManager.createTransaction(destinationArtifact.getBranch(),
               "ArtifactImportOperationFactory: Artifact Import Wizard transaction");
      }

      List<IOperation> ops = new ArrayList<IOperation>();
      ops.add(new SourceToRoughArtifactOperation(logger, extractor, sourceFile, collector));
      ops.add(new RoughToRealArtifactOperation(transaction, destinationArtifact, collector, resolver, false));
      ops.add(new ArtifactValidationCheckOperation(destinationArtifact.getDescendants(), stopOnError));
      if (executeTransaction) {
         ops.add(new CompleteArtifactImportOperation(transaction, destinationArtifact));
      }
      return new CompositeOperation("Artifact Import", Activator.PLUGIN_ID, ops);
   }

   private static void CheckAndThrow(Object... objects) {
      for (Object object : objects) {
         Assert.isNotNull(object);
      }
   }

   /**
    * Creates a full import process.
    * <ol>
    * <li>SourceToRoughArtifactOperation</li>
    * <li>FilterArtifactTypesByAttributeTypes</li> if runFilterByAttributes == true
    * <li>RoughToRealArtifactOperation</li>
    * <li>FetchAndAddDescendantsOperation</li>
    * <li>ArtifactValidationCheckOperation</li>
    * <li>CompleteArtifactImportOperation</li>
    * </ol>
    */
   public static IOperation createOperation(File sourceFile, Artifact destinationArtifact, OperationLogger logger, IArtifactExtractor extractor, IArtifactImportResolver resolver, RoughArtifactCollector collector, Collection<IArtifactType> selectionArtifactTypes, boolean stopOnError, boolean deleteUnMatched, boolean runFilterByAttributes) throws OseeCoreException {
      List<IOperation> ops = new ArrayList<IOperation>();
      ops.add(createArtifactsCompOperation(
         "Artifact Import - SourceToRoughArtifact, FilterArtifactTypesByAttributeTypes", sourceFile,
         destinationArtifact, logger, extractor, collector, selectionArtifactTypes, runFilterByAttributes));
      ops.add(createRoughToRealOperation(
         "Artifact Import - RoughToRealArtifactOperation, ArtifactValidationCheckOperation, CompleteArtifactImportOperation",
         destinationArtifact, resolver, stopOnError, collector, deleteUnMatched));
      return new CompositeOperation("Artifact Import - ArtifactAndRoughToRealOperation, RoughToRealOperation",
         Activator.PLUGIN_ID, ops);
   }

   /**
    * @see ArtifactImportPage
    */
   public static IOperation createArtifactsCompOperation(String opDescription, File sourceFile, Artifact destinationArtifact, OperationLogger logger, IArtifactExtractor extractor, RoughArtifactCollector collector, Collection<IArtifactType> selectionArtifactTypes, boolean runFilterByAttributes) {
      List<IOperation> ops = new ArrayList<IOperation>();
      ops.add(new SourceToRoughArtifactOperation(logger, extractor, sourceFile, collector));
      if (runFilterByAttributes) {
         ops.add(new FilterArtifactTypesByAttributeTypes(destinationArtifact.getBranch(), collector,
            selectionArtifactTypes));
      }
      return new CompositeOperation(opDescription, Activator.PLUGIN_ID, ops);
   }

   /**
    * @throws OseeCoreException
    * @see ArtifactImportWizard
    */
   public static IOperation createRoughToRealOperation(String opName, final Artifact destinationArtifact, IArtifactImportResolver resolver, boolean stopOnError, RoughArtifactCollector collector, boolean deleteUnmatchedArtifacts) throws OseeCoreException {
      SkynetTransaction transaction =
         TransactionManager.createTransaction(destinationArtifact.getBranch(),
            "Artifact Import Wizard transaction " + opName);

      List<IOperation> ops = new ArrayList<IOperation>();
      ops.add(new RoughToRealArtifactOperation(transaction, destinationArtifact, collector, resolver,
         deleteUnmatchedArtifacts));

      final List<Artifact> children = new ArrayList<Artifact>();
      ops.add(new FetchAndAddDescendantsOperation(children, destinationArtifact));
      ops.add(new ArtifactValidationCheckOperation(children, stopOnError));
      ops.add(new CompleteArtifactImportOperation(transaction, destinationArtifact));

      return new CompositeOperation(opName, Activator.PLUGIN_ID, ops);
   }

   private static class FetchAndAddDescendantsOperation extends AbstractOperation {

      private final List<Artifact> children;
      private final Artifact destination;

      /**
       * @param children list to add result of <code>destination.getDescendants()</code> to
       * @param destination
       */
      public FetchAndAddDescendantsOperation(List<Artifact> children, Artifact destination) {
         super("Fetch and Add Descendants", Activator.PLUGIN_ID);
         this.children = children;
         this.destination = destination;
      }

      @Override
      protected void doWork(IProgressMonitor monitor) throws Exception {
         try {
            children.addAll(this.destination.getDescendants());
         } catch (OseeCoreException ex) {
            throw new OseeCoreException(String.format("Unable to get artifact children: artifact:[%s] branch:[%s]",
               this.destination.getGuid(), this.destination.getBranch().getGuid()), ex);
         }
      }
   }

}
