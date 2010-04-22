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
package org.eclipse.osee.ats.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsBranchManager;
import org.eclipse.osee.ats.world.WorldEditor;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.skynet.core.revision.ChangeManager;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.render.word.WordChangeReportOperation;
import org.eclipse.osee.framework.ui.swt.ImageManager;

/**
 * @author Donald G. Dunne
 */
public class ExportChangeReportsAction extends Action {
   //   private final WorldEditor worldEditor;
   private final boolean reverse = true;

   public ExportChangeReportsAction(WorldEditor worldEditor) {
      setText("Export Change Report(s)");
      setImageDescriptor(getImageDescriptor());
      //      this.worldEditor = worldEditor;
   }

   public Collection<TeamWorkFlowArtifact> getWorkflows() throws OseeCoreException {
      Collection<String> dontCreate =
            Arrays.asList(new String[] {"10083", "10255", "10267", "10703", "10773", "10783", "10903", "11041",
                  "11090", "11123", "11127", "11278", "11288", "11355", "11389", "11392", "11411", "11438", "11439",
                  "11440", "11460", "11465", "11571", "11574", "11649", "11733", "11819", "11822", "11859", "11925",
                  "11967", "12111", "8992", "9176", "9311", "9342", "9343", "9344", "9345", "9346", "9347", "9417",
                  "9418", "9422", "9430", "9445", "9463", "9484", "9492", "9534", "9656", "9927", "9997"});

      Collection<String> legacyIds =
            Arrays.asList(new String[] {"11123", "11127", "11132", "11152", "11157", "11158", "11159", "11162",
                  "11164", "11165", "11166", "11168", "11169", "11170", "11173", "11174", "11175", "11178", "11179",
                  "11180", "11181", "11182", "11185", "11189", "11190", "11192", "11204", "11208", "11209", "11211",
                  "11214", "11215", "11216", "11217", "11222", "11227", "11230", "11232", "11235", "11236", "11237",
                  "11238", "11240", "11242", "11243", "11247", "11252", "11254", "11258", "11259", "11263", "11264",
                  "11278", "11283", "11284", "11288", "11289", "11301", "11302", "11304", "11305", "11308", "11310",
                  "11312", "11318", "11319", "11326", "11355", "11362", "11363", "11369", "11372", "11376", "11383",
                  "11389", "11392", "11407", "11411", "11420", "11435", "11438", "11439", "11440", "11460", "11465",
                  "11537", "11539", "11541", "11551", "11552", "11564", "11568", "11570", "11571", "11574", "11583",
                  "11584", "11585", "11597", "11622", "11623", "11624", "11625", "11626", "11627", "11628", "11629",
                  "11630", "11631", "11632", "11640", "11641", "11642", "11643", "11644", "11649", "11651", "11654",
                  "11655", "11656", "11657", "11659", "11660", "11661", "11663", "11664", "11666", "11667", "11668",
                  "11669", "11670", "11671", "11672", "11673", "11674", "11675", "11676", "11677", "11679", "11687",
                  "11688", "11690", "11691", "11692", "11693", "11702", "11703", "11706", "11733", "11738", "11745",
                  "11771", "11772", "11774", "11780", "11819", "11820", "11821", "11822", "11824", "11847", "11856",
                  "11857", "11859", "11875", "11880", "11881", "11882", "11887", "11900", "11901", "11907", "11908",
                  "11909", "11911", "11912", "11914", "11918", "11922", "11925", "11926", "11930", "11932", "11933",
                  "11938", "11939", "11943", "11947", "11949", "11953", "11954", "11957", "11958", "11961", "11962",
                  "11965", "11967", "11968", "11970", "11974", "11976", "11979", "11981", "11984", "11985", "11988",
                  "11992", "11996", "11999", "12004", "12005", "12007", "12009", "12019", "12020", "12022", "12023",
                  "12032", "12051", "12052", "12059", "12060", "12061", "12065", "12068", "12073", "12077", "12084",
                  "12085", "12086", "12094", "12098", "12100", "12102", "12104", "12106", "12111", "12112", "12113",
                  "12117", "12119", "12134", "12141", "12142", "12143", "12155", "12162", "12164", "12170", "12181",
                  "12206", "12208", "12211", "12212", "12213"

            // TODO LATER
            //                  "5812", "6126", "6127", "6156", "6162", "6243", "6282",
            //                  "6283", "6284", "6285", "6286", "6287", "6288", "6289", "6290", "6291", "6292", "6293", "6294",
            //                  "6295", "6296", "6297", "6306", "6308", "6318", "6351", "6352", "6355", "6358", "6424", "6484",
            //                  "6574", "6579", "6583", "6599", "6601", "6603", "6665", "6666", "6668", "6701", "6703", "6719",
            //                  "6720", "6721", "6722", "6724", "6726", "6728", "6736", "6737", "6751", "6752", "6759", "6783",
            //                  "6786", "6787", "6788", "6799", "6803", "6810", "6812", "6813", "6816", "6818", "6826", "6830",
            //                  "6831", "6832", "6839", "6868", "6873", "6881", "6887", "6889", "6905", "6908", "6911", "6914",
            //                  "6922", "6933", "6937", "6945", "6969", "6970", "6972", "6994", "6998", "7011", "7025", "7032",
            //                  "7041", "7063", "7088", "7094", "7116", "7130", "7152", "7154", "7155", "7156", "7157", "7179",
            //                  "7219", "7220", "7223", "7224", "7225", "7227", "7228", "7229", "7230", "7231", "7232", "7233",
            //                  "7234", "7235", "7236", "7237", "7238", "7239", "7240", "7241", "7263", "7272", "7286", "7300",
            //                  "7318", "7345", "7350", "7367", "7368", "7371", "7376", "7407", "7444", "7481", "7484", "7485",
            //                  "7486", "7489", "7491", "7492", "7493", "7496", "7497", "7498", "7499", "7500", "7503", "7504",
            //                  "7505", "7514", "7518", "7520", "7533", "7539", "7562", "7566", "7567", "7569", "7570", "7571",
            //                  "7572", "7573", "7574", "7576", "7577", "7579", "7580", "7581", "7582", "7583", "7584", "7604",
            //                  "7605", "7606", "7607", "7608", "7609", "7610", "7625", "7626", "7630", "7638", "7639", "7665",
            //                  "7668", "7672", "7673", "7674", "7675", "7684", "7687", "7698", "7700", "7701", "7703", "7704",
            //                  "7705", "7708", "7710", "7713", "7714", "7716", "7718", "7720", "7721", "7722", "7727", "7729",
            //                  "7735", "7736", "7737", "7740", "7743", "7744", "7749", "7752", "7753", "7755", "7756", "7757",
            //                  "7759", "7772", "7773", "7784", "7785", "7805", "7806", "7811", "7812", "7829", "7842", "7847",
            //                  "7849", "7853", "7867", "7877", "7884", "7894", "7896", "7899", "7909", "7934", "7936", "7938",
            //                  "7940", "7941", "7945", "7949", "7952", "7956", "7963", "7964", "7966", "7976", "7983", "7985",
            //                  "7986", "7995", "8000", "8013", "8015", "8016", "8017", "8018", "8023", "8024", "8025", "8026",
            //                  "8027", "8029", "8032", "8033", "8036", "8038", "8049", "8062", "8063", "8064", "8076", "8103",
            //                  "8104", "8106", "8107", "8108", "8111", "8112", "8114", "8115", "8116", "8118", "8125", "8139",
            //                  "8148", "8151", "8152", "8155", "8158", "8159", "8163", "8164", "8165", "8166", "8167", "8168",
            //                  "8169", "8171", "8179", "8183", "8185", "8190", "8191", "8195", "8198", "8199", "8200", "8201",
            //                  "8204", "8210", "8211", "8250", "8251", "8252", "8253", "8255", "8280", "8290", "8293", "8302",
            //                  "8304", "8309", "8311", "8312", "8315", "8321", "8328", "8335", "8339", "8342", "8343", "8344",
            //                  "8345", "8346", "8349", "8350", "8351", "8356", "8357", "8358", "8360", "8363", "8365", "8366",
            //                  "8371", "8374", "8375", "8376", "8383", "8384", "8392", "8394", "8396", "8397", "8398", "8400",
            //                  "8401", "8402", "8405", "8406", "8409", "8410", "8411", "8412", "8413", "8414", "8415", "8416",
            //                  "8417", "8418", "8419", "8421", "8423", "8424", "8428", "8429", "8431", "8432", "8433", "8434",
            //                  "8435", "8436", "8437", "8444", "8445", "8448", "8449", "8454", "8457", "8458", "8459", "8460",
            //                  "8461", "8463", "8464", "8465", "8468", "8469", "8471", "8474", "8475", "8477", "8479", "8480",
            //                  "8481", "8482", "8483", "8484", "8485", "8486", "8488", "8489", "8490", "8491",
            });
      List<TeamWorkFlowArtifact> workflows = new ArrayList<TeamWorkFlowArtifact>();
      if (workflows.isEmpty()) {
         List<Artifact> artifacts =
               ArtifactQuery.getArtifactListFromAttributeValues(AtsAttributeTypes.LegacyPCRId, legacyIds,
                     CoreBranches.COMMON, legacyIds.size());
         for (Artifact artifact : artifacts) {
            if (artifact.getArtifactType().getGuid().equals("AAMFDjZ1UVAQTXHk2GgA")) {
               TeamWorkFlowArtifact teamWorkflow = (TeamWorkFlowArtifact) artifact;
               String legacyId = teamWorkflow.getWorldViewLegacyPCR();
               if (!dontCreate.contains(legacyId)) {
                  workflows.add(teamWorkflow);
               }
            }
         }
         Collections.sort(workflows);
         if (reverse) {
            Collections.reverse(workflows);
         }
      }
      return workflows;
      //return worldEditor.getWorldComposite().getXViewer().getSelectedTeamWorkflowArtifacts();
   }

