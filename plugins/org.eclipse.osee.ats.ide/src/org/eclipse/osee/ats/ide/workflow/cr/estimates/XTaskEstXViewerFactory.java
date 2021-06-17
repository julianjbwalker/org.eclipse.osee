/*******************************************************************************
 * Copyright (c) 2021 Boeing.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.ide.workflow.cr.estimates;

import org.eclipse.osee.ats.ide.workflow.task.mini.MiniTaskXViewerFactory;

/**
 * @author Donald G. Dunne
 */
public class XTaskEstXViewerFactory extends MiniTaskXViewerFactory {

   public final static String NAMESPACE = "TaskEstXViewer";

   public XTaskEstXViewerFactory() {
      super(NAMESPACE);
   }

}
