package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapping.User;

public interface LinkEditRequestedListener {

	void linkEditRequested(InputClosedListener l,CollaborativeStringTextFieldBinding cv, User u);

}
