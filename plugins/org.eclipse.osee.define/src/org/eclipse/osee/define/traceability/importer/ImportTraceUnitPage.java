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
package org.eclipse.osee.define.traceability.importer;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osee.define.internal.Activator;
import org.eclipse.osee.define.traceability.TraceUnitExtensionManager;
import org.eclipse.osee.define.traceability.TraceUnitExtensionManager.TraceHandler;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.MutableBoolean;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.plugin.util.DirectoryOrFileSelector;
import org.eclipse.osee.framework.ui.skynet.branch.BranchSelectComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.WizardDataTransferPage;

/**
 * @author Roberto E. Escobar
 */
public class ImportTraceUnitPage extends WizardDataTransferPage {
   private static final String PAGE_NAME = "org.eclipse.osee.define.traceability.importer.importTraceUnitPage";

   private static final String PAGE_TITLE = "Import Trace Units into OSEE";
   private static final String PAGE_DESCRIPTION = "Imports trace units into OSEE and updates relations.";

   private static final String SOURCE_GROUP = "Select trace unit(s) source path";
   private static final String BRANCH_SELECT_GROUP = "Select branch to import into";
   private static final String IMPORT_BRANCH_TOOLTIP = "Only working branches are allowed";

   private static final String TRACE_UNIT_HANDLER_GROUP = "Select trace unit parser";
   private static final String TRACE_UNIT_ERROR = "Please select a trace unit handler";

   private static final String OPTIONS_GROUP = "Processing Options";
   private static final String PERSIST_CHANGES = "Persist Changes";
   private static final String PERSIST_TOOLTIP =
      "When not selected, will report all trace marks found per trace unit file.";

   private static final String RECURSION_BUTTON = "Traverse sub-folders";
   private static final String RECURSION_TOOLTIP =
      "When selected, processing will include folders and their sub-folders.";

   private static final String FILE_WITH_PATHS_BUTTON = "Is File With Embedded Paths";
   private static final String FILE_WITH_PATHS_TOOLTIP =
      "Select when using a source file with multiple paths separated with newlines.";

   private static final String SELECTED_TRACE_HANDLERS_KEY = "trace.handlers";
   private static final String BRANCH_KEY = "branch.selected";
   private static final String SOURCE_URI_KEY = "source.uri";
   private static final String SOURCE_URI_IS_DIRECTORY_KEY = "source.uri.is.directory";
   private static final String IS_ART_PERSIST_ALLOWED_KEY = "is.art.persist.allowed";
   private static final String IS_FOLDER_RECURSION_KEY = "is.folder.recurse.allowed";
   private static final String IS_FILE_WITH_MULTI_PATHS_KEY = "is.file.with.multi.paths";

   private DirectoryOrFileSelector directoryFileSelector;
   private BranchSelectComposite branchSelectComposite;
   private final MutableBoolean isFolderRecursionAllowed;
   private final MutableBoolean isArtifactPersistanceAllowed;
   private List<IResource> currentResourceSelection;
   private final MutableBoolean isFileContainingMultiplePaths;
   private final Map<Button, Boolean> traceUnitHandlers;
   private final Map<String, Button> optionButtons;

   public ImportTraceUnitPage(IStructuredSelection selection) {
      super(PAGE_NAME);
      setTitle(PAGE_TITLE);
      setDescription(PAGE_DESCRIPTION);
      this.traceUnitHandlers = new HashMap<Button, Boolean>();
      this.optionButtons = new HashMap<String, Button>();

      this.isFolderRecursionAllowed = new MutableBoolean(false);
      this.isArtifactPersistanceAllowed = new MutableBoolean(false);
      this.isFileContainingMultiplePaths = new MutableBoolean(false);

      if (selection != null && selection.size() >= 1) {
         currentResourceSelection = new LinkedList<IResource>();
         Iterator<?> it = selection.iterator();
         while (it.hasNext()) {
            Object element = it.next();
            if (element instanceof IAdaptable) {
               IResource toAdd = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
               if (toAdd != null) {
                  currentResourceSelection.add(toAdd);
               }
            }
         }
      }
   }

   @Override
   public void createControl(Composite parent) {
      initializeDialogUnits(parent);

      Composite composite = new Composite(parent, SWT.NULL);
      composite.setLayout(new GridLayout());
      composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
      composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      composite.setFont(parent.getFont());

      createTestUnitSourceArea(composite);
      createBranchSelectArea(composite);
      createParserSelectArea(composite);
      createOptionsArea(composite);

      restoreWidgetValues();
      updateWidgetEnablements();

      setControl(composite);

      setPageComplete(determinePageCompletion());

      directoryFileSelector.addListener(SWT.Selection, new Listener() {

         @Override
         public void handleEvent(Event event) {
            Button button = optionButtons.get(IS_FILE_WITH_MULTI_PATHS_KEY);
            if (isWidgetAccessible(button) && isWidgetAccessible(directoryFileSelector)) {
               button.setEnabled(!directoryFileSelector.isDirectorySelected());
            }
            setPageComplete(determinePageCompletion());
         }
      });
   }

