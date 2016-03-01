package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;
import java.util.List;

import de.unisaarland.edutech.conceptmapfx.InputViewController.Position;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class ConceptMapViewBuilder {

	private ConceptMapView conceptMapView;
	private ConceptMapViewController controller;
	private Scene scene;

	public ConceptMapViewBuilder() {
		try {
			FXMLLoader conceptMapLoader = new FXMLLoader(getClass().getResource("ConceptMapView.fxml"));

			conceptMapView = conceptMapLoader.load();
			controller = conceptMapLoader.getController();
			scene = new Scene(conceptMapView,800,600);
			controller.addNewLinkListener(controller);
			
			controller.sceneWidthProperty().bind(scene.widthProperty());
			controller.sceneHeightProperty().bind(scene.heightProperty());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public ConceptMapViewController getController() {
		return controller;
	}
	
	public Scene build() {
		return scene;
	}

	public ConceptMapViewBuilder withConceptMap(ConceptMap conceptMap) {
		withParticipants(conceptMap.getExperiment().getParticipants());

		controller.setConceptMap(conceptMap);
		return this;
	}

	private ConceptMapViewBuilder withParticipants(List<User> participants) {
		try {
			setInputPositions(participants.get(0), participants.get(1), participants.get(2), participants.get(3));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return this;

	}

	private void setInputPositions(User u1, User u2, User u3, User u4) throws IOException {
		// north
		final Pane v1 = addInputComponent(Position.TOP, u1);
		v1.setRotate(180);
		scene.widthProperty().addListener((observeable, oldVal, newVal) -> {
			v1.setLayoutX(newVal.doubleValue() * 0.6 - v1.getWidth() / 2);
		});

		// west
		final Pane v2 = addInputComponent(Position.LEFT, u2);
		v2.setRotate(90);

		// as we rotate around center, we need to readjust on screen
		scene.widthProperty().addListener(c -> {
			v2.setTranslateX(-(v2.getWidth() / 2 - v2.getHeight() / 2));
		});

		scene.heightProperty().addListener((observeable, oldVal, newVal) -> {
			v2.setLayoutY(newVal.doubleValue() * 0.5);
		});

		// south
		final Pane v3 = addInputComponent(Position.BOTTOM, u3);
		scene.widthProperty().addListener((observeable, oldVal, newVal) -> {
			v3.setLayoutX(newVal.doubleValue() * 0.5 - (v3.getWidth() / 2));
		});

		scene.heightProperty().addListener((observeable, oldVal, newVal) -> {
			v3.setLayoutY(newVal.doubleValue() - v3.getHeight());
		});

		// east
		final Pane v4 = addInputComponent(Position.RIGHT, u4);
		v4.setRotate(270);
		// as we rotate around center, we need to readjust on screen
		scene.heightProperty().addListener(c -> {
			v4.setTranslateX((v4.getWidth() / 2 - v4.getHeight() / 2));
		});
		scene.widthProperty().addListener((observeable, oldVal, newVal) -> {
			v4.setLayoutX(newVal.doubleValue() - v4.getWidth());

		});

		scene.heightProperty().addListener((observeable, oldVal, newVal) -> {
			v4.setLayoutY(newVal.doubleValue() * 0.5);
		});

	}

	private Pane addInputComponent(InputViewController.Position p, User u) throws IOException {
		FXMLLoader inputLoader = new FXMLLoader(Main.class.getResource("InputView.fxml"));
		Pane inputView = inputLoader.load();
		inputView.setId(p.toString());
		InputViewController inputController = inputLoader.getController();
		inputController.setUser(u);
		inputController.setPosition(p);
		conceptMapView.add(inputView);
		inputController.addNewConceptListener(controller);
		controller.addInputViewController(inputController);
		return inputView;
	}
}
