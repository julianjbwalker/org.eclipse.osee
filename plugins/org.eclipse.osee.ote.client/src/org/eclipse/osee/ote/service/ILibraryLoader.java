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
package org.eclipse.osee.ote.service;

public interface ILibraryLoader {
   /**
    * loads a message class dictionary. If one is already loaded then it will be unloaded.
    */
   void loadMessageDictionary(IMessageDictionary dictionary);

   /**
    * unloads the current {@link IMessageDictionary} from the system. 
    */
   void unloadMessageDictionary();

   /**
    * gets the currently loaded {@link IMessageDictionary}
    * 
    * @return the {@link IMessageDictionary} or null if one is not loaded
    */
   IMessageDictionary getLoadedDictionary();
}
