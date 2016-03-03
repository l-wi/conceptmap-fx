package de.unisaarland.edutech.conceptmapfx.observablemap;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;

public class ObservableConcept extends Concept implements Cloneable,Observable {

	private List<ConceptMapObserver> observers = new ArrayList<>();

	public ObservableConcept(CollaborativeString name) {
		super(name);
	}

	public void addListener(ConceptMapObserver c) {
		this.observers.add(c);
	}

	@Override
	public void setPosition(double x, double y, double r) {
		observers.forEach((l) -> l.beforeChange());
		super.setPosition(x, y, r);
		observers.forEach((l) -> l.afterChange());
	}

	@Override
	public ObservableConcept clone() {
		ObservableConcept s = (ObservableConcept) super.clone();
		s.observers = new ArrayList<>();
		s.observers.addAll(this.observers);
		return s;
	}
}
