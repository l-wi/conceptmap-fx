/*******************************************************************************
 * conceptmap-fx a concept mapping prototype for research.
 * Copyright (C) Tim Steuer (master's thesis 2016)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, US
 *******************************************************************************/
package de.unisaarland.edutech.conceptmapfx.conceptmap;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import de.unisaarland.edutech.conceptmapfx.Main;
import de.unisaarland.edutech.conceptmapfx.SessionSaver;
import de.unisaarland.edutech.conceptmapfx.UndoHistory;
import de.unisaarland.edutech.conceptmapfx.awt.AwarenessBars;
import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewBuilder;
import de.unisaarland.edutech.conceptmapfx.datalogging.InteractionLogger;
import de.unisaarland.edutech.conceptmapfx.input.InputViewController;
import de.unisaarland.edutech.conceptmapfx.input.InputViewController.Position;
import de.unisaarland.edutech.conceptmapfx.observablemap.Observable;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableCollaborativeString;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConcept;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptMap;
import de.unisaarland.edutech.conceptmapfx.prompts.PromptLoader;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class ConceptMapViewBuilder {

	private ConceptMapView conceptMapView;
	private ConceptMapViewController controller;

	private Optional<SessionSaver> saver = Optional.empty();
	private Optional<UndoHistory> history = Optional.empty();

	private Scene scene;
	private ConceptMap conceptMap;
	private ConceptViewBuilder conceptViewBuilder;

	private AwarenessBars[] awarenessTools;

	private InputViewController[] inputControllers;
	private boolean isUsingVoting;

	public ConceptMapViewBuilder() {
		try {
			FXMLLoader conceptMapLoader = new FXMLLoader(getClass().getResource("/ConceptMapView.fxml"));

			conceptMapView = conceptMapLoader.load();
			controller = conceptMapLoader.getController();
			scene = new Scene(conceptMapView);

			DefaultNewLinkListener newLinkListener = new DefaultNewLinkListener(controller);

			controller.addNewLinkListener(newLinkListener);

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
		attachSpeechListenerToInputControllers();

		controller.getInputControllers().forEach((i) -> {
			i.setConceptMapView(controller.getView());
			conceptViewBuilder.withMovingListener(i);
		});

		attachToReloadedMap();
		controller.setConceptMap(conceptMap);
		if (history.isPresent())
			history.get().activate();
		if (saver.isPresent())
			saver.get().activate();

		return scene;
	}

	private void attachSpeechListenerToInputControllers() {
		controller.getInputControllers().forEach((c) -> {
			controller.getInputControllers().forEach(cA -> c.addSpeechListener(cA));

		});
	}

	public Optional<SessionSaver> getSessionSaver() {
		return saver;
	}

	public ConceptMapViewBuilder withConceptMap(ConceptMap conceptMap) {
		this.conceptMap = conceptMap;

		initHistoryAndSessionSaverIfNeeded(conceptMap);

		withParticipants(conceptMap.getExperiment().getParticipants());

		return this;
	}

	public ConceptMapViewBuilder withAWT(int count, int barWidth, int barHeight, double zpdLower, double zpdUpper) {
		awarenessTools = new AwarenessBars[count];

		for (int i = 0; i < count; i++) {
			awarenessTools[i] = new AwarenessBars(count, barWidth, barHeight, zpdLower, zpdUpper);
			awarenessTools[i].setOwnersBar(i);
			InteractionLogger.getInstance().bindAWT(awarenessTools[i]);

		}
		return this;
	}

	private void initHistoryAndSessionSaverIfNeeded(ConceptMap conceptMap) {
		if (conceptMap instanceof ObservableConceptMap) {
			UndoHistory history = new UndoHistory((ObservableConceptMap) conceptMap, controller);
			this.saver = Optional.of(new SessionSaver((ObservableConceptMap) conceptMap));
			history.addListener(this.saver.get());
			this.history = Optional.of(history);
		}
	}

	public ConceptMapViewBuilder withConceptViewBuilder(ConceptViewBuilder builder) {
		this.conceptViewBuilder = builder;
		DefaultConceptDeletedListener conceptDeletedListener = new DefaultConceptDeletedListener(controller);
		DefaultConceptMovementListener conceptMovementListener = new DefaultConceptMovementListener(controller);

		builder.withMovedListener(conceptMovementListener).withMovingListener(conceptMovementListener)
				.withDeletedListener(conceptDeletedListener);
		controller.setConceptViewBuilder(builder);

		return this;
	}

	private ConceptMapViewBuilder withParticipants(List<User> participants) {
		try {
			setInputPositions(participants);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return this;

	}

	private void setInputPositions(List<User> u) throws IOException {
		// north

		inputControllers = new InputViewController[4];

		setPositionTop(u.get(0), 0);

		setPositionBottom(u.get(1), 1);

		if (u.size() > 2)
			setPositionLeft(u.get(2), 2);

		if (u.size() > 3)
			setPositionRight(u.get(3), 3);

	}

	private void setPositionRight(User u4, int i) throws IOException {
		// east
		final Pane v4 = initInputController(Position.RIGHT, u4, i);
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

	private void setPositionBottom(User u3, int i) throws IOException {
		// south
		final Pane v3 = initInputController(Position.BOTTOM, u3, i);
		scene.widthProperty().addListener((observeable, oldVal, newVal) -> {
			v3.setLayoutX(newVal.doubleValue() * 0.5 - (v3.getWidth() / 2));
		});

		scene.heightProperty().addListener((observeable, oldVal, newVal) -> {
			v3.setLayoutY(newVal.doubleValue() - v3.getHeight());
		});
	}

	private void setPositionLeft(User u2, int i) throws IOException {
		// west
		final Pane v2 = initInputController(Position.LEFT, u2, i);
		v2.setRotate(90);

		// as we rotate around center, we need to readjust on screen
		scene.widthProperty().addListener(c -> {
			v2.setTranslateX(-(v2.getWidth() / 2 - v2.getHeight() / 2));
		});

		scene.heightProperty().addListener((observeable, oldVal, newVal) -> {
			v2.setLayoutY(newVal.doubleValue() * 0.5);
		});
	}

	private void setPositionTop(User u1, int i) throws IOException {
		final Pane v1 = initInputController(Position.TOP, u1, i);
		v1.setRotate(180);
		scene.widthProperty().addListener((observeable, oldVal, newVal) -> {
			v1.setLayoutX(newVal.doubleValue() * 0.6 - v1.getWidth() / 2);
		});
	}

	private Pane initInputController(InputViewController.Position p, User u, int i) throws IOException {
		FXMLLoader inputLoader = new FXMLLoader(Main.class.getResource("/InputView.fxml"));
		Pane inputView = inputLoader.load();
		inputView.setId(p.toString());

		InputViewController inputController = inputLoader.getController();
		inputControllers[i] = inputController;

		inputController.setUser(u);
		inputController.setPosition(p);
		inputController.setAlignListener(new DefaultAlignListener(controller));
		inputController.setFocusQuestion(conceptMap.getExperiment().getFocusQuestion().getQuestion());

		if (awarenessTools != null)
			inputController.setAWT(awarenessTools[i]);

		if (history.isPresent())
			inputController.setUndoHistory(history.get());

		conceptMapView.add(inputView);
		inputController.addNewConceptListener(new DefaultNewConceptListener(conceptViewBuilder, controller));
		controller.addInputViewController(inputController);

		conceptViewBuilder.withEditRequestedListener(inputController);
		conceptViewBuilder.withDeletedListener(inputController);
		return inputView;
	}

	public ConceptMapViewBuilder attachToListener(Observable o) {
		if (history.isPresent() && saver.isPresent()) {
			o.addListener(history.get());
			o.addListener(saver.get());
		} else
			throw new RuntimeException("No Undo / Session Saver set!");

		return this;
	}

	private ConceptMapViewBuilder attachToReloadedMap() {
		for (int i = 0; i < conceptMap.getConceptCount(); i++) {
			ObservableConcept c = (ObservableConcept) conceptMap.getConcept(i);
			c.addListener(history.get());
			c.addListener(saver.get());

			ObservableCollaborativeString name = (ObservableCollaborativeString) c.getName();

			name.addListener(history.get());
			name.addListener(saver.get());
		}

		for (int col = 0; col < conceptMap.getConceptCount(); col++) {
			for (int row = 0; row < conceptMap.getConceptCount(); row++) {
				Link link = conceptMap.getLink(col, row);
				if (link != null) {
					ObservableCollaborativeString name = (ObservableCollaborativeString) link.getCaption();

					name.addListener(history.get());
					name.addListener(saver.get());
				}
			}
		}

		return this;
	}

	public void withVoting() {
		List<User> participants = this.conceptMap.getExperiment().getParticipants();

		for (int i = 0; i < participants.size(); i++) {
			inputControllers[i].useVoting();
		}
	}

	public ConceptMapViewBuilder withPrompts(PromptLoader promptLoader) {

		List<User> participants = this.conceptMap.getExperiment().getParticipants();

		for (int i = 0; i < participants.size(); i++) {
			String promptForUser = participants.get(i).getPrompt();
			if (promptForUser != null)
				inputControllers[i].setPrompt(promptForUser);

		}

		return this;
	}

}
