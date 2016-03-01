package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;
import java.util.Stack;

import de.unisaarland.edutech.conceptmapfx.observablemap.ConceptMapObserver;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptFactory;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptMap;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableLinkFactory;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.FocusQuestion;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application implements ConceptMapObserver {

	private ObservableConceptMap conceptMap;
	private Stack<ObservableConceptMap> states = new Stack<ObservableConceptMap>();
	private boolean isRestoringState;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		// setup dummy data

		ObservableConceptFactory conceptFactory = new ObservableConceptFactory();
		conceptFactory.addListener(this);

		ObservableLinkFactory linkFactory = new ObservableLinkFactory();
		linkFactory.addListener(this);

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

		conceptMap = new ObservableConceptMap(experiment, linkFactory);

		conceptMap.addListener(this);

		Concept lightsaber = conceptFactory.create(u2, "very very long Lightsaber");
		lightsaber.setPosition(0.5, 0.5, 30);
		conceptMap.addConcept(lightsaber);

		Concept loss = conceptFactory.create(u4, "Arm loss");
		loss.setPosition(0.2, 0.664, 0);
		conceptMap.addConcept(loss);

		Concept sith = conceptFactory.create(u1, "Sith");
		sith.setPosition(0.4, 0.664, 84);
		conceptMap.addConcept(sith);

		Link link = conceptMap.addUndirectedLink(lightsaber, loss);
		link.getCaption().append(u4, "causes");

		Link link2 = conceptMap.addDirectedLink(lightsaber, sith);
		link2.getCaption().append(u2, "uses");

		// Begin UI code
		primaryStage.setMaximized(true);
		primaryStage.setTitle("Concept Mapping");

		ConceptViewBuilder conceptBuilder = new ConceptViewBuilder(conceptMap, conceptFactory);

		ConceptMapViewBuilder builder = new ConceptMapViewBuilder();

		Scene scene = builder.withConceptViewBuilder(conceptBuilder).withConceptMap(conceptMap).build();

		scene.setOnKeyTyped((l) -> {

			if (l.getCharacter().equals("f"))
				primaryStage.setFullScreen(true);

			if (l.getCharacter().equals("u") && !states.isEmpty()) {
				Platform.runLater(() -> {
					isRestoringState = true;
					ConceptMapViewController controller = builder.getController();
					ObservableConceptMap undoMap = states.pop();
					controller.setConceptMap(undoMap);
					controller.layout();
					conceptMap = undoMap;
					isRestoringState = false;
				});
			}

		});

		primaryStage.setScene(scene);
		primaryStage.show();

	}

	@Override
	public void beforeChange() {
		if (!isRestoringState) {
			states.push(conceptMap.clone());
		}

	}

	@Override
	public void afterChange() {

	}
}
