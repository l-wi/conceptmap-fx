package de.unisaarland.edutech.conceptmapfx.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapView;
import de.unisaarland.edutech.conceptmapfx.event.ConceptContentChangeListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.input.InputViewController;
import de.unisaarland.edutech.conceptmapfx.input.InputViewController.Position;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptFactory;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class ConceptViewBuilder {

	private ConceptMap map;

	private Pane conceptViewPane;

	private ConceptViewController controller;

	private ConceptFactory factory;


	private List<ConceptDeletedListener> deletedListeners = new ArrayList<>();

	private List<ConceptMovingListener> movingListeners = new ArrayList<>();

	private List<ConceptMovedListener> movedListeners = new ArrayList<>();

	private List<ConceptEditRequestedListener> editListeners = new ArrayList<>();

	public ConceptViewBuilder(ConceptMap map, ConceptFactory factory) {

		reset(map, factory);

	}

	private void reset(ConceptMap map, ConceptFactory factory) {
		try {
			this.map = map;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConceptView.fxml"));
			this.conceptViewPane = loader.load();
			this.controller = loader.getController();
			this.controller.setParticipants(map.getExperiment().getParticipants());
			this.factory = factory;
		} catch (IOException e) {
			// should never happen fxml error
			throw new RuntimeException(e);
		}
	}

	public ConceptViewBuilder withMap(ConceptMap map) {
		reset(map, this.factory);
		return this;
	}

	public ConceptViewController buildControllerAndAddView(InputViewController positionOver,
			ConceptMapView conceptMapPane) {

		conceptMapPane.add(conceptViewPane);
		conceptMapPane.applyCss();
		conceptMapPane.layout();

		moveConceptToRightPosition(positionOver, conceptViewPane);

		addListenersToController();

		requestInput(controller);

		ConceptViewController result = controller;

		reset(map, factory);

		return result;
	}

	public ConceptViewController buildControllerAndAddView(ConceptMapView conceptMapPane) {

		conceptMapPane.add(conceptViewPane);

		addListenersToController();

		requestInput(controller);

		ConceptViewController result = controller;

		reset(map, factory);

		return result;

	}

	public ConceptViewBuilder forConcept(Concept c) {
		controller.setConcept(c);
		return this;
	}

	public ConceptViewBuilder withNewConcept(User u) {
		Concept c = initConcept(u);
		return forConcept(c);
	}

	private Concept initConcept(User u) {
		Concept concept = factory.create(u);
		map.addConcept(concept);
		return concept;
	}

	private void requestInput(ConceptViewController conceptViewController) {
		conceptViewController.setUserEnabled(controller.getConcept().getOwner(), true);
	}

	private void moveConceptToRightPosition(InputViewController inputViewController, Pane conceptViewPane) {

		Node n = inputViewController.getNewButton();
		
		double height = inputViewController.getView().getHeight();
		
		Point2D p = n.getLocalToSceneTransform().transform(0,-(height-80));
		
		conceptViewPane.setTranslateX(p.getX());
		conceptViewPane.setTranslateY(p.getY());
		
		conceptViewPane.setRotate(inputViewController.getRotate());
		
	}

	public void addListenersToController() {
		editListeners.forEach((l) -> controller.addConceptEditRequestedListener(l));
		movedListeners.forEach((l) -> controller.addConceptMovedListener(l));
		movingListeners.forEach((l) -> controller.addConceptMovingListener(l));
		deletedListeners.forEach((l) -> controller.addConceptDeletedListener(l));
	}

	public ConceptViewBuilder withEditRequestedListener(ConceptEditRequestedListener l) {
		editListeners.add(l);
		return this;
	}

	public ConceptViewBuilder withMovedListener(ConceptMovedListener l) {
		movedListeners.add(l);
		return this;
	}

	public ConceptViewBuilder withMovingListener(ConceptMovingListener l) {
		movingListeners.add(l);
		return this;
	}

	public ConceptViewBuilder withDeletedListener(ConceptDeletedListener l) {
		deletedListeners.add(l);
		return this;
	}

	public ConceptViewBuilder withConceptEmptyListener(ConceptContentChangeListener l) {
		controller.addConceptEmptyListener(l);
		return this;

	}
}
