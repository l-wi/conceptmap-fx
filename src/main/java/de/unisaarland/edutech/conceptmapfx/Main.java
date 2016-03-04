package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;
import java.util.Optional;

import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptFactory;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptMap;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableLinkFactory;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.FocusQuestion;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {

		SessionRestoreState restorer = new SessionRestoreState();

		ObservableConceptMap conceptMap = null;

		Optional<ObservableConceptMap> restoredMap = restorer.restoreSessionIfNeeded();

		// data from other view
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

		// setting up construction facilities

		ObservableConceptFactory conceptFactory = new ObservableConceptFactory();
		ObservableLinkFactory linkFactory = new ObservableLinkFactory();

		if (!restoredMap.isPresent())
			conceptMap = new ObservableConceptMap(experiment, linkFactory);
		else
			conceptMap = restoredMap.get();

		ConceptViewBuilder conceptBuilder = new ConceptViewBuilder(conceptMap, conceptFactory);
		ConceptMapViewBuilder conceptMapViewBuilder = new ConceptMapViewBuilder();

		conceptMapViewBuilder.withConceptViewBuilder(conceptBuilder).withConceptMap(conceptMap);

		conceptMapViewBuilder.attachToListener(conceptMap).attachToListener(linkFactory)
				.attachToListener(conceptFactory);

		if (restoredMap.isPresent())
			conceptMapViewBuilder.attachToReloadedMap();

		// creating some dummy data

		if (!restoredMap.isPresent()) {
			Concept lightsaber = conceptFactory.create(u2, "very very long	 Lightsaber");
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
		} else
			System.out.println("loading restored map!");
		// Begin UI code

		primaryStage.setMaximized(true);
		primaryStage.setTitle("Concept Mapping");

		Scene scene = conceptMapViewBuilder.build();

		scene.setOnKeyTyped((l) -> {
			if (l.getCharacter().equals("f"))
				primaryStage.setFullScreen(true);
		});

		primaryStage.setScene(scene);

		Optional<SessionSaver> sessionSaverOptional = conceptMapViewBuilder.getSessionSaver();

		if (sessionSaverOptional.isPresent()) {
			restorer.handleRestoreState(primaryStage, sessionSaverOptional.get());
		}

		primaryStage.show();

		// TODO Frontend
		// TODO Rotate Translate Group
		// TODO selected eintippen geht nicht (Bug?)
		// TODO rotate two nodes simultaneously (Bug?)
		// TODO parallel keyboard input (Bug?)
	}

}
