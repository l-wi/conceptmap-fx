package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.Editable;
import de.unisaarland.edutech.conceptmapping.User;

public interface ConceptEditRequestedListener{

	void conceptEditRequested(InputClosedListener l,Editable cv, User u);

}
