package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;

import de.unisaarland.edutech.conceptmapfx.InputViewController.Position;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptContentChangeListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

public class ConceptViewBuilder {

	private ConceptMap map;

	private Pane conceptViewPane;

	private ConceptViewController controller;

	public ConceptViewBuilder(ConceptMap map) {
		try {
			this.map = map;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ConceptView.fxml"));
			this.conceptViewPane = loader.load();
			this.controller = loader.getController();
			this.controller.setParticipants(map.getExperiment().getParticipants());
		} catch (IOException e) {
			// should never happen fxml error
			throw new RuntimeException(e);
		}

	}

	public ConceptViewController buildControllerAndAddView(InputViewController positionOver, Pane conceptMapPane) {

		conceptMapPane.getChildren().add(conceptViewPane);
		conceptMapPane.applyCss();
		conceptMapPane.layout();

		moveConceptToRightPosition(positionOver, conceptViewPane);

		requestInput(controller);
		return controller;
	}

	public ConceptViewController buildControllerAndAddView(Pane conceptMapPane) {

		conceptMapPane.getChildren().add(conceptViewPane);

		requestInput(controller);
		return controller;

	}

	public ConceptViewBuilder forConcept(Concept c) {
		controller.setConcept(c);
		return this;
	}

	public ConceptViewBuilder withNewConcept(User u) {
		Concept c = initConcept(u);
		return forConcept(c);
	}

	public ConceptViewBuilder withEditRequestedListener(ConceptEditRequestedListener l) {
		controller.addConceptEditRequestedListener(l);
		return this;
	}

	public ConceptViewBuilder withMovedListener(ConceptMovedListener l) {
		controller.addConceptMovedListener(l);
		return this;
	}

	private Concept initConcept(User u) {
		Concept concept = new Concept(new CollaborativeString(u));
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

	public ConceptViewBuilder withMovingListener(ConceptMovingListener l) {
		controller.addConceptMovingListener(l);
		return this;
	}

	public ConceptViewBuilder withDeletedListener(ConceptDeletedListener l) {
		controller.addConceptDeletedListener(l);
		return this;
	}

	public ConceptViewBuilder withConceptEmptyListener(ConceptContentChangeListener usersController) {
		controller.addConceptEmptyListener(usersController);
		return this;

	}
}
