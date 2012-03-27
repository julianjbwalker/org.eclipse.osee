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
package org.eclipse.osee.ats.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.core.actions.ISelectedAtsArtifacts;
import org.eclipse.osee.ats.core.artifact.GoalArtifact;
import org.eclipse.osee.ats.core.task.TaskArtifact;
import org.eclipse.osee.ats.core.type.AtsArtifactTypes;
import org.eclipse.osee.ats.core.type.AtsRelationTypes;
import org.eclipse.osee.ats.core.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.goal.GoalXViewerFactory;
import org.eclipse.osee.ats.goal.RemoveFromGoalAction;
import org.eclipse.osee.ats.goal.SetGoalOrderAction;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.world.IMenuActionProvider;
import org.eclipse.osee.ats.world.IWorldEditor;
import org.eclipse.osee.ats.world.IWorldEditorProvider;
import org.eclipse.osee.ats.world.WorldComposite;
import org.eclipse.osee.ats.world.WorldLabelProvider;
import org.eclipse.osee.ats.world.WorldXViewer;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactDragAndDrop;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.Widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Roberto E. Escobar
 * @author Donald G. Dunne
 */
public class SMAGoalMembersSection extends Composite implements ISelectedAtsArtifacts, IWorldEditor, IMenuActionProvider {

   private final SMAEditor editor;
   private WorldComposite worldComposite;
   private static final Map<String, CustomizeData> editorToCustDataMap = new HashMap<String, CustomizeData>(20);
   private final String id;
   private final Integer defaultTableWidth;

   public SMAGoalMembersSection(String id, SMAEditor editor, Composite parent, int style, Integer defaultTableWidth) {
      super(parent, style);
      this.id = id;
      this.editor = editor;
      this.defaultTableWidth = defaultTableWidth;

      setLayout(new GridLayout(2, true));
      setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      ToolBar toolBar = createToolBar();
      addDropToAddLabel(editor.getToolkit());
      addDropToRemoveLabel(editor.getToolkit());

      createWorldComposite();
      createActions();
      setupListenersForCustomizeDataCaching();
      fillActionBar(toolBar);
      editor.getToolkit().adapt(this);
   }

   private ToolBar createToolBar() {
      Composite actionComp = new Composite(this, SWT.NONE);
      actionComp.setLayout(ALayout.getZeroMarginLayout());
      GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
      gd.horizontalSpan = 2;
      actionComp.setLayoutData(gd);

      ToolBar toolBar = new ToolBar(actionComp, SWT.FLAT | SWT.RIGHT);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      toolBar.setLayoutData(gd);

      editor.getToolkit().adapt(actionComp);
      editor.getToolkit().adapt(toolBar);
      return toolBar;
   }

   private void refreshTableSize() {
      GridData gd = null;
      if (defaultTableWidth != null) {
         gd = new GridData(SWT.FILL, SWT.NONE, true, false);
         gd.heightHint = defaultTableWidth;
      } else {
         gd = new GridData(SWT.FILL, SWT.FILL, true, true);
      }
      gd.widthHint = 200;
      gd.horizontalSpan = 2;
      worldComposite.setLayoutData(gd);
      worldComposite.layout(true);
      layout();
      getParent().layout();
   }

   private void fillActionBar(ToolBar toolBar) {

      new ActionContributionItem(worldComposite.getXViewer().getCustomizeAction()).fill(toolBar, -1);
   }

