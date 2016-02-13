package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;

import org.apache.commons.logging.Log;

import de.unisaarland.edutech.conceptmapfx.InputViewController.Position;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.FocusQuestion;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

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

		Concept lightsaber = new Concept(new CollaborativeString(u2, "Lightsaber"));
		lightsaber.setX(0.5);
		lightsaber.setY(0.5);
		lightsaber.setRotate(30);
		conceptMap.addConcept(lightsaber);

		Concept loss = new Concept(new CollaborativeString(u4, "Arm loss"));
		loss.setX(0.2);
		loss.setY(0.664);
		loss.setRotate(0);
		conceptMap.addConcept(loss);

		// Begin UI code
		primaryStage.setMaximized(true);
		primaryStage.setTitle("Concept Mapping");

		ConceptMapViewBuilder builder = new ConceptMapViewBuilder();
		Scene scene = builder.withConceptMap(conceptMap).build();

		scene.setOnKeyTyped((l) -> {

			if (l.getCharacter().equals("f"))
				primaryStage.setFullScreen(true);
		});

		primaryStage.setScene(scene);
		primaryStage.show();

	}
}
