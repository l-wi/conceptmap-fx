package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	public FXMLLoader initLoginView(Stage s, Parent next, ExperimentCreateController exController) throws IOException {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExarminerLoginView.fxml"));
		loader.load();
		ExaminerLoginController controller = loader.getController();
		controller.setNext((u) -> {
			exController.setResearcher(u);
			s.setScene(new Scene(next, s.getScene().getWidth(), s.getScene().getHeight()));
		});

		return loader;

	}

	public FXMLLoader initExperimentView(Stage s, Parent next) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExperimentCreateView.fxml"));
		loader.load();
		ExperimentCreateController controller = loader.getController();
		controller.setNext((e) -> System.out.println(e));
		return loader;
	}

	@Override
	public void start(Stage primaryStage) throws IOException {

//		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipantsView.fxml"));
//		Parent p = loader.load();
////		ParticipantsController controller = loader.getController();
//		
//		
//		primaryStage.setMaximized(true);
//		primaryStage.setScene(new Scene(p));
//		primaryStage.show();
		
		FXMLLoader experimentLoader = initExperimentView(primaryStage, null);
		FXMLLoader loginLoader = initLoginView(primaryStage, experimentLoader.getRoot(),
				experimentLoader.getController());

		Scene s = new Scene(loginLoader.getRoot());
		primaryStage.setMaximized(true);
		primaryStage.setScene(s);
		primaryStage.show();

		// FXMLLoader loader = new
		// FXMLLoader(getClass().getResource("/ExarminerLoginView.fxml"));
		// Parent p = loader.load();
		// ExaminerLoginController controller = loader.getController();
		// controller.setNext((u) -> System.out.println(u));
		//

		// User u = new User("TEST", "test@localhost.de");
		//
		// FXMLLoader loader = new
		// FXMLLoader(getClass().getResource("/ExperimentCreateView.fxml"));
		// Parent p = loader.load();
		// ExperimentCreateController controller = loader.getController();
		// controller.setResearcher(u);
		// controller.setNext((e) -> System.out.println(e));
		//
		// Scene s = new Scene(p);
		// primaryStage.setMaximized(true);
		// primaryStage.setScene(s);
		// primaryStage.show();

		//
		//
		// // data from other view
		// User u1 = new User("Ben", "ben@localhost.com");
		// User u2 = new User("Han", "han@localhost.com");
		// User u3 = new User("Chewi", "chewi@localhost.com");
		// User u4 = new User("Lea", "lea@localhost.com");
		//
		// FocusQuestion question = new FocusQuestion("Who is Ray?", u3);
		// Experiment experiment = new Experiment(u4, question);
		//
		// experiment.addParticipant(u1);
		// experiment.addParticipant(u2);
		// experiment.addParticipant(u3);
		// experiment.addParticipant(u4);
		//
		// toConceptMapStage(primaryStage, experiment);
		//
		// // TODO Frontend
		// // TODO Rotate Translate Group
		// // TODO selected eintippen geht nicht (Bug?)
		// // TODO rotate two nodes simultaneously (Bug?)
		// // TODO parallel keyboard input (Bug?)
		// }
		//
		// private void toConceptMapStage(Stage primaryStage, Experiment
		// experiment) {
		// // setting up construction facilities
		//
		// ObservableConceptMap conceptMap = null;
		//
		// SessionRestoreState restorer = new SessionRestoreState();
		//
		// Optional<ObservableConceptMap> restoredMap =
		// restorer.restoreSessionIfNeeded();
		//
		// ObservableConceptFactory conceptFactory = new
		// ObservableConceptFactory();
		// ObservableLinkFactory linkFactory = new ObservableLinkFactory();
		//
		// if (!restoredMap.isPresent()){
		// conceptMap = new ObservableConceptMap(experiment, linkFactory);
		// }
		// else{
		// conceptMap = restoredMap.get();
		// System.out.println("loading restored map!");
		//
		// }
		//
		// ConceptViewBuilder conceptBuilder = new
		// ConceptViewBuilder(conceptMap, conceptFactory);
		// ConceptMapViewBuilder conceptMapViewBuilder = new
		// ConceptMapViewBuilder();
		//
		// conceptMapViewBuilder.withConceptViewBuilder(conceptBuilder).withConceptMap(conceptMap);
		//
		// conceptMapViewBuilder.attachToListener(conceptMap).attachToListener(linkFactory)
		// .attachToListener(conceptFactory);
		//
		// // Begin UI code
		// primaryStage.setMaximized(true);
		// primaryStage.setTitle("Concept Mapping");
		//
		// Scene scene = conceptMapViewBuilder.build();
		//
		// scene.setOnKeyTyped((l) -> {
		// if (l.getCharacter().equals("f"))
		// primaryStage.setFullScreen(true);
		// });
		//
		// primaryStage.setScene(scene);
		//
		// Optional<SessionSaver> sessionSaverOptional =
		// conceptMapViewBuilder.getSessionSaver();
		//
		// if (sessionSaverOptional.isPresent()) {
		// restorer.handleRestoreState(primaryStage,
		// sessionSaverOptional.get());
		// }
		//
		// primaryStage.show();
	}

}
