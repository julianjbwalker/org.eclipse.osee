/*
 * Created on Jun 6, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.config.internal;

import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.core.config.AtsConfigCache;
import org.eclipse.osee.ats.core.config.IActionableItemFactory;
import org.eclipse.osee.framework.jdk.core.util.HumanReadableId;

/**
 * @author Donald G. Dunne
 */
public class ActionableItemFactory implements IActionableItemFactory {

   private final AtsConfigCache cache;

   public ActionableItemFactory(AtsConfigCache cache) {
      this.cache = cache;
   }

   @Override
   public IAtsActionableItem createActionableItem(String guid, String title) {
      return createActionableItem(title, guid, HumanReadableId.generate());
   }

   public IAtsActionableItem createActionableItem(String title, String guid, String humanReadableId) {
      if (guid == null) {
         throw new IllegalArgumentException("guid can not be null");
      }
      IAtsActionableItem ai = new ActionableItem(title, guid, humanReadableId);
      cache.cache(ai);
      return ai;
   }

   @Override
   public IAtsActionableItem getOrCreate(String guid, String name) {
      IAtsActionableItem ai = cache.getSoleByGuid(guid, IAtsActionableItem.class);
      if (ai == null) {
         ai = createActionableItem(guid, name);
      }
      return ai;
   }
}