   private boolean isWidgetAccessible(Widget widget) {
      return widget != null && !widget.isDisposed();
   }

   /**
    * The <code>WizardResourceImportPage</code> implementation of this <code>Listener</code> method handles all events
    * and enablements for controls on this page. Subclasses may extend.
    * 
    * @param event Event
    */
   @Override
   public void handleEvent(Event event) {
      setPageComplete(determinePageCompletion());
   }

   protected void createTestUnitSourceArea(Composite parent) {
      directoryFileSelector = new DirectoryOrFileSelector(parent, SWT.NONE, SOURCE_GROUP, this, true);
      updateDirectoryFileSelection();
   }

   private void updateDirectoryFileSelection() {
      directoryFileSelector.setDirectorySelected(true);
      if (currentResourceSelection != null) {
         String text;
         if (currentResourceSelection.isEmpty()) {
            IResource selected = currentResourceSelection.iterator().next();
            directoryFileSelector.setDirectorySelected(selected.getType() != IResource.FILE);
            text = selected.getLocation().toString();
         } else {
            directoryFileSelector.setDirectorySelected(false);
            StringBuilder sb = new StringBuilder();
            for (IResource resource : currentResourceSelection) {
               sb.append(resource.getLocation().toString());
               sb.append(DirectoryOrFileSelector.FILE_SEPARATOR);
            }
            sb.setLength(sb.length() - DirectoryOrFileSelector.FILE_SEPARATOR.length());
            text = sb.toString();
         }
         directoryFileSelector.setText(text);
      }
   }

   protected void createBranchSelectArea(Composite parent) {
      Group composite = new Group(parent, SWT.NONE);
      composite.setText(BRANCH_SELECT_GROUP);
      composite.setLayout(new GridLayout());
      composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      composite.setToolTipText(IMPORT_BRANCH_TOOLTIP);

      branchSelectComposite = new BranchSelectComposite(composite, SWT.BORDER, true);
      branchSelectComposite.setToolTipText(IMPORT_BRANCH_TOOLTIP);
      branchSelectComposite.addListener(new Listener() {

         @Override
         public void handleEvent(Event event) {
            setPageComplete(determinePageCompletion());
         }
      });
   }

