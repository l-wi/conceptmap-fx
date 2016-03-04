package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Comparator;
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

	private static final String RESTORE_FILE_NAME = "lock";

	public static void main(String[] args) {
		launch(args);
	}

	private File lockFile;

	@Override
	public void start(Stage primaryStage) throws IOException {	
		
		ObservableConceptMap conceptMap = null;

		Optional<ObservableConceptMap> restoredMap = restoreSessionIfNeeded();

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

		Optional<SessionSaver> sessionSaver = conceptMapViewBuilder.getSessionSaver();

		if (sessionSaver.isPresent()) {
			File workingDir = sessionSaver.get().getWorkingDir();
			this.lockFile = new File(workingDir, RESTORE_FILE_NAME);
			lockFile.createNewFile();
			primaryStage.setOnCloseRequest((e) -> {
				lockFile.delete();
				System.exit(0);
			});
		}
		primaryStage.show();

		// TODO Frontend
		// TODO Rotate Translate Group
		// TODO selected eintippen geht nicht (Bug?)
		// TODO rotate two nodes simultaneously (Bug?)
		// TODO parallel keyboard input (Bug?)
	}

	public Optional<ObservableConceptMap> restoreSessionIfNeeded() {
		File sessionFolder = new File("./session");

		if (!isDirectory(sessionFolder))
			sessionFolder.mkdir();

		for (File d : sessionFolder.listFiles()) {
			if (isDirectory(sessionFolder)) {
				File[] directoryContents = sortContentsNumerical(d);

				if (directoryContents.length > 0) {

					if (directoryContents[0].getName().equals(RESTORE_FILE_NAME)) {
						directoryContents[0].delete();
						File currentState = directoryContents[directoryContents.length - 1];
						try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(currentState))) {
							ObservableConceptMap cm = (ObservableConceptMap) stream.readObject();
							return Optional.of(cm);

						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		}

		return Optional.empty();
	}

	private File[] sortContentsNumerical(File d) {
		File[] directoryContents = d.listFiles();

		Comparator<File> c = (a, b) -> {

			String nameA = a.getName();
			String nameB = b.getName();

			int n1 = nameAsInt(nameA);
			int n2 = nameAsInt(nameB);

			return n1 - n2;
		};

		Arrays.sort(directoryContents, c);
		return directoryContents;
	}

	private boolean isDirectory(File r) {
		return r.exists() && r.isDirectory();
	}

	private int nameAsInt(String nameA) {

		int index = nameA.indexOf(".");
		if (index == -1)
			return -1000;
		String nameWithoutEnding = nameA.substring(0, index);

		return Integer.parseInt(nameWithoutEnding);

	}

}
