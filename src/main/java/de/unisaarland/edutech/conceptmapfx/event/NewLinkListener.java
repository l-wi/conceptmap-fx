package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.ConceptViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface NewLinkListener {

	void newLink(ConceptViewController cv1, ConceptViewController c2, User u);
}
