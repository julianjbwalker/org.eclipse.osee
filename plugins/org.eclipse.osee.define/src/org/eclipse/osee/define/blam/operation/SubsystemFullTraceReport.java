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
package org.eclipse.osee.define.blam.operation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.define.traceability.ScriptTraceabilityOperation;
import org.eclipse.osee.define.traceability.TraceUnitExtensionManager;
import org.eclipse.osee.define.traceability.TraceUnitExtensionManager.TraceHandler;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.io.CharBackedInputStream;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ExcelXmlWriter;
import org.eclipse.osee.framework.jdk.core.util.io.xml.ISheetWriter;
import org.eclipse.osee.framework.plugin.core.util.AIFile;
import org.eclipse.osee.framework.plugin.core.util.OseeData;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.widgets.XCheckBox;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XText;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.SwtXWidgetRenderer;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Ryan D. Brooks
 */
public class SubsystemFullTraceReport extends AbstractBlam {
   private CharBackedInputStream charBak;
   private ISheetWriter writer;
   private HashCollection<Artifact, String> requirementsToCodeUnits;
   private static int SOFTWARE_REQUIREMENT_INDEX = 9;
   private static int TEST_INDEX = 13;
   private final ArrayList<String> tests = new ArrayList<String>(50);

   private final String SCRIPT_ROOT_DIR = "Script Root Directory";
   private final String USE_TRACE_IN_OSEE = "Use traceability from Subsystem Requirements";
   private XCheckBox useTraceInOsee;
   private XText scriptDir;

   private static final String TRACE_HANDLER_CHECKBOX =
      "<XWidget xwidgetType=\"XCheckBox\" displayName=\"%s\" labelAfter=\"true\" horizontalLabel=\"true\"/>";
   private Collection<String> availableTraceHandlers;

   @Override
   public String getName() {
      return "Subsystem Full Trace Report";
   }

   private void init() throws IOException {
      charBak = new CharBackedInputStream();
      writer = new ExcelXmlWriter(charBak.getWriter());
   }

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      List<Artifact> artifacts = variableMap.getArtifacts("Subsystem Requirements");
      if (artifacts.isEmpty()) {
         throw new OseeArgumentException("must specify a set of artifacts");
      }
      IOseeBranch branch = artifacts.get(0).getBranch();

      init();
      String scriptDir = variableMap.getString(SCRIPT_ROOT_DIR);
      Boolean checked = variableMap.getBoolean(USE_TRACE_IN_OSEE);

      Collection<TraceHandler> traceHandlers = new LinkedList<TraceHandler>();
      for (String handler : availableTraceHandlers) {
         if (variableMap.getBoolean(handler)) {
            TraceHandler traceHandler = TraceUnitExtensionManager.getInstance().getTraceHandlerByName(handler);
            traceHandlers.add(traceHandler);
         }
      }

      if (!checked) {
         File dir = new File(scriptDir);
         if (dir.exists()) {
            ScriptTraceabilityOperation traceOperation =
               new ScriptTraceabilityOperation(dir.getParentFile(), branch, false, traceHandlers);
            Operations.executeWorkAndCheckStatus(traceOperation, monitor);
            requirementsToCodeUnits = traceOperation.getRequirementToCodeUnitsMap();
         }
      }

      writeMainSheet(prepareSubsystemRequirements(artifacts));

