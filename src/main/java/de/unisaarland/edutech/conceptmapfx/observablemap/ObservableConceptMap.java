package de.unisaarland.edutech.conceptmapfx.observablemap;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.LinkFactory;
import javafx.beans.value.ChangeListener;

public class ObservableConceptMap extends ConceptMap implements Cloneable, Observable {

	private List<ConceptMapObserver> observers = new ArrayList<>();;

	public ObservableConceptMap(Experiment experiment, int conceptCapacity, LinkFactory factory,
			ChangeListener<ConceptMap> c) {
		super(experiment, conceptCapacity, factory);
	}

	public ObservableConceptMap(Experiment experiment, LinkFactory factory) {
		super(experiment, factory);

	}

	public ObservableConceptMap(Experiment experiment) {
		super(experiment);
	}

	public void addListener(ConceptMapObserver c) {
		this.observers.add(c);
	}

	@Override
	public int addConcept(Concept c) {

		observers.forEach((l) -> l.beforeChange());
		int res = super.addConcept(c);
		observers.forEach((l) -> l.afterChange());
		return res;
	}

	@Override
	public Link addDirectedLink(Concept a, Concept b) {
		observers.forEach((l) -> l.beforeChange());
		Link res = super.addDirectedLink(a, b);
		observers.forEach((l) -> l.afterChange());
		return res;
	}

	@Override
	public Link addDirectedLink(int firstConceptIndex, int secondConceptIndex) {
		observers.forEach((l) -> l.beforeChange());
		Link res = super.addDirectedLink(firstConceptIndex, secondConceptIndex);
		observers.forEach((l) -> l.afterChange());
		return res;
	}

	@Override
	public Link addUndirectedLink(Concept a, Concept b) {
		observers.forEach((l) -> l.beforeChange());
		Link res = super.addUndirectedLink(a, b);
		observers.forEach((l) -> l.afterChange());
		return res;
	}

	@Override
	public Link addUndirectedLink(int a, int b) {
		observers.forEach((l) -> l.beforeChange());
		Link res = super.addUndirectedLink(a, b);
		observers.forEach((l) -> l.afterChange());
		return res;
	}

	@Override
	public void removeConcept(Concept c) {
		observers.forEach((l) -> l.beforeChange());
		super.removeConcept(c);
		observers.forEach((l) -> l.afterChange());
	}

	@Override
	public void removeDirectedLink(Concept a, Concept b) {
		observers.forEach((l) -> l.beforeChange());
		super.removeDirectedLink(a, b);
		observers.forEach((l) -> l.afterChange());
	}

	@Override
	public void removeUndirectedLink(Concept a, Concept b) {
		observers.forEach((l) -> l.beforeChange());
		super.removeUndirectedLink(a, b);
		observers.forEach((l) -> l.afterChange());
	}

	@Override
	public void setDirectedRelationToUndirected(Concept start, Concept end) {
		observers.forEach((l) -> l.beforeChange());
		super.setDirectedRelationToUndirected(start, end);
		observers.forEach((l) -> l.afterChange());
	}

	@Override
	public void clear() {
		observers.forEach((l) -> l.beforeChange());
		super.clear();
		observers.forEach((l) -> l.afterChange());
	}

	@Override
	public ObservableConceptMap clone() {
		ObservableConceptMap s = (ObservableConceptMap) super.clone();
		s.observers = new ArrayList<>();
		s.observers.addAll(this.observers);
		return s;
	}
}
