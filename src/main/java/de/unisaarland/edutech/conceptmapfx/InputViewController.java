package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.comtel2000.keyboard.control.KeyboardPane;
import org.comtel2000.keyboard.robot.IRobot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptContentChangeListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewConceptListener;
import de.unisaarland.edutech.conceptmapfx.observablemap.ConceptMapObserver;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class InputViewController implements ConceptEditRequestedListener, LinkEditRequestedListener,
		LinkDeletedListener, ConceptDeletedListener, ConceptContentChangeListener {

	private static final Logger LOG = LoggerFactory.getLogger(InputViewController.class);

	private User user;

	private InputClosedListener closedListener;
	private List<NewConceptListener> conceptListners = new ArrayList<NewConceptListener>();

	public enum Position {
		TOP, BOTTOM, RIGHT, LEFT
	}

	@FXML
	private KeyboardPane keyboard;
	@FXML
	private VBox inputControls;
	@FXML
	private AnchorPane inputPane;
	@FXML
	private Button btnNewConcept;
	@FXML
	private Button btnUndo;

	private Position position;

	private UserRobotHandler currentRobotHandler;

	private CollaborativeStringTextFieldBinding collaborativeStringBinding;

	private int emptyConceptCount = 0;

	private UndoHistory undoHistory;

	@FXML
	public void initialize() {
		try {
			keyboard.setKeyBoardStyle(getClass().getResource("/input.css").toString());
			keyboard.setSpaceKeyMove(false);
			keyboard.setLayerPath(new File("./keyboardLayout").toPath());
			keyboard.load();

			// remove the default handler
			IRobot defaultHandler = keyboard.getRobotHandler().get(0);
			keyboard.removeRobotHandler(defaultHandler);

			keyboard.setDisable(true);

		} catch (IOException | URISyntaxException e) {
			LOG.error("Program cannot run!", e);
			throw new RuntimeException("Program cannot run!", e);
		}
	}

	@FXML
	public void onCloseAction() {

		Set<Node> toHide = inputControls.lookupAll(".hideable");

		for (Node n : toHide) {
			FadeTransition ft = new FadeTransition(Duration.millis(300), n);
			ft.setFromValue(1.0);
			ft.setToValue(0.0);
			ft.play();
		}

		releaseInput();
	}

	@FXML
	public void onNewAction() {
		btnNewConcept.setDisable(true);
		emptyConceptCount++;
		fireNew();
	}

	public void addNewConceptListener(NewConceptListener l) {
		conceptListners.add(l);
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

	}

	@Override
	public void linkEditRequested(InputClosedListener l, CollaborativeStringTextFieldBinding cv, User u) {
		editRequested(l, cv, u);
	}

	private void acquireInput(CollaborativeStringTextFieldBinding cv, User u) {

		LOG.info("acquiring input for user:\t" + this.user);

		currentRobotHandler = new UserRobotHandler(cv, u);
		this.keyboard.addRobotHandler(currentRobotHandler);
		this.collaborativeStringBinding = cv;

		Set<Node> hiddenNodes = inputControls.lookupAll(".hideable");

		unhideInputControl(hiddenNodes);

	}

	private void unhideInputControl(Set<Node> hiddenNodes) {
		for (Node n : hiddenNodes) {
			if (n.getOpacity() == 0) {
				FadeTransition ft = new FadeTransition(Duration.millis(300), n);
				ft.setFromValue(0.0);
				ft.setToValue(1.0);
				ft.play();
			}
		}
	}

	private void releaseInput() {

		if (currentRobotHandler != null && closedListener != null) {
			LOG.info("releasing input for user:\t" + this.user);
			this.keyboard.removeRobotHandler(currentRobotHandler);
			closedListener.inputClosed(this.user);
			currentRobotHandler = null;
			closedListener = null;
			keyboard.setDisable(true);
		}

	}

	public void conceptDeleted(ConceptViewController cv, User u) {

		Concept concept = cv.getConcept();

		boolean belongsToUser = concept.getOwner().equals(this.user);
		boolean isEmpty = concept.getName().getContent().isEmpty();
		if (belongsToUser && isEmpty) {
			emptyConceptCount--;
			btnNewConcept.setDisable(emptyConceptCount > 0);
		}

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

	@Override
	public void conceptContentChanged(ConceptViewController source, String oldContent, String newContent) {
		if (newContent.isEmpty() && !oldContent.isEmpty())
			emptyConceptCount++;
		else if (!newContent.isEmpty() && oldContent.isEmpty())
			emptyConceptCount--;

		btnNewConcept.setDisable(emptyConceptCount > 0);

	}

	@FXML
	public void onUndoAction() {
		undoHistory.undo();
		if (undoHistory.isEmpty()) {
			btnUndo.setDisable(true);
		}
	}

	public void setUndoHistory(UndoHistory undo) {
		this.undoHistory = undo;
		this.undoHistory.addUndoButton(btnUndo);
	}

}
