/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.api.util;

import java.util.Collection;
import org.eclipse.osee.framework.jdk.core.type.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public interface IAtsChangeSet {

   void add(Object obj) throws OseeCoreException;

   Collection<Object> getObjects();

   void execute() throws OseeCoreException;

   void clear();

   void addExecuteListener(IExecuteListener listener);

   void addToDelete(Object obj) throws OseeCoreException;

   void addAll(Object... objects) throws OseeCoreException;

   boolean isEmpty();

}