   @Override
   public void run() {
      try {
         IOperation operation = new ExportChangesOperation(getWorkflows());
         Operations.executeAsJob(operation, true);
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex.toString(), ex);
      }
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.EXPORT_DATA);
   }

   public void updateEnablement() throws OseeCoreException {
      setEnabled(!getWorkflows().isEmpty());
   }

   private static final class ExportChangesOperation extends AbstractOperation {
      private final Collection<TeamWorkFlowArtifact> workflows;

      public ExportChangesOperation(Collection<TeamWorkFlowArtifact> workflows) {
         super("Exporting Change Report(s)", AtsPlugin.PLUGIN_ID);
         this.workflows = workflows;
      }

      private TransactionRecord pickTransaction(IArtifact workflow) throws OseeCoreException {
         int minTransactionId = -1;
         for (TransactionRecord transaction : TransactionManager.getCommittedArtifactTransactionIds(workflow)) {
            if (minTransactionId < transaction.getId()) {
               minTransactionId = transaction.getId();
            }
         }
         if (minTransactionId == -1) {
            throw new OseeStateException("no transaction records found for " + workflow);
         }
         return TransactionManager.getTransactionId(minTransactionId);
      }

      @Override
      protected void doWork(IProgressMonitor monitor) throws Exception {
         for (Artifact workflow : workflows) {
            AtsBranchManager atsBranchMgr = ((TeamWorkFlowArtifact) workflow).getBranchMgr();

            Collection<Change> changes = new ArrayList<Change>();
            IOperation operation = null;
            if (atsBranchMgr.isCommittedBranchExists()) {
               operation = ChangeManager.comparedToPreviousTx(pickTransaction(workflow), changes);
            } else {
               Branch workingBranch = atsBranchMgr.getWorkingBranch();
               if (workingBranch != null) {
                  operation = ChangeManager.comparedToParent(workingBranch, changes);
               }
            }
            if (operation != null) {
               doSubWork(operation, monitor, 0.50);
            }
            if (!changes.isEmpty()) {
               String folderName = workflow.getSoleAttributeValueAsString(AtsAttributeTypes.LegacyPCRId, null);
               IOperation subOp = new WordChangeReportOperation(changes, true, folderName);
               doSubWork(subOp, monitor, 0.50);
            } else {
               monitor.worked(calculateWork(0.50));
            }
         }

      }
   }
}
