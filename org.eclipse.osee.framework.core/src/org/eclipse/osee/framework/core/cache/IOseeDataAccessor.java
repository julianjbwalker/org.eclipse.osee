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
package org.eclipse.osee.framework.core.cache;

import java.util.Collection;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.IOseeStorableType;

/**
 * @author Roberto E. Escobar
 */
public interface IOseeDataAccessor<T extends IOseeStorableType> {

   public void load(IOseeCache<T> cache) throws OseeCoreException;

   public void store(Collection<T> types) throws OseeCoreException;
}
