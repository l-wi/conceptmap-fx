package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.ConceptViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface ConceptMovedListener {

	void conceptMoved(double x, double y, double rotate, ConceptViewController cv, User u);
}
