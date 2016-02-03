package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.ConceptViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface ConceptMovingListener {

	void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u);
}
