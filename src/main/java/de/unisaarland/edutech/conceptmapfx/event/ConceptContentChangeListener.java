package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;

public interface ConceptContentChangeListener {

	void conceptContentChanged(ConceptViewController source,String oldContent,String newContent);
}
