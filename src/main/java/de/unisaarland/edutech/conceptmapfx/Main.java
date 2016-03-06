package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;
import java.util.Optional;

import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptFactory;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptMap;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableLinkFactory;
import de.unisaarland.edutech.conceptmapping.Experiment;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	private Experiment experiment;

	public LoginController initUserLoginView() {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserLoginView.fxml"));
		tryLoadingFXMLOrThrow(loader);
		return loader.getController();

	}

	public LoginController initExperimentLoginView() {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExarminerLoginView.fxml"));
		tryLoadingFXMLOrThrow(loader);

		return loader.getController();

	}

	public ExperimentCreateController initExperimentView() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExperimentCreateView.fxml"));
		tryLoadingFXMLOrThrow(loader);
		return loader.getController();
	}

	private void tryLoadingFXMLOrThrow(FXMLLoader loader) {
		try {
			loader.load();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void start(Stage primaryStage) throws IOException {

		// Load examiner view

		LoginController examinerController = initExperimentLoginView();
		examinerController.addImageCSSClass("researcher");

		ExperimentCreateController experimentController = initExperimentView();

		LoginController userTopController = initUserLoginView();
		userTopController.setPrompt("insert/select top user");
		userTopController.addImageCSSClass("background-top");

		LoginController userLeftController = initUserLoginView();
		userLeftController.setPrompt("insert/select left user");
		userLeftController.addImageCSSClass("background-left");

		LoginController userRightController = initUserLoginView();
		userRightController.setPrompt("insert/select right user");
		userRightController.addImageCSSClass("background-right");

		LoginController userBottomController = initUserLoginView();
		userBottomController.setPrompt("insert/select bottom user");
		userBottomController.addImageCSSClass("background-bottom");

		// set links with controllers
		examinerController.setNext((u) -> {
			experimentController.setResearcher(u);
			Parent newView = experimentController.getView();
			showScene(primaryStage, newView);
		});

		experimentController.setNext((e) -> {
			this.experiment = e;
			showScene(primaryStage, userTopController.getView());
		});

		userTopController.setNext((u) -> {
			this.experiment.addParticipant(u);
			showScene(primaryStage, userLeftController.getView());
		});

		userLeftController.setNext((u) -> {
			this.experiment.addParticipant(u);
			showScene(primaryStage, userBottomController.getView());
		});

		userBottomController.setNext((u) -> {
			this.experiment.addParticipant(u);
			showScene(primaryStage, userRightController.getView());
		});

		userRightController.setNext((u) -> {
			this.experiment.addParticipant(u);
			toConceptMapStage(primaryStage, this.experiment);

		});

		primaryStage.setScene(new Scene(examinerController.getView()));
		primaryStage.setMaximized(true);
		primaryStage.show();
	}

	// TODO Frontend ( Show Focus Question + Show Focus Question during mapping)
	// TODO highlight that it is possible to add a new focus question!
	// TODO close keyboard and undo causes somehow bug !
	// TODO test the damn thing to death
	// TODO selected eintippen geht nicht (Bug?)
	// TODO rotate two nodes simultaneously (Bug?)
	// TODO parallel keyboard input (Bug?)
	// TODO Rotate Translate Group

	private void toConceptMapStage(Stage primaryStage, Experiment experiment) {
		// setting up construction facilities

		ObservableConceptMap conceptMap = null;

		SessionRestoreState restorer = new SessionRestoreState();

		Optional<ObservableConceptMap> restoredMap = restorer.restoreSessionIfNeeded();

		ObservableConceptFactory conceptFactory = new ObservableConceptFactory();
		ObservableLinkFactory linkFactory = new ObservableLinkFactory();

		if (!restoredMap.isPresent()) {
			conceptMap = new ObservableConceptMap(experiment, linkFactory);
		} else {
			conceptMap = restoredMap.get();
			System.out.println("loading restored map!");

		}

		ConceptViewBuilder conceptBuilder = new ConceptViewBuilder(conceptMap, conceptFactory);
		ConceptMapViewBuilder conceptMapViewBuilder = new ConceptMapViewBuilder();

		conceptMapViewBuilder.withConceptViewBuilder(conceptBuilder).withConceptMap(conceptMap);

		conceptMapViewBuilder.attachToListener(conceptMap).attachToListener(linkFactory)
				.attachToListener(conceptFactory);

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

	}

	private void showScene(Stage primaryStage, Parent newView) {
		Scene oldScene = primaryStage.getScene();

		Scene s = new Scene(newView, oldScene.getWidth(), oldScene.getHeight());

		primaryStage.setScene(s);
	}

}
