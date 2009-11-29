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

package org.eclipse.osee.framework.ui.skynet.render.word.template;

import org.eclipse.osee.framework.core.enums.CoreAttributes;
import org.eclipse.osee.framework.core.enums.CoreRelations;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.ui.skynet.render.word.WordMLProducer;

/**
 * @author Andrew M. Finkbeiner
 */
public class TISAttributeHandler implements ITemplateAttributeHandler {

   @Override
   public void process(WordMLProducer wordMl, Artifact artifact, TemplateAttribute attribute) throws OseeCoreException {
      for (Artifact requirement : artifact.getRelatedArtifacts(CoreRelations.Verification__Requirement)) {
         wordMl.addParagraphBold(requirement.getSoleAttributeValue(CoreAttributes.PARAGRAPH_NUMBER, "") + "\t" + requirement.getName());
         String str = requirement.getSoleAttributeValue(WordAttribute.WORD_TEMPLATE_CONTENT);
         wordMl.addWordMl(str);
      }
   }

   @Override
   public boolean canHandle(Artifact artifact, TemplateAttribute attribute) throws OseeCoreException {
      return attribute.getName().equals("TIS Traceability");
   }

}
