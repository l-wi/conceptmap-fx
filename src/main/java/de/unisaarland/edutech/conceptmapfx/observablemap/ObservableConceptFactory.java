package de.unisaarland.edutech.conceptmapfx.observablemap;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptFactory;
import de.unisaarland.edutech.conceptmapping.User;

public class ObservableConceptFactory implements ConceptFactory {

	private List<ConceptMapObserver> observers = new ArrayList<>();

	public void addListener(ConceptMapObserver c) {
		this.observers.add(c);
	}

	@Override
	public Concept create(User u) {
		ObservableCollaborativeString name = new ObservableCollaborativeString(u);
		ObservableConcept c = new ObservableConcept(name);
		observers.forEach((l) -> {
			c.addListener(l);
			name.addListener(l);
		});
		return c;
	}

	@Override
	public Concept create(User u, String s) {
		ObservableCollaborativeString name = new ObservableCollaborativeString(u, s);
		ObservableConcept c = new ObservableConcept(name);
		observers.forEach((l) -> {
			c.addListener(l);
			name.addListener(l);
		});
		return c;

	}

}
