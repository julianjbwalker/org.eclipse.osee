/*
 * Created on Sep 22, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.test.import1;

import java.util.logging.Level;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Donald G. Dunne
 */
public class NavigationButton3 extends Button {

   public Image image;

   /**
    * @param parent
    * @param style
    */
   public NavigationButton3(Composite parent, int style, Image image) {
      super(parent, style);
   }

   public String getRationale() {
      try {
         if (getStyle() == 4) { // 1, 1, y
            return "Navigate Here"; // 1, 2, y
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex); // 1, 3, n
      }
      return "Navigate"; // 1, 4, n
   }

}
