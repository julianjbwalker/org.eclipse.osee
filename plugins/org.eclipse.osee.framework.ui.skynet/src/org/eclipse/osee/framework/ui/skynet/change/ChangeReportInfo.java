/*
 * Created on Apr 29, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.change;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.data.TransactionDelta;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.change.Change;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.KeyedImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class ChangeReportInfo implements EditorSection.IWidget {

   private static final String LOADING = "<form><p vspace='false'><b>Loading ...</b></p></form>";
   private static final String NO_CHANGES_FOUND = "<b>No changes were found</b><br/>";

   private final ChangeUiData changeData;

   private FormText formText;
   private Label label;
   private ScrolledForm form;

   public ChangeReportInfo(ChangeUiData changeData) {
      this.changeData = changeData;
   }

   @Override
   public void onCreate(IManagedForm managedForm, Composite parent) {
      FormToolkit toolkit = managedForm.getToolkit();
      form = managedForm.getForm();
      form.getBody().setLayout(new GridLayout());
      form.getBody().setBackground(parent.getBackground());

      Composite composite = toolkit.createComposite(parent, SWT.NONE);
      composite.setLayout(ALayout.getZeroMarginLayout(2, false));
      composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

      label = toolkit.createLabel(composite, "", SWT.NONE);
      label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

      formText = toolkit.createFormText(composite, true);
      GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
      gd.widthHint = 200;
      formText.setLayoutData(gd);
      formText.layout();

      updateInfo(false);
      toolkit.paintBordersFor(form.getBody());
   }

   @Override
   public void onUpdate() {
      updateInfo(true);
   }

   private void updateImageLabel() {
      KeyedImage imageKey = FrameworkImage.DELTAS;
      if (!changeData.isRebaseline()) {
         if (changeData.isBranchValid()) {
            imageKey = FrameworkImage.DELTAS_DIFFERENT_BRANCHES;
         } else if (changeData.isTransactionValid()) {
            imageKey = FrameworkImage.DELTAS_TXS_SAME_BRANCH;
         }
      }
      label.setImage(ImageManager.getImage(imageKey));
   }

   public void updateInfo(boolean changeReportWasLoaded) {
      updateImageLabel();
      StringBuilder sb = new StringBuilder();
      createInfoPage(sb, changeReportWasLoaded);
      try {
         formText.setText(sb.toString(), true, true);
      } catch (Exception ex) {
         formText.setText(Lib.exceptionToString(ex), false, false);
      }
      // FormText doesn't size correctly, so determine it's height
      GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
      gridData.heightHint = 8 * (2 + sb.toString().split("<br/>").length);
      formText.setLayoutData(gridData);
   }

   public void createInfoPage(StringBuilder sb, boolean changeReportWasLoaded) {
      sb.append("<form>");
      sb.append("<p>");
      boolean isRebaselined = changeData.isRebaseline();
      if (!changeData.isLoaded()) {
         sb.append(String.format("<b>%s</b><br/>", "Cleared on shut down. Press refresh to reload"));
         sb.append("<br/>");
         addSimpleBranchInfo(sb);
         sb.append("<br/>");
         addExtraInfo(sb);
      } else if (changeReportWasLoaded && !isRebaselined) {
         if (changeData.getChanges().isEmpty()) {
            addSimpleBranchInfo(sb);
            sb.append(NO_CHANGES_FOUND);
         } else {
            addChangesInfo(sb);
         }
         sb.append("<br/><br/>");
         addExtraInfo(sb);
      } else if (isRebaselined) {
         addSimpleBranchInfo(sb);
         sb.append(String.format("<br/><b>%s</b><br/>",
               "The branch has been updated from parent and cannot be refreshed."));
         sb.append(String.format("<b>%s</b>", "Please close down and re-open this change report"));
      }
      sb.append("</p>");
      sb.append("</form>");
   }

   private void addSimpleBranchInfo(StringBuilder sb) {
      if (changeData.isBranchValid()) {
         sb.append(String.format("<b>%s</b><br/>", changeData.getBranch()));
      } else if (changeData.isTransactionValid()) {
         TransactionRecord transaction = changeData.getTransaction();
         try {
            sb.append(String.format("<b>%s</b><br/>", transaction.getBranch()));
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
      }
   }

   private String getAssociated() {
      String message = "";
      Artifact associatedArtifact = changeData.getAssociatedArtifact();
      if (associatedArtifact != null) {
         message = associatedArtifact.getName();
      } else {
         message = "Unkown";
      }
      return String.format("<b>Associated With: </b> %s<br/>", message);
   }

   private void addChangesInfo(StringBuilder sb) {
      Change change = changeData.getChanges().iterator().next();
      TransactionDelta delta = change.getTxDelta();
      try {
         TransactionRecord tx1 = delta.getStartTx();
         TransactionRecord tx2 = delta.getEndTx();

         NumberFormat formatter = NumberFormat.getInstance();

         if (delta.areOnTheSameBranch()) {
            sb.append(String.format("<b>Branch: </b> %s", tx1.getBranch()));
            sb.append("<br/>");
            sb.append(String.format("<b>StartTx: </b> %s <b>EndTx:</b> %s", formatter.format(tx1.getId()),
                  formatter.format(tx2.getId())));
         } else {
            sb.append(String.format("<b>Branch %s: </b> %s", 1, tx1.getBranch()));
            sb.append("<br/>");
            sb.append(String.format("<b>Tx: </b> %s", formatter.format(tx1.getId())));
            sb.append("<br/>");
            sb.append("<b>Compared To</b>");
            sb.append("<br/>");
            sb.append(String.format("<b>Branch %s: </b> %s", 2, tx2.getBranch()));
            sb.append("<br/>");
            sb.append(String.format("<b>Tx: </b> %s", formatter.format(tx2.getId())));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
      }
   }

   private void addExtraInfo(StringBuilder sb) {
      sb.append(getAssociated());

      TransactionRecord transaction = changeData.getTransaction();
      boolean isNotCommitted =
            changeData.isBranchValid() || (changeData.isTransactionValid() && transaction.getComment() == null);

      if (isNotCommitted) {

      } else {
         DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
         sb.append(String.format("<b>Committed On: </b> %s<br/>", dateFormat.format(transaction.getTimeStamp())));
         String author = "Unknown";
         try {
            User user = UserManager.getUserByArtId(transaction.getAuthor());
            author = user.toString();
         } catch (OseeCoreException ex) {
            OseeLog.log(SkynetGuiPlugin.class, Level.SEVERE, ex);
         }
         sb.append(String.format("<b>Committed By: </b> %s<br/>", author));
         sb.append(String.format("<b>Comment: </b> %s", transaction.getComment()));
      }
   }

   @Override
   public void onLoading() {
      formText.setText(LOADING, true, false);
   }

}
