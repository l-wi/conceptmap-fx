package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface ConceptDeletedListener {

	void conceptDeleted(ConceptViewController cv, User u);
}
