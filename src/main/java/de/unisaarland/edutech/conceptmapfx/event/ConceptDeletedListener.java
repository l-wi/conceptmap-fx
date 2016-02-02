package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.ConceptViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface ConceptDeletedListener {

	void conceptDeleted(ConceptViewController cv, User u);
}
