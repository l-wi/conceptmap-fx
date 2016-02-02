package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.ConceptViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface ConceptEditRequestedListener{

	void conceptEditRequested(InputClosedListener l,ConceptViewController cv, User u);

}
