package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapping.User;

public interface ConceptEditRequestedListener{

	void conceptEditRequested(InputClosedListener l,CollaborativeStringTextFieldBinding cv, User u);

}
