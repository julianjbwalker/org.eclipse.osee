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
package org.eclipse.osee.framework.ui.skynet;

import java.util.Date;
import java.util.GregorianCalendar;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osee.framework.skynet.core.attribute.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.BinaryAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.BooleanAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.DateAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.EnumeratedAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.widgets.cellEditor.DateValue;
import org.eclipse.osee.framework.ui.skynet.widgets.cellEditor.EnumeratedValue;
import org.eclipse.osee.framework.ui.skynet.widgets.cellEditor.StringValue;
import org.eclipse.osee.framework.ui.swt.IDirtiableEditor;
import org.eclipse.swt.widgets.Item;

/**
 * @author Ryan D. Brooks
 */
public class AttributeCellModifier implements ICellModifier {
   private TableViewer tableViewer;
   private DateValue dateValue;
   private EnumeratedValue enumeratedValue;
   private StringValue stringValue;
   private IDirtiableEditor editor;

   private AttributesComposite attrComp;

   public AttributeCellModifier(IDirtiableEditor editor, TableViewer tableViewer, AttributesComposite attrComp) {
      super();
      this.tableViewer = tableViewer;
      this.attrComp = attrComp;
      this.dateValue = new DateValue();
      this.enumeratedValue = new EnumeratedValue();
      this.stringValue = new StringValue();
      this.editor = editor;

      // this.pList = new PermissionList();
      // pList.addPermission(Permission.PermissionEnum.EDITREQUIREMENT);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
    */
   public boolean canModify(Object element, String property) {
      attrComp.updateLabel("");
      if (element != null) {
         if (element instanceof Item) {
            element = ((Item) element).getData();
         }
         try {
            Attribute<?> attribute = (Attribute<?>) element;

            if (attribute instanceof WordAttribute) {
               return false;
            }
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, true);
         }
      }
      return property.equals("value");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
    */
   public Object getValue(Object element, String property) {
      Attribute<?> attribute = (Attribute<?>) element;
      Object object = attribute.getValue();
      if (attribute instanceof EnumeratedAttribute) {
         enumeratedValue.setValue(attribute.getDisplayableString());
         enumeratedValue.setChocies(((EnumeratedAttribute) attribute).getChoices());
         return enumeratedValue;
      } else if (attribute instanceof BooleanAttribute) {
         enumeratedValue.setValue(attribute.getDisplayableString());
         enumeratedValue.setChocies(BooleanAttribute.booleanChoices);
         return enumeratedValue;
      } else if (object instanceof Date) {
         dateValue.setValue((Date) object);
         return dateValue;
      } else {
         stringValue.setValue(attribute.getDisplayableString());
         return stringValue;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String,
    *      java.lang.Object)
    */
   public void modify(Object element, String property, Object value) {
      if (element != null) {
         // Note that it is possible for an SWT Item to be passed instead of the model element.
         if (element instanceof Item) {
            element = ((Item) element).getData();
         }
         try {
            Attribute<?> attribute = (Attribute<?>) element;

            if (attribute instanceof DateAttribute) {
               if (value instanceof GregorianCalendar) {
                  ((DateAttribute) attribute).setValue(new Date(((GregorianCalendar) value).getTimeInMillis()));
               } else {
                  ((DateAttribute) attribute).setValue((Date) value);
               }
            } else if (!(attribute instanceof BinaryAttribute)) {
               //binary attributes should not be changed.
               attribute.setFromString((String) value);
            }
         } catch (Exception ex) {
            OSEELog.logException(SkynetGuiPlugin.class, ex, true);
         }
         tableViewer.update(element, null);
         editor.onDirtied();
         attrComp.notifyModifyAttribuesListeners();
      }
   }
}
