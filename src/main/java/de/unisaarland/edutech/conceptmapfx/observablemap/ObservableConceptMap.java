package de.unisaarland.edutech.conceptmapfx.observablemap;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.LinkFactory;
import javafx.beans.value.ChangeListener;

public class ObservableConceptMap extends ConceptMap implements Cloneable {

	private List<ConceptMapObserver> observers  = new ArrayList<>();;

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
		ConceptMap oldMap = (ConceptMap) this.clone();
		int res = super.addConcept(c);
		observers.forEach((l) -> l.changedMap(oldMap, this));
		return res;
	}

	@Override
	public Link addDirectedLink(Concept a, Concept b) {
		ConceptMap oldMap = (ConceptMap) this.clone();
		Link res = super.addDirectedLink(a, b);
		observers.forEach((l) -> l.changedMap(oldMap, this));
		return res;
	}

	@Override
	public Link addDirectedLink(int firstConceptIndex, int secondConceptIndex) {
		ConceptMap oldMap = (ConceptMap) this.clone();
		Link res = super.addDirectedLink(firstConceptIndex, secondConceptIndex);
		observers.forEach((l) -> l.changedMap(oldMap, this));
		return res;
	}

	@Override
	public Link addUndirectedLink(Concept a, Concept b) {
		ConceptMap oldMap = (ConceptMap) this.clone();
		Link res = super.addUndirectedLink(a, b);
		observers.forEach((l) -> l.changedMap(oldMap, this));
		return res;
	}

	@Override
	public Link addUndirectedLink(int a, int b) {
		ConceptMap oldMap = (ConceptMap) this.clone();
		Link res = super.addUndirectedLink(a, b);
		observers.forEach((l) -> l.changedMap(oldMap, this));
		return res;
	}

	@Override
	public void removeConcept(Concept c) {
		ConceptMap oldMap = (ConceptMap) this.clone();
		super.removeConcept(c);
		observers.forEach((l) -> l.changedMap(oldMap, this));
	}

	@Override
	public void removeDirectedLink(Concept a, Concept b) {
		ConceptMap oldMap = (ConceptMap) this.clone();
		super.removeDirectedLink(a, b);
		observers.forEach((l) -> l.changedMap(oldMap, this));
	}

	@Override
	public void removeUndirectedLink(Concept a, Concept b) {
		ConceptMap oldMap = (ConceptMap) this.clone();

		super.removeUndirectedLink(a, b);
		observers.forEach((l) -> l.changedMap(oldMap, this));
	}

	@Override
	public void setDirectedRelationToUndirected(Concept start, Concept end) {
		ConceptMap oldMap = (ConceptMap) this.clone();
		super.setDirectedRelationToUndirected(start, end);
		observers.forEach((l) -> l.changedMap(oldMap, this));
	}

	@Override
	public void clear() {
		ConceptMap oldMap = (ConceptMap) this.clone();
		super.clear();
		observers.forEach((l) -> l.changedMap(oldMap, this));
	}

	@Override
	public ObservableConceptMap clone() {
		ObservableConceptMap s = (ObservableConceptMap) super.clone();
		s.observers = new ArrayList<>();
		s.observers.addAll(this.observers);
		return s;
	}
}
