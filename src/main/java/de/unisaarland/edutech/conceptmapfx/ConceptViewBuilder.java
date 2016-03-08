package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapfx.InputViewController.Position;
import de.unisaarland.edutech.conceptmapfx.event.ConceptContentChangeListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptFactory;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
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

		Point2D p = new Point2D(0, -50);
		Point2D pScene = inputViewController.transformLocalToScene(p);

		Pane n = conceptViewPane;

		double x = pScene.getX();
		double y = pScene.getY();

		if (inputViewController.getPosition() == Position.TOP) {
			x = x - n.getWidth();
		} else if (inputViewController.getPosition() == Position.LEFT) {
			x = x - n.getWidth() / 2 + n.getHeight() / 2;
			y = y + n.getWidth() / 2 - n.getHeight() / 2;
		} else if (inputViewController.getPosition() == Position.RIGHT) {
			x = x - n.getWidth() / 2 - n.getHeight() / 2;
			y = y - n.getWidth() / 2 - n.getHeight() / 2;
		} else if (inputViewController.getPosition() == Position.BOTTOM) {
			y = y - n.getHeight();
		}

		conceptViewPane.setTranslateX(x);
		conceptViewPane.setTranslateY(y);

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
