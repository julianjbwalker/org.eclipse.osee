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
package org.eclipse.osee.framework.skynet.core.commit;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Roberto E. Escobar
 */
public interface IChangeDataAccessor {

   public Collection<OseeChange> getChangeData(IProgressMonitor monitor, IChangeFactory factory, IChangeLocator locator) throws Exception;

}
