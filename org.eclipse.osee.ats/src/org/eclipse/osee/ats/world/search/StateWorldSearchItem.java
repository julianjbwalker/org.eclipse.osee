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
package org.eclipse.osee.ats.world.search;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.artifact.ATSAttributes;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.AttributeValueSearch;
import org.eclipse.osee.framework.skynet.core.artifact.search.ISearchPrimitive;
import org.eclipse.osee.framework.skynet.core.artifact.search.Operator;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;

/**
 * @author Donald G. Dunne
 */
public class StateWorldSearchItem extends WorldSearchItem {

   private final String stateClass;
   private String selectedStateClass;

   public StateWorldSearchItem(String name) {
      this(name, null);
   }

   public StateWorldSearchItem() {
      this("Search by Current State", null);

   }

   public String getStateSearchName() {
      if (stateClass != null)
         return stateClass;
      else
         return selectedStateClass;
   }

   @Override
   public String getSelectedName() {
      return String.format("%s - %s", super.getSelectedName(), getStateSearchName());
   }

   public StateWorldSearchItem(String name, String stateClass) {
      super(name);
      this.stateClass = stateClass;
   }

   @Override
   public void performSearch() throws SQLException, IllegalArgumentException {
      if (stateClass != null)
         searchIt(stateClass);
      else
         searchIt();
   }

   private void searchIt(String stateClass) throws SQLException {
      List<ISearchPrimitive> baseCriteria;
      baseCriteria = new LinkedList<ISearchPrimitive>();
      baseCriteria.add(new AttributeValueSearch(ATSAttributes.CURRENT_STATE_ATTRIBUTE.getStoreName(), stateClass,
            Operator.CONTAINS));

      Collection<Artifact> arts = ArtifactPersistenceManager.getInstance().getArtifacts(baseCriteria, true,
            BranchPersistenceManager.getInstance().getAtsBranch());
      if (isCancelled()) return;
      addResultArtifacts(arts);

   }

   private void searchIt() throws SQLException, IllegalArgumentException {
      if (selectedStateClass != null) searchIt(selectedStateClass);
   }

   @Override
   public boolean performUI() {
      if (stateClass != null) return true;
      EntryDialog ed = new EntryDialog("Enter State", "Enter state name.");
      if (ed.open() == 0) {
         selectedStateClass = ed.getEntry();
         return true;
      }
      return false;
   }

}
