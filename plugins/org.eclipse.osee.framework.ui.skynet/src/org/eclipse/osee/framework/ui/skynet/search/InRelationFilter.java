/*********************************************************************
 * Copyright (c) 2004, 2007 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/

package org.eclipse.osee.framework.ui.skynet.search;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.osee.framework.core.data.RelationTypeToken;
import org.eclipse.osee.framework.core.enums.RelationSide;
import org.eclipse.osee.framework.jdk.core.type.OseeArgumentException;
import org.eclipse.osee.framework.skynet.core.artifact.search.ISearchPrimitive;
import org.eclipse.osee.framework.skynet.core.artifact.search.InRelationSearch;
import org.eclipse.osee.framework.ui.skynet.search.filter.FilterTableViewer;
import org.eclipse.swt.widgets.Control;

/**
 * @author Ryan D. Brooks
 */
public class InRelationFilter extends SearchFilter {
   private final ComboViewer relationTypeList;
   private final ComboViewer relationSideList;

   public InRelationFilter(Control optionsControl, ComboViewer relationTypeList, ComboViewer relationSideList) {
      super("Artifact in Relation", optionsControl);
      this.relationTypeList = relationTypeList;
      this.relationSideList = relationSideList;
   }

   @Override
   public void addFilterTo(FilterTableViewer filterViewer) {
      String typeName = relationTypeList.getCombo().getText();
      String sideName = relationSideList.getCombo().getText();

      RelationTypeToken relationType = (RelationTypeToken) relationTypeList.getData(typeName);
      Boolean sideAName = null;
      try {
         sideAName = relationType.getSideName(RelationSide.SIDE_A).equals(sideName);
      } catch (OseeArgumentException ex) {
         // do nothing, user wants either
      }
      ISearchPrimitive primitive = new InRelationSearch(relationType, sideAName);

      filterViewer.addItem(primitive, getFilterName(), typeName, sideName);
   }

   @Override
   public boolean isValid() {
      return true;
   }

   @Override
   public void loadFromStorageString(FilterTableViewer filterViewer, String type, String value, String storageString, boolean isNotEnabled) {
      ISearchPrimitive primitive = InRelationSearch.getPrimitive(storageString);
      filterViewer.addItem(primitive, getFilterName(), type, value);
   }

   @Override
   public String getSearchDescription() {
      return "This search will return all artifacts in the selected relation";
   }
}
