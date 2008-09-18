/*
 * Created on Sep 13, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.event;

import java.util.Collection;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.event.UnloadedArtifact;

/**
 * @author Donald G. Dunne
 */
public interface IArtifactsPurgedEventListener extends IEventListner {
   public void handleArtifactsPurgedEvent(Sender sender, Collection<? extends Artifact> cacheArtifacts, Collection<UnloadedArtifact> unloadedArtifacts);

}
