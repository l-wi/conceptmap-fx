package de.unisaarland.edutech.conceptmapfx.observablemap;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Link;

public interface ConceptMapObserver {

	void changedMap(ConceptMap oldMap , ConceptMap newMap);
	
	void changedConcept(Concept oldConcept, Concept newConcept);
	
	void changedContent(CollaborativeString oldContent, CollaborativeString newContent);
}
