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
package de.unisaarland.edutech.conceptmapfx.input;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.comtel2000.keyboard.control.KeyButton;
import org.comtel2000.keyboard.control.KeyboardPane;
import org.comtel2000.keyboard.control.KeyboardType;
import org.comtel2000.keyboard.event.KeyButtonEvent;
import org.comtel2000.keyboard.robot.IRobot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.UndoHistory;
import de.unisaarland.edutech.conceptmapfx.awt.AwarenessBars;
import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapView;
import de.unisaarland.edutech.conceptmapfx.event.AlignListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewConceptListener;
import de.unisaarland.edutech.conceptmapfx.event.SpeechRecognitionListner;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapfx.link.LinkViewController;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class InputViewController implements ConceptEditRequestedListener, LinkEditRequestedListener,
		LinkDeletedListener, ConceptDeletedListener, SpeechRecognitionListner, ConceptMovingListener {

	private static final int FADE_OUT_TIME_CLOSE = 800;

	private static final Logger LOG = LoggerFactory.getLogger(InputViewController.class);

	private User user;

	private InputClosedListener closedListener;
	private List<NewConceptListener> conceptListners = new ArrayList<NewConceptListener>();
	private List<SpeechRecognitionListner> speechListeners = new ArrayList<SpeechRecognitionListner>();

	public enum Position {
		TOP, BOTTOM, RIGHT, LEFT
	}

	@FXML
	private AnchorPane awtPane;

	@FXML
	private KeyboardPane keyboard;
	@FXML
	private HBox inputControls;
	@FXML
	private AnchorPane inputPane;
	@FXML
	private Button btnNewConcept;
	@FXML
	private Button btnUndo;
	@FXML
	private ToggleButton btnVote;
	@FXML
	private Label owner;
	@FXML
	private Pane ownerBox;
	@FXML
	private Pane ownerPane;
	@FXML
	private Label question;
	@FXML
	private Button btnAlign;
	@FXML
	private ToggleButton btnSpeak;
	@FXML
	private Label lblPrompts;
	@FXML
	private Button btnClose;
	@FXML
	private Label lblMirroring;

	private Position position;

	private UserRobotHandler currentRobotHandler;

	private CollaborativeStringTextFieldBinding collaborativeStringBinding;

	private UndoHistory undoHistory;

	private AlignListener alignListener;

	private DefaultSpeechListener speechListener;

	private double translateX;

	private boolean isUsingVoting;

	private ConceptMapView conceptMapView;

	private Polygon highlightPolygon;

	@FXML
	public void initialize() {
		try {

			initKeyboard();
			initButtons();
			initQuestion();
			hideInput();
			initDragging();
			initMirrorLabel();

		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("Program cannot run!", e);
		}
	}

	private void initDragging() {
		ownerPane.setOnMousePressed((e) -> {
			translateX = e.getX();
		});

		ownerPane.setOnTouchPressed((e) -> {
			translateX = e.getTouchPoint().getX();
		});

		ownerPane.setOnMouseDragged((e) -> {

			if (Math.abs(e.getX() - translateX) > 30) {
				double absoluteX = inputControls.getTranslateX();
				double dX = e.getX() - translateX;
				double translation = absoluteX + dX;

				Point2D p1 = inputPane.getLocalToSceneTransform().transform(translation, 0);
				Point2D p2 = inputPane.getLocalToSceneTransform().transform(translation + inputPane.getWidth(), 0);

				double parentWidth = ((Pane) inputPane.getParent()).getWidth();
				double parentHeight = ((Pane) inputPane.getParent()).getHeight();

				if (exceedsBounds(p1, parentWidth, parentHeight) || exceedsBounds(p2, parentWidth, parentHeight))
					return;

				inputControls.setTranslateX(translation);

				computeHighlightPoints(this.collaborativeStringBinding);

			}
		});

		ownerPane.setOnTouchMoved((e) -> {
			TouchPoint touchPoint = e.getTouchPoint();
			if (Math.abs(touchPoint.getX() - translateX) > 30) {
				double absoluteX = inputControls.getTranslateX();
				double dX = touchPoint.getX() - translateX;
				double translation = absoluteX + dX;

				Point2D p1 = inputPane.getLocalToSceneTransform().transform(translation, 0);
				Point2D p2 = inputPane.getLocalToSceneTransform().transform(translation + inputPane.getWidth(), 0);

				double parentWidth = ((Pane) inputPane.getParent()).getWidth();
				double parentHeight = ((Pane) inputPane.getParent()).getHeight();

				if (exceedsBounds(p1, parentWidth, parentHeight) || exceedsBounds(p2, parentWidth, parentHeight))
					return;

				inputControls.setTranslateX(translation);
				
				computeHighlightPoints(this.collaborativeStringBinding);

			}
		});
	}

	private boolean exceedsBounds(Point2D p, double width, double height) {
		return (p.getX() < 0 || p.getX() >= width || p.getY() < 0 || p.getY() >= height);
	}

	private void initButtons() {

		// FIXME currently removing undo function because it is buggy and is
		// hard to fix
		btnUndo.setVisible(false);
		btnUndo.setManaged(false);
		this.btnVote.setVisible(isUsingVoting);

		btnAlign.setOnTouchPressed(e -> setTouchHighlightAndFire(e, btnAlign));
		btnUndo.setOnTouchPressed(e -> setTouchHighlightAndFire(e, btnUndo));
		btnNewConcept.setOnTouchPressed(e -> setTouchHighlightAndFire(e, btnNewConcept));
		btnSpeak.setOnTouchPressed(e -> setTouchHighlightAndFire(e, btnSpeak));
		btnVote.setOnTouchPressed(e -> setTouchHighlightAndFire(e, btnVote));

		btnAlign.setOnTouchReleased(e -> removeTouchHighlight(btnAlign));
		btnUndo.setOnTouchReleased(e -> removeTouchHighlight(btnUndo));
		btnNewConcept.setOnTouchReleased(e -> removeTouchHighlight(btnNewConcept));
		btnSpeak.setOnTouchReleased(e -> removeTouchHighlight(btnSpeak));
		btnVote.setOnTouchReleased(e -> removeTouchHighlight(btnVote));
	}

	private void initKeyboard() throws MalformedURLException, IOException, URISyntaxException {
		keyboard.setKeyBoardStyle(getClass().getResource("/css/input.css").toString());
		keyboard.setSpaceKeyMove(false);
		keyboard.setLayerPath(new File("./keyboardLayout").toPath());
		keyboard.load();

		/*
		 * Workaround to allow multitouch input while moving an element:
		 * 
		 */
		addTouchListenerToKeyboard();
		keyboard.setKeyboardType(KeyboardType.TEXT_SHIFT);
		addTouchListenerToKeyboard();
		keyboard.setKeyboardType(KeyboardType.TEXT);

		// remove the default handler
		IRobot defaultHandler = keyboard.getRobotHandler().get(0);
		keyboard.removeRobotHandler(defaultHandler);
		keyboard.setDisable(true);
	}

	private void initQuestion() {
		question.setWrapText(true);
		question.widthProperty().addListener((c, o, n) -> {
			double rotate = question.getRotate();
			int cycleCount = 4;
			double timeframe = 700;
			float angle = 3f;

			RotateTransition rotateTransition = new RotateTransition(Duration.millis(timeframe / cycleCount), question);

			rotateTransition.setFromAngle(-angle);
			rotateTransition.setToAngle(angle);

			rotateTransition.setCycleCount(cycleCount);
			rotateTransition.setAutoReverse(true);
			rotateTransition.setDelay(Duration.seconds(4));
			rotateTransition.setOnFinished((e) -> question.setRotate(rotate));
			rotateTransition.play();
		});

		btnNewConcept.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(btnNewConcept, Priority.ALWAYS);
	}

	private void addTouchListenerToKeyboard() {
		Set<Node> keys = keyboard.lookupAll(".key-button");

		keys.forEach(n -> {
			KeyButton key = (KeyButton) n;
			key.setOnTouchPressed(e -> {
				if (e.getTouchCount() > 1) {
					key.fireEvent(new KeyButtonEvent(KeyButtonEvent.SHORT_PRESSED));
					setTouchHighlightAndFire(e, key);
					e.consume();
				}

			});
			key.setOnTouchReleased(e -> {
				removeTouchHighlight(key);
				e.consume();
			});
		});
	}

	private void setTouchHighlightAndFire(TouchEvent e, ButtonBase b) {
		b.setStyle("-fx-background-color: #dcdcdc");

		if (e.getTouchCount() > 1) {
			b.fire();
		}
	}

	private void removeTouchHighlight(ButtonBase b) {
		PauseTransition wait = new PauseTransition(Duration.millis(300));
		wait.setOnFinished((e) -> {
			b.setStyle("");
		});
		wait.play();
	}

	@FXML
	public void onCloseAction() {
		// TODO revert back to close on button click
		FadeTransition fd1 = new FadeTransition(Duration.millis(FADE_OUT_TIME_CLOSE), btnClose);
		fd1.setToValue(0);

		FadeTransition fd2 = new FadeTransition(Duration.millis(FADE_OUT_TIME_CLOSE), keyboard);
		fd2.setToValue(0);

		ParallelTransition p = new ParallelTransition();

		p.setOnFinished(e -> {
			hideInput();
			releaseInput();
			keyboard.setOpacity(1);
			btnClose.setOpacity(1);
		});

		p.getChildren().addAll(fd1, fd2);
		p.play();

	}

	@FXML
	public void onCloseTouchPressed(TouchEvent e) {
		if (e.getTouchCount() > 1)
			onCloseAction();
	}

	private void hideInput() {
		Set<Node> toHide = inputControls.lookupAll(".hideable");

		for (Node n : toHide) {
			n.setVisible(false);
		}
	}

	@FXML
	public void onNewAction() {
		fireNew();
	}

	@FXML
	public void onNewTouchPressed(TouchEvent e) {
		if (e.getTouchCount() > 1)
			onNewAction();

	}

	public void addNewConceptListener(NewConceptListener l) {
		conceptListners.add(l);
	}

	public void addSpeechListener(SpeechRecognitionListner l) {
		speechListeners.add(l);
	}

	private void fireNew() {
		conceptListners.forEach(l -> l.newConcept(this));
	}

	private void setInputClosedListener(InputClosedListener l) {
		closedListener = l;
	}

	public void conceptEditRequested(InputClosedListener l, CollaborativeStringTextFieldBinding editable, User u) {
		editRequested(l, editable, u);
	}

	private void editRequested(InputClosedListener l, CollaborativeStringTextFieldBinding editable, User u) {
		// does somebody else want to work on our current node
		if (editable.equals(this.collaborativeStringBinding) && !u.equals(this.user))
			releaseInput();

		// it is not an edit request for us.
		if (!this.user.equals(u))
			return;

		// for us, release current and acquire new
		releaseInput();

		setInputClosedListener(l);
		acquireInput(editable, u);
		keyboard.setDisable(false);
		btnSpeak.setDisable(false);
		speechListener.setBinding(editable);

	}

	@Override
	public void linkEditRequested(InputClosedListener l, CollaborativeStringTextFieldBinding cv, User u) {
		editRequested(l, cv, u);
	}

	private void acquireInput(CollaborativeStringTextFieldBinding cv, User u) {

		currentRobotHandler = new UserRobotHandler(cv, u);
		this.keyboard.addRobotHandler(currentRobotHandler);
		this.collaborativeStringBinding = cv;

		Set<Node> hiddenNodes = inputControls.lookupAll(".hideable");

		unhideInputControl(hiddenNodes);

		boolean hasVoted = cv.hasVoted(u);
		btnVote.setSelected(hasVoted);
		btnVote.setDisable(cv.isLinkEditing());

		// FIXME currently removing undo function because it is buggy and is
		// hard to fix
		btnUndo.setVisible(false);

		// cv.setPauseTransition(realeaseTransition);

		lblMirroring.setVisible(true);
		lblMirroring.textProperty().bind(cv.textProperty());
		showHighlightingLights(cv);

	}

	private void initMirrorLabel() {
		lblMirroring.setVisible(false);
		lblMirroring.translateXProperty().bind(
				inputControls.widthProperty().divide(2).subtract(lblMirroring.widthProperty().divide(2)).subtract(50));
		lblMirroring.setTranslateY(10);
	}

	private void showHighlightingLights(CollaborativeStringTextFieldBinding cv) {
		highlightPolygon = new Polygon();
		highlightPolygon.setOpacity(0.5);
		highlightPolygon.setFill(Color.WHEAT);

		computeHighlightPoints(cv);

		conceptMapView.add(highlightPolygon);
	}

	private void computeHighlightPoints(CollaborativeStringTextFieldBinding cv) {
		if(highlightPolygon== null)
			return;
		
		TextFlow caption = cv.getCaption();
		Point2D centerOfCaption = caption.getParent().getLocalToSceneTransform().transform(caption.getWidth() / 2,
				caption.getHeight() / 2);

		int widthOfHighlight = 50;

		Point2D p3 = this.inputControls.getLocalToSceneTransform()
				.transform(this.inputControls.getWidth() / 2 - widthOfHighlight / 2, 0);
		Point2D p4 = this.inputControls.getLocalToSceneTransform()
				.transform(this.inputControls.getWidth() / 2 + widthOfHighlight / 2, 0);

		highlightPolygon.getPoints().clear();
		highlightPolygon.getPoints().addAll(centerOfCaption.getX(), centerOfCaption.getY(), centerOfCaption.getX(),
				centerOfCaption.getY(), p3.getX(), p3.getY(), p4.getX(), p4.getY());

		// highlightPolygon.setStroke(Color.YELLOW);
	}

	private void unhideInputControl(Set<Node> hiddenNodes) {
		for (Node n : hiddenNodes) {
			n.setVisible(true);
		}

		this.btnVote.setVisible(isUsingVoting && btnVote.isVisible());
	}

	private void releaseInput() {

		if (currentRobotHandler != null && closedListener != null) {
			this.keyboard.removeRobotHandler(currentRobotHandler);
			closedListener.inputClosed(this.user);
			currentRobotHandler = null;
			closedListener = null;
			keyboard.setDisable(true);
			btnSpeak.setDisable(true);
			btnVote.setDisable(true);
			btnVote.setSelected(false);
			this.conceptMapView.remove(highlightPolygon);
			highlightPolygon = null;
			lblMirroring.setVisible(false);

		}

	}

	public void setConceptMapView(ConceptMapView v) {
		this.conceptMapView = v;
	}

	public void conceptDeleted(ConceptViewController cv, User u) {

		if (u == null || !u.equals(this.user))
			return;

		releaseInput();
	}

	public void linkDeleted(LinkViewController lv, User u) {
		if (u == null || !u.equals(this.user))
			return;

		releaseInput();
	}

	public void setUser(User u) {
		this.user = u;
		owner.setText(u.getName());
		this.speechListener = new DefaultSpeechListener(u, btnSpeak, btnNewConcept);

	}

	public User getUser() {
		return user;
	}

	public Bounds getSceneBounds() {
		return inputControls.localToScene(inputControls.getBoundsInLocal());
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
		adjustNewButton();
		adjustOwnerBoxColor();
	}

	private void adjustOwnerBoxColor() {
		ObservableList<String> styleClasses = ownerBox.getStyleClass();

		((Label) ownerBox.lookup(".letter")).setText(user.getName().subSequence(0, 1).toString());

		String css = "background-";
		switch (position) {
		case BOTTOM:
			styleClasses.add(css + "bottom");
			break;
		case TOP:
			styleClasses.add(css + "top");
			break;
		case LEFT:
			styleClasses.add(css + "left");
			break;
		case RIGHT:
			styleClasses.add(css + "right");
			break;
		}
		return;
	}

	private void adjustNewButton() {
		ObservableList<String> styleClasses = btnNewConcept.getStyleClass();
		String css = "newBtn";
		switch (position) {
		case BOTTOM:
			styleClasses.add(css + "Bottom");
			break;
		case TOP:
			styleClasses.add(css + "Top");
			break;
		case LEFT:
			styleClasses.add(css + "Left");
			break;
		case RIGHT:
			styleClasses.add(css + "Right");
			break;
		}
	}

	public Point2D transformLocalToScene(Point2D pLocalToInput) {
		return inputControls.getLocalToSceneTransform().transform(pLocalToInput);
	}

	public double getRotate() {
		return inputPane.getRotate();
	}

	@FXML
	public void onUndoAction() {
		undoHistory.undo();
		if (undoHistory.isEmpty()) {
			btnUndo.setDisable(true);
		}
	}

	@FXML
	public void onAlignAction() {
		alignListener.align();
	}

	public void setAlignListener(AlignListener l) {
		this.alignListener = l;
	}

	public void setUndoHistory(UndoHistory undo) {
		this.undoHistory = undo;
		this.undoHistory.addUndoButton(btnUndo);
	}

	public void setFocusQuestion(String question) {
		this.question.setText(question);
	}

	public void setAWT(AwarenessBars awt) {
		awtPane.getChildren().add(awt);
	}

	@FXML
	public void onVoteAction() {
		collaborativeStringBinding.vote(user, btnVote.isSelected());
	}

	@FXML
	public void onSpeechAction() {
		if (btnSpeak.isSelected())
			fireSpeechRecognitionStarted();
		else
			fireSpeechRecognitionEnded();
	}

	private void fireSpeechRecognitionStarted() {
		speechListeners.forEach((l) -> l.speechRecognitionStarted(getUser()));
	}

	private void fireSpeechRecognitionEnded() {
		speechListeners.forEach((l) -> l.speechRecognitionFinished(getUser()));
	}

	@Override
	public void speechRecognitionStarted(User u) {
		speechListener.speechRecognitionStarted(u);
	}

	@Override
	public void speechRecognitionFinished(User u) {
		speechListener.speechRecognitionFinished(u);
	}

	public void setPrompt(String text) {
		this.lblPrompts.setVisible(true);
		this.lblPrompts.setManaged(true);
		this.lblPrompts.setText(text);
	}

	public void useVoting() {
		isUsingVoting = true;
		this.btnVote.setVisible(isUsingVoting);
		this.btnVote.setDisable(true);

	}

	public Button getNewButton() {
		return btnNewConcept;
	}

	public Pane getView() {
		return inputPane;
	}

	@Override
	public void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		if (highlightPolygon != null)
			computeHighlightPoints(this.collaborativeStringBinding);
	}
}