   protected void createParserSelectArea(Composite parent) {
      Group composite = new Group(parent, SWT.NONE);
      composite.setText(TRACE_UNIT_HANDLER_GROUP);
      composite.setLayout(new GridLayout());
      composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      try {
         for (TraceHandler handler : TraceUnitExtensionManager.getInstance().getAllTraceHandlers()) {
            createTraceHandler(composite, handler.getName(), handler.getId());
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }

   }

   private void createTraceHandler(Composite parent, String text, String handlerId) {
      Button handlerButton = new Button(parent, SWT.CHECK);
      handlerButton.setText(text);
      handlerButton.setData(handlerId);
      handlerButton.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            Object source = e.getSource();
            if (source instanceof Button) {
               Button button = (Button) source;
               traceUnitHandlers.put(button, button.getSelection());
            }
            setPageComplete(determinePageCompletion());
         }
      });
      traceUnitHandlers.put(handlerButton, false);
   }

   protected void createOptionsArea(Composite parent) {
      Group composite = new Group(parent, SWT.NONE);
      composite.setText(OPTIONS_GROUP);
      composite.setLayout(new GridLayout());
      composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      createOptionButton(composite, IS_ART_PERSIST_ALLOWED_KEY, PERSIST_CHANGES, PERSIST_TOOLTIP,
         isArtifactPersistanceAllowed);
      createOptionButton(composite, IS_FOLDER_RECURSION_KEY, RECURSION_BUTTON, RECURSION_TOOLTIP,
         isFolderRecursionAllowed);
      createOptionButton(composite, IS_FILE_WITH_MULTI_PATHS_KEY, FILE_WITH_PATHS_BUTTON, FILE_WITH_PATHS_TOOLTIP,
         isFileContainingMultiplePaths);
   }

   private void createOptionButton(Composite parent, String buttonId, String buttonText, String buttonToolTip, final MutableBoolean toModify) {
      Button optionButton = new Button(parent, SWT.CHECK);
      optionButton.setText(buttonText);
      optionButton.setToolTipText(buttonToolTip);
      optionButton.setData(toModify);
      optionButton.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            Object object = e.getSource();
            if (object instanceof Button) {
               toModify.setValue(((Button) object).getSelection());
            }
            setPageComplete(determinePageCompletion());
         }
      });
      optionButton.setSelection(toModify.getValue());
      optionButtons.put(buttonId, optionButton);
   }

   /*
    * @see WizardPage#becomesVisible
    */
   @Override
   public void setVisible(boolean visible) {
      super.setVisible(visible);
      // policy: wizards are not allowed to come up with an error message
      if (visible) {
         setErrorMessage(null);
      }
   }

   @Override
   protected boolean validateSourceGroup() {
      boolean result = directoryFileSelector.validate(this);
      if (result) {
         IOseeBranch branch = getSelectedBranch();
         if (branch == null) {
            result = false;
            setErrorMessage("Please select a valid working branch");
         }
      }
      if (result) {
         result &= validateParser(this);
      }
      return result;
   }

   protected boolean validateParser(WizardDataTransferPage wizardPage) {
      boolean result = false;
      String[] selectedHandlers = getTraceUnitHandlerIds();
      if (selectedHandlers.length > 0) {
         result = true;
      } else {
         wizardPage.setErrorMessage(TRACE_UNIT_ERROR);
      }
      return result;
   }

   public Iterable<URI> getSourceURI() {
      List<URI> toReturn = null;
      if (isWidgetAccessible(directoryFileSelector)) {
         toReturn = new LinkedList<URI>();
         for (File file : directoryFileSelector.getSelection()) {
            toReturn.add(file.toURI());
         }
      }
      return toReturn;
   }

   public IOseeBranch getSelectedBranch() {
      return isWidgetAccessible(branchSelectComposite) ? branchSelectComposite.getSelectedBranch() : null;
   }

   public boolean isFolderRecursionAllowed() {
      return isFolderRecursionAllowed.getValue();
   }

   public boolean isArtifactPersistanceAllowed() {
      return isArtifactPersistanceAllowed.getValue();
   }

   public boolean isFileContainingMultiplePaths() {
      return isWidgetAccessible(directoryFileSelector) ? !directoryFileSelector.isDirectorySelected() && isFileContainingMultiplePaths.getValue() : isFileContainingMultiplePaths.getValue();
   }

   public String[] getTraceUnitHandlerIds() {
      List<String> selectedIds = new ArrayList<String>();
      for (Button button : traceUnitHandlers.keySet()) {
         Boolean value = traceUnitHandlers.get(button);
         if (value != null && value == true) {
            selectedIds.add((String) button.getData());
         }
      }
      return selectedIds.toArray(new String[selectedIds.size()]);
   }

   @Override
   protected void restoreWidgetValues() {
      super.restoreWidgetValues();
      IDialogSettings settings = getDialogSettings();
      if (settings != null) {

         String source = settings.get(SOURCE_URI_KEY);
         if (Strings.isValid(source)) {
            directoryFileSelector.setDirectorySelected(settings.getBoolean(SOURCE_URI_IS_DIRECTORY_KEY));
            directoryFileSelector.setText(source);
         }

         for (String id : optionButtons.keySet()) {
            Boolean value = settings.getBoolean(id);
            if (id.equals(IS_FILE_WITH_MULTI_PATHS_KEY)) {
               if (directoryFileSelector.isDirectorySelected()) {
                  value = false;
               }
            }
            Button button = optionButtons.get(id);
            button.setSelection(value);
            Object data = button.getData();
            if (data instanceof MutableBoolean) {
               ((MutableBoolean) data).setValue(value);
            }
         }

         try {
            Integer branchId = settings.getInt(BRANCH_KEY);
            if (branchId > 0) {
               Branch branch = BranchManager.getBranch(branchId);
               if (branch != null) {
                  branchSelectComposite.setSelected(branch);
               }
            }
         } catch (Exception ex) {
            // Do Nothing
         }

         String[] traceHandlers = settings.getArray(SELECTED_TRACE_HANDLERS_KEY);
         if (traceHandlers != null && traceHandlers.length > 0) {
            Set<String> traceIds = new HashSet<String>(Arrays.asList(traceHandlers));
            for (Button button : traceUnitHandlers.keySet()) {
               Object data = button.getData();
               if (data instanceof String) {
                  String id = (String) data;
                  if (traceIds.contains(id)) {
                     button.setSelection(true);
                     traceUnitHandlers.put(button, true);
                  }
               }
            }
         }
      }
   }

   @Override
   protected void saveWidgetValues() {
      super.saveWidgetValues();
      IDialogSettings settings = getDialogSettings();
      if (settings != null) {
         IOseeBranch branch = getSelectedBranch();
         if (branch != null) {
            try {
               settings.put(BRANCH_KEY, BranchManager.getBranchId(branch));
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }

         if (isWidgetAccessible(directoryFileSelector)) {
            settings.put(SOURCE_URI_KEY, directoryFileSelector.getText());
         }

         settings.put(SELECTED_TRACE_HANDLERS_KEY, getTraceUnitHandlerIds());
         settings.put(SOURCE_URI_IS_DIRECTORY_KEY, directoryFileSelector.isDirectorySelected());
         settings.put(IS_ART_PERSIST_ALLOWED_KEY, isArtifactPersistanceAllowed());
         settings.put(IS_FOLDER_RECURSION_KEY, isFolderRecursionAllowed());
         settings.put(IS_FILE_WITH_MULTI_PATHS_KEY, isFileContainingMultiplePaths());
      }
   }

   @Override
   protected boolean allowNewContainerName() {
      return false;
   }

}