   private void createWorldComposite() {
      worldComposite =
         new WorldComposite(this, new GoalXViewerFactory((GoalArtifact) editor.getAwa()), this, SWT.BORDER);

      CustomizeData customizeData = editorToCustDataMap.get(getTableExpandKey());
      if (customizeData == null) {
         customizeData = worldComposite.getCustomizeDataCopy();
      }
      WorldLabelProvider labelProvider = (WorldLabelProvider) worldComposite.getXViewer().getLabelProvider();
      labelProvider.setParentGoal((GoalArtifact) editor.getAwa());

      worldComposite.getWorldXViewer().addMenuActionProvider(this);

      GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
      gd.horizontalSpan = 2;
      worldComposite.setLayoutData(gd);

      try {
         customizeData = null;
         worldComposite.load("Members", editor.getAwa().getRelatedArtifacts(AtsRelationTypes.Goal_Member),
            customizeData, TableLoadOption.None);

      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      refreshTableSize();

   }

   private String getTableExpandKey() {
      return editor.getAwa().getHumanReadableId() + id;
   }

   private void setupListenersForCustomizeDataCaching() {
      worldComposite.addDisposeListener(new DisposeListener() {

         @Override
         public void widgetDisposed(DisposeEvent e) {
            editorToCustDataMap.put(getTableExpandKey(), worldComposite.getCustomizeDataCopy());
         }
      });
      editor.addEditorListeners(new ISMAEditorListener() {

         @Override
         public void editorDisposing() {
            editorToCustDataMap.remove(getTableExpandKey());
         }
      });
   }

   protected void addDropToAddLabel(FormToolkit toolkit) {
      Label dropToAddLabel = new Label(this, SWT.BORDER);
      dropToAddLabel.setText(" Drop New Members Here");
      dropToAddLabel.setBackgroundImage(ImageManager.getImage(AtsImage.DROP_HERE_TO_ADD_BACKGROUND));
      GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
      gd.heightHint = 25;
      dropToAddLabel.setLayoutData(gd);
      toolkit.adapt(dropToAddLabel, true, true);

      new ArtifactDragAndDrop(dropToAddLabel, editor.getAwa(), ArtifactEditor.EDITOR_ID) {
         @Override
         public void performArtifactDrop(Artifact[] dropArtifacts) {
            super.performArtifactDrop(dropArtifacts);
            try {
               List<Artifact> members = new ArrayList<Artifact>();
               members.addAll(((GoalArtifact) editor.getAwa()).getMembers());
               for (Artifact art : dropArtifacts) {
                  if (!members.contains(art)) {
                     members.add(art);
                     editor.getAwa().addRelation(AtsRelationTypes.Goal_Member, art);
                  }
               }
               editor.getAwa().setRelationOrder(AtsRelationTypes.Goal_Member, members);
               editor.doSave(null);
            } catch (OseeCoreException ex) {
               OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      };
   }

   protected void addDropToRemoveLabel(FormToolkit toolkit) {
      Label dropToAddLabel = new Label(this, SWT.BORDER);
      dropToAddLabel.setText(" Drop Members to Remove");
      dropToAddLabel.setBackgroundImage(ImageManager.getImage(AtsImage.DROP_HERE_TO_REMOVE_BACKGROUND));
      GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
      gd.heightHint = 25;
      dropToAddLabel.setLayoutData(gd);
      toolkit.adapt(dropToAddLabel, true, true);

      new ArtifactDragAndDrop(dropToAddLabel, editor.getAwa(), ArtifactEditor.EDITOR_ID) {
         @Override
         public void performArtifactDrop(Artifact[] dropArtifacts) {
            super.performArtifactDrop(dropArtifacts);
            final Set<Artifact> artifacts = new HashSet<Artifact>();
            final List<TaskArtifact> tasks = new ArrayList<TaskArtifact>();
            final List<Artifact> artList = new ArrayList<Artifact>();
            for (Artifact artifact : dropArtifacts) {
               artifacts.add(artifact);
               artList.add(artifact);
               if (artifact instanceof TaskArtifact) {
                  tasks.add((TaskArtifact) artifact);
               }
            }
            RemoveFromGoalAction remove =
               new RemoveFromGoalAction((GoalArtifact) editor.getAwa(), new ISelectedAtsArtifacts() {

                  @Override
                  public Set<? extends Artifact> getSelectedSMAArtifacts() {
                     return artifacts;
                  }

                  @Override
                  public List<Artifact> getSelectedAtsArtifacts() {
                     return artList;
                  }

                  @Override
                  public List<TaskArtifact> getSelectedTaskArtifacts() {
                     return tasks;
                  }
               });
            remove.run();
         }
      };
   }

   public void refresh() {
      Displays.ensureInDisplayThread(new Runnable() {

         @Override
         public void run() {
            if (Widgets.isAccessible(worldComposite)) {
               worldComposite.getXViewer().refresh();
            }
         }
      });
   }

   @Override
   public void dispose() {
      if (Widgets.isAccessible(worldComposite)) {
         worldComposite.dispose();
      }
      super.dispose();
   }

   @Override
   public void createToolBarPulldown(Menu menu) {
      // do nothing
   }

   @Override
   public String getCurrentTitleLabel() {
      return "";
   }

   @Override
   public IWorldEditorProvider getWorldEditorProvider() {
      return null;
   }

   @Override
   public void reSearch() {
      // do nothing
   }

   @Override
   public void reflow() {
      // do nothing
   }

   @Override
   public void setTableTitle(String title, boolean warning) {
      // do nothing
   }

   Action setGoalOrderAction, removeFromGoalAction;

   public void createActions() {
      setGoalOrderAction = new SetGoalOrderAction((GoalArtifact) editor.getAwa(), this);
      removeFromGoalAction = new RemoveFromGoalAction((GoalArtifact) editor.getAwa(), this);
   }

   @Override
   public void updateMenuActionsForTable() {
      MenuManager mm = worldComposite.getXViewer().getMenuManager();

      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, setGoalOrderAction);
      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, removeFromGoalAction);
      mm.insertBefore(WorldXViewer.MENU_GROUP_ATS_WORLD_EDIT, new Separator());
   }

   @Override
   public Set<Artifact> getSelectedSMAArtifacts() {
      Set<Artifact> artifacts = new HashSet<Artifact>();
      for (Artifact art : worldComposite.getSelectedArtifacts()) {
         if (art instanceof AbstractWorkflowArtifact) {
            artifacts.add(art);
         }
      }
      return artifacts;
   }

   @Override
   public List<Artifact> getSelectedAtsArtifacts() {
      List<Artifact> artifacts = new ArrayList<Artifact>();
      for (Artifact art : worldComposite.getSelectedArtifacts()) {
         if (art.isOfType(AtsArtifactTypes.AtsArtifact)) {
            artifacts.add(art);
         }
      }
      return artifacts;
   }

   @Override
   public List<TaskArtifact> getSelectedTaskArtifacts() {
      List<TaskArtifact> tasks = new ArrayList<TaskArtifact>();
      for (Artifact art : worldComposite.getSelectedArtifacts()) {
         if (art instanceof TaskArtifact) {
            tasks.add((TaskArtifact) art);
         }
      }
      return tasks;
   }

   public WorldComposite getWorldComposite() {
      return worldComposite;
   }

}
