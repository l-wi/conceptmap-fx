package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.Editable;
import de.unisaarland.edutech.conceptmapfx.LinkViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface LinkEditRequestedListener {

	void linkEditRequested(InputClosedListener l,Editable cv, User u);

}
