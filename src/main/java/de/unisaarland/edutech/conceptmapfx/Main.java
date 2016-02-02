package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;

import de.unisaarland.edutech.conceptmapfx.InputViewController.Position;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.FocusQuestion;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

	private ConceptMapViewController conceptMapController;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		// setup dummy data
		User u1 = new User("Ben", "ben@localhost.com");
		User u2 = new User("Han", "han@localhost.com");
		User u3 = new User("Chewi", "chewi@localhost.com");
		User u4 = new User("Lea", "lea@localhost.com");

		FocusQuestion question = new FocusQuestion("Who is Ray?", u3);
		Experiment experiment = new Experiment(u4, question);

		experiment.addParticipant(u1);
		experiment.addParticipant(u2);
		experiment.addParticipant(u3);
		experiment.addParticipant(u4);
		
		ConceptMap conceptMap = new ConceptMap(experiment);

		// Begin UI code

		FXMLLoader conceptMapLoader = new FXMLLoader(getClass().getResource("ConceptMapView.fxml"));
		Pane conceptMapView = conceptMapLoader.load();
		conceptMapController = conceptMapLoader.getController();
		conceptMapController.setConceptMap(conceptMap);

		Scene scene = new Scene(conceptMapView);

		setInputPositions(scene, u1, u2, u3, u4, conceptMapView);

		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Concept Mapping");

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void setInputPositions(Scene primaryStage, User u1, User u2, User u3, User u4, Pane conceptMapView)
			throws IOException {
		// north
		final Pane v1 = addInputComponent(Position.NORTH,conceptMapView, u1);
		v1.setRotate(180);
		primaryStage.widthProperty().addListener((observeable, oldVal, newVal) -> {
			v1.setLayoutX(newVal.doubleValue() * 0.6 - v1.getWidth() / 2);
		});

		// west
		final Pane v2 = addInputComponent(Position.WEST,conceptMapView, u2);
		v2.setRotate(90);

		// as we rotate around center, we need to readjust on screen
		primaryStage.widthProperty().addListener(c -> {
			v2.setTranslateX(-(v2.getWidth() / 2 - v2.getHeight() / 2));
		});

		primaryStage.heightProperty().addListener((observeable, oldVal, newVal) -> {
			v2.setLayoutY(newVal.doubleValue() * 0.5);
		});

		// south
		final Pane v3 = addInputComponent(Position.SOUTH, conceptMapView, u3);
		primaryStage.widthProperty().addListener((observeable, oldVal, newVal) -> {
			v3.setLayoutX(newVal.doubleValue() * 0.5 - (v3.getWidth() / 2));
		});

		primaryStage.heightProperty().addListener((observeable, oldVal, newVal) -> {
			v3.setLayoutY(newVal.doubleValue() - v3.getHeight());
		});

		// east
		final Pane v4 = addInputComponent(Position.EAST, conceptMapView, u4);
		v4.setRotate(270);
		// as we rotate around center, we need to readjust on screen
		primaryStage.heightProperty().addListener(c -> {
			v4.setTranslateX((v4.getWidth() / 2 - v4.getHeight() / 2));
		});
		primaryStage.widthProperty().addListener((observeable, oldVal, newVal) -> {
			v4.setLayoutX(newVal.doubleValue() - v4.getWidth());

		});

		primaryStage.heightProperty().addListener((observeable, oldVal, newVal) -> {
			v4.setLayoutY(newVal.doubleValue() * 0.5);
		});

	}

	private Pane addInputComponent(InputViewController.Position p, Pane conceptMapView, User u) throws IOException {
		FXMLLoader inputLoader = new FXMLLoader(getClass().getResource("InputView.fxml"));
		Pane inputView = inputLoader.load();
		InputViewController inputController = inputLoader.getController();
		inputController.setUser(u);
		inputController.setPosition(p);
		conceptMapView.getChildren().add(inputView);
		inputController.addNewConceptListener(conceptMapController);
		conceptMapController.addInputViewController(inputController);
		return inputView;
	}
}
