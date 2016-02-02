package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.LinkViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface LinkEditRequestedListener {

	void linkEditRequested(LinkViewController cv, User u);

}
