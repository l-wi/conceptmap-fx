package de.unisaarland.edutech.conceptmapfx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.awt.AWTConfig;
import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewBuilder;
import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapViewBuilder;
import de.unisaarland.edutech.conceptmapfx.datalogging.InteractionLogger;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptFactory;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptMap;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableLinkFactory;
import de.unisaarland.edutech.conceptmapfx.preparation.ExperimentCreateController;
import de.unisaarland.edutech.conceptmapfx.preparation.LoginController;
import de.unisaarland.edutech.conceptmapfx.prompts.PromptLoader;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static final String FINAL_MAP_SCREENSHOT = "finalMap.png";

	public static void main(String[] args) {
		launch(args);
	}

	private SessionRestoreState restorer;
	private Experiment experiment;
	private ObservableConceptMap conceptMap;
	private PromptLoader promptLoader;

	// FIXME load colors from css to have them at a central location.
	private static final Color userTop = Color.web("#0C8CFF");
	private static final Color userLeft = Color.web("#F00054");
	private static final Color userRight = Color.web("#CCF000");
	private static final Color userBottom = Color.web("#00F09C");

	public static Map<User, Color> userColors = new HashMap<>();

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

		showOnfirstOrPrimaryDisplay(primaryStage);

		restorer = new SessionRestoreState();

		Optional<ObservableConceptMap> restoredMap = restorer.restoreSessionIfNeeded();

		if (restoredMap.isPresent()) {
			this.conceptMap = restoredMap.get();
			this.experiment = conceptMap.getExperiment();
			toConceptMapStage(primaryStage, experiment);
			primaryStage.show();
			return;
		}

		// Load examiner view

		LoginController examinerController = initExperimentLoginView();
		examinerController.addImageCSSClass("researcher");

		ExperimentCreateController experimentController = initExperimentView();

		promptLoader = new PromptLoader();

		LoginController userTopController = initUserLoginView();
		userTopController.setPrompt("insert/select top user");
		userTopController.addImageCSSClass("background-top");
		userTopController.usePrompts(promptLoader);

		LoginController userLeftController = initUserLoginView();
		userLeftController.setPrompt("insert/select left user");
		userLeftController.addImageCSSClass("background-left");
		userLeftController.usePrompts(promptLoader);

		LoginController userRightController = initUserLoginView();
		userRightController.setPrompt("insert/select right user");
		userRightController.addImageCSSClass("background-right");
		userRightController.usePrompts(promptLoader);

		LoginController userBottomController = initUserLoginView();
		userBottomController.setPrompt("insert/select bottom user");
		userBottomController.addImageCSSClass("background-bottom");
		userBottomController.usePrompts(promptLoader);

		// set links with controllers
		examinerController.setNext((u) -> {
			experimentController.setResearcher(u);
			Parent newView = experimentController.getView();
			showScene(primaryStage, newView);
		});

		experimentController.setNext((e) -> {
			this.experiment = e;

			userTopController.setNext((u) -> {
				this.experiment.addParticipant(u);

				showScene(primaryStage, userBottomController.getView());
			});

			userLeftController.setNext((u) -> {
				this.experiment.addParticipant(u);

				showScene(primaryStage, userRightController.getView());
			});

			userBottomController.setNext((u) -> {
				this.experiment.addParticipant(u);
				showScene(primaryStage, userLeftController.getView());
			});

			userRightController.setNext((u) -> {
				this.experiment.addParticipant(u);
				toConceptMapStage(primaryStage, this.experiment);

			});

			if (e.USER_COUNT == 2) {
				userBottomController.setNext((u) -> {
					this.experiment.addParticipant(u);
					toConceptMapStage(primaryStage, this.experiment);
				});
			}

			if (e.USER_COUNT == 3) {
				userLeftController.setNext((u) -> {
					this.experiment.addParticipant(u);
					toConceptMapStage(primaryStage, this.experiment);
				});

			}

			showScene(primaryStage, userTopController.getView());
		});

		primaryStage.setScene(new Scene(examinerController.getView()));
		primaryStage.setMaximized(true);
		primaryStage.show();
	}

	// TODO data export
	// TODO rotate two nodes simultaneously (Bug?)
	// TODO test the damn thing to death
	// TODO refactor the damn thing

	private void showOnfirstOrPrimaryDisplay(Stage primaryStage) {
		List<Screen> screens = Screen.getScreens();
		if (screens.size() == 2) {
			javafx.stage.Screen secondary = screens.get(1);
			primaryStage.setX(secondary.getBounds().getMinX());
			primaryStage.setY(secondary.getBounds().getMinY());
		}

	}

	private void toConceptMapStage(Stage primaryStage, Experiment experiment) {
		// setting up construction facilities

		ObservableConceptFactory conceptFactory = new ObservableConceptFactory();
		ObservableLinkFactory linkFactory = new ObservableLinkFactory();

		if (conceptMap == null)
			conceptMap = new ObservableConceptMap(experiment, linkFactory);

		InteractionLogger.getInstance().setConceptMap(conceptMap);
		initColors();

		ConceptViewBuilder conceptBuilder = new ConceptViewBuilder(conceptMap, conceptFactory);
		ConceptMapViewBuilder conceptMapViewBuilder = new ConceptMapViewBuilder();

		if (conceptMap.getExperiment().USE_AWT)
			conceptMapViewBuilder.withAWT(experiment.USER_COUNT, 150, 40, AWTConfig.getZPDLowerBound(),
					AWTConfig.getZPDHigherBound());

		conceptMapViewBuilder.withConceptViewBuilder(conceptBuilder).withConceptMap(conceptMap);

		conceptMapViewBuilder.withPrompts(promptLoader);

		if (conceptMap.getExperiment().USE_VOTING)
			conceptMapViewBuilder.withVoting();

		conceptMapViewBuilder.attachToListener(conceptMap).attachToListener(linkFactory)
				.attachToListener(conceptFactory);

		// Begin UI code
		primaryStage.setMaximized(true);
		primaryStage.setTitle("Concept Mapping");

		Scene scene = conceptMapViewBuilder.build();

		scene.setOnKeyTyped((l) -> {
			if (l.getCharacter().equals("f"))
				primaryStage.setFullScreen(true);
			if (l.getCharacter().equals(" ")) {
				showSummaryView(primaryStage);
			}

		});

		primaryStage.setScene(scene);
		primaryStage.setFullScreen(true);

		Optional<SessionSaver> sessionSaverOptional = conceptMapViewBuilder.getSessionSaver();

		if (sessionSaverOptional.isPresent()) {
			restorer.handleRestoreState(primaryStage);
		}

	}

	private void showSummaryView(Stage primaryStage) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/SummaryView.fxml"));
		tryLoadingFXMLOrThrow(loader);
		takeScreenshot(primaryStage);

		SummaryViewController controller = loader.getController();
		controller.setUserSummary(conceptMap.getExperiment(), InteractionLogger.getInstance().getStatistics());

		Scene s = new Scene(loader.getRoot());

		primaryStage.setScene(s);
		primaryStage.setFullScreen(true);
	}

	private void takeScreenshot(Stage primaryStage) {
		try {
			File finalMapPng = new File(SessionSaver.getWorkingDir(), FINAL_MAP_SCREENSHOT);

			WritableImage image = primaryStage.getScene().getRoot().snapshot(new SnapshotParameters(), null);
			BufferedImage fromFXImage = SwingFXUtils.fromFXImage(image, null);
			ImageOutputStream outputStream = new FileImageOutputStream(finalMapPng);

			ImageIO.write(fromFXImage, "png", outputStream);
			outputStream.flush();
		} catch (IOException e) {
			LOG.error("could not take screenshot", e);
		}
	}

	private void initColors() {
		Main.userColors.put(conceptMap.getExperiment().getParticipants().get(0), Main.userTop);
		Main.userColors.put(conceptMap.getExperiment().getParticipants().get(1), Main.userBottom);
		if (conceptMap.getExperiment().getParticipants().size() > 2)
			Main.userColors.put(conceptMap.getExperiment().getParticipants().get(2), Main.userLeft);
		if (conceptMap.getExperiment().getParticipants().size() > 3)
			Main.userColors.put(conceptMap.getExperiment().getParticipants().get(3), Main.userRight);
	}

	private void showScene(Stage primaryStage, Parent newView) {
		Scene oldScene = primaryStage.getScene();

		Scene s = new Scene(newView, oldScene.getWidth(), oldScene.getHeight());

		primaryStage.setScene(s);
	}

}