      writer.endWorkbook();
      IFile iFile = OseeData.getIFile("Subsystem_Trace_Report_" + Lib.getDateTimeString() + ".xml");
      AIFile.writeToFile(iFile, charBak);
      Program.launch(iFile.getLocation().toOSString());
   }

   private List<Artifact> prepareSubsystemRequirements(List<Artifact> artifacts) throws OseeCoreException {
      List<Artifact> subsystemRequirements = new ArrayList<Artifact>(400);
      for (Artifact artifact : artifacts) {
         if (artifact.isOfType(CoreArtifactTypes.Folder)) {
            subsystemRequirements.addAll(artifact.getDescendants());
         } else {
            subsystemRequirements.add(artifact);
         }
      }
      return subsystemRequirements;
   }

   private void writeMainSheet(List<Artifact> artifacts) throws IOException, OseeCoreException {
      writer.startSheet("report", 18);
      writer.writeRow(CoreArtifactTypes.SystemRequirementMSWord.getName(), null, null,
         CoreArtifactTypes.SubsystemRequirementMSWord.getName(), null, null, null, null, null,
         CoreArtifactTypes.SoftwareRequirement.getName());
      writer.writeRow("Paragraph #", "Requirement Name", "Requirement Text", "Paragraph #", "Requirement Name",
         "Requirement Text", "Subsystem", CoreAttributeTypes.QualificationMethod.getName(), "Test Procedure",
         "Paragraph #", "Requirement Name", "Partitions", CoreAttributeTypes.QualificationMethod.getName(),
         "Test Script/Test Procedure");

      for (Artifact subSystemRequirement : artifacts) {
         processSubSystemRequirement(subSystemRequirement);
      }
      writer.endSheet();
   }

   private void processSubSystemRequirement(Artifact subSystemRequirement) throws IOException, OseeCoreException {
      boolean topRowForSubsystemReq = true;
      for (Artifact systemRequirement : subSystemRequirement.getRelatedArtifacts(CoreRelationTypes.Requirement_Trace__Higher_Level)) {
         writer.writeCell(systemRequirement.getSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, ""));
         writer.writeCell(systemRequirement.getName());
         writer.writeCell(getRequirementText(systemRequirement));

         if (topRowForSubsystemReq) {
            writer.writeCell(subSystemRequirement.getSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, ""));
            writer.writeCell(subSystemRequirement.getName());
            writer.writeCell(getRequirementText(subSystemRequirement));
            writer.writeCell(subSystemRequirement.getSoleAttributeValue(CoreAttributeTypes.Subsystem, ""));
            writer.writeCell(subSystemRequirement.getAttributesToStringSorted(CoreAttributeTypes.QualificationMethod));
            writer.writeCell(Collections.toString(",",
               subSystemRequirement.getRelatedArtifacts(CoreRelationTypes.Verification__Verifier)));
            topRowForSubsystemReq = false;
         }

         for (Artifact softwareRequirement : subSystemRequirement.getRelatedArtifacts(CoreRelationTypes.Requirement_Trace__Lower_Level)) {
            processSoftwareRequirement(softwareRequirement);
         }
         writer.endRow();
      }
   }

   private String getRequirementText(Artifact req) throws OseeCoreException {
      Attribute<?> templateContent = req.getSoleAttribute(CoreAttributeTypes.WordTemplateContent);
      String ret = templateContent.getDisplayableString();
      return StringUtils.trim(ret);
   }

   private void processSoftwareRequirement(Artifact softwareRequirement) throws IOException, OseeCoreException {
      writer.writeCell(softwareRequirement.getSoleAttributeValue(CoreAttributeTypes.ParagraphNumber, ""),
         SOFTWARE_REQUIREMENT_INDEX);
      writer.writeCell(softwareRequirement.getName());
      writer.writeCell(Collections.toString(",",
         softwareRequirement.getAttributesToStringList(CoreAttributeTypes.Partition)));
      writer.writeCell(softwareRequirement.getAttributesToStringSorted(CoreAttributeTypes.QualificationMethod));

      tests.clear();
      for (Artifact testProcedure : softwareRequirement.getRelatedArtifacts(CoreRelationTypes.Validation__Validator)) {
         tests.add(testProcedure.getName());
      }
      Collection<String> testScripts = null;
      if (requirementsToCodeUnits != null) {
         testScripts = requirementsToCodeUnits.getValues(softwareRequirement);
      } else {
         List<Artifact> relatedArtifacts =
            softwareRequirement.getRelatedArtifacts(CoreRelationTypes.Verification__Verifier);
         testScripts = Artifacts.getNames(relatedArtifacts);
      }
      if (testScripts != null) {
         for (String testScript : testScripts) {
            tests.add(new File(testScript).getName());
         }
      }
      writer.writeCell(Collections.toString(", ", tests), TEST_INDEX);
      writer.endRow();
   }

   @Override
   public String getXWidgetsXml() throws OseeCoreException {
      StringBuilder sb = new StringBuilder();
      sb.append("<xWidgets>");
      sb.append("<XWidget xwidgetType=\"XCheckBox\" displayName=\"" + USE_TRACE_IN_OSEE + "\" defaultValue=\"true\" labelAfter=\"true\" horizontalLabel=\"true\" />");
      sb.append("<XWidget xwidgetType=\"XText\" displayName=\"" + SCRIPT_ROOT_DIR + "\" defaultValue=\"C:/UserData/workspaceScripts\" toolTip=\"Leave blank if test script traceability is not needed.\" />");
      availableTraceHandlers = new LinkedList<String>();
      sb.append("<XWidget xwidgetType=\"XLabel\" displayName=\"Select appropriate script parser (if script traceability needed):\" />");
      Collection<String> traceHandlerNames = TraceUnitExtensionManager.getInstance().getAllTraceHandlerNames();
      for (String handler : traceHandlerNames) {
         sb.append(String.format(TRACE_HANDLER_CHECKBOX, handler));
         availableTraceHandlers.add(handler);
      }
      sb.append("<XWidget xwidgetType=\"XListDropViewer\" displayName=\"Subsystem Requirements\" />");
      sb.append("</xWidgets>");
      return sb.toString();
   }

   @Override
   public String getDescriptionUsage() {
      return "Generates subsystem requirement full traceability report";
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Define.Trace");
   }

   @Override
   public void widgetCreated(XWidget widget, FormToolkit toolkit, Artifact art, SwtXWidgetRenderer dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
      super.widgetCreated(widget, toolkit, art, dynamicXWidgetLayout, modListener, isEditable);

      if (widget.getLabel().equals(SCRIPT_ROOT_DIR)) {
         scriptDir = (XText) widget;
         scriptDir.setEnabled(false);
         scriptDir.getControl().setBackground(Displays.getSystemColor(SWT.COLOR_GRAY));
      }

      if (widget.getLabel().equals(USE_TRACE_IN_OSEE)) {
         useTraceInOsee = (XCheckBox) widget;
         useTraceInOsee.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
               if (useTraceInOsee.isChecked()) {
                  scriptDir.setEnabled(false);
                  scriptDir.getControl().setBackground(Displays.getSystemColor(SWT.COLOR_GRAY));
               } else {
                  scriptDir.setEnabled(true);
                  scriptDir.getControl().setBackground(Displays.getSystemColor(SWT.COLOR_WHITE));
               }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
               //Do Nothing
            }
         });
      }
   }
}