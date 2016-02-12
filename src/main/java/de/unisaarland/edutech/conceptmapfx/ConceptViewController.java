package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.FourUserTouchEditable.State;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.util.Duration;

public class ConceptViewController implements ConceptMovingListener, InputClosedListener {

	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewController.class);

	private List<ConceptEditRequestedListener> conceptEditListeners = new ArrayList<ConceptEditRequestedListener>();
	private List<ConceptMovingListener> conceptMovingListeners = new ArrayList<ConceptMovingListener>();
	private List<ConceptMovedListener> conceptMovedListeners = new ArrayList<ConceptMovedListener>();

	@FXML
	private FourUserTouchEditable fourUserTouchEditable;

	private Concept concept;

	private List<User> participants;

	private double dragX;

	private double dragY;

	private boolean isPressed;

	private int touchEventsActive;

	private CollaborativeStringTextFieldBinding colBinding;

	private PauseTransition showSelectedMenuTransition;

	private long clickInTime;

	public void addConceptEditRequestedListener(ConceptEditRequestedListener l) {
		conceptEditListeners.add(l);
	}

	public void addConceptMovedListener(ConceptMovedListener l) {
		conceptMovedListeners.add(l);
	}

	public void addConceptMovingListener(ConceptMovingListener l) {
		conceptMovingListeners.add(l);
	}

	public void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		cv.rotate(rotate);
		cv.translate(x, y);
	}

	private void fireConceptMoved() {
		conceptMovedListeners.forEach(l -> l.conceptMoved(this));
	}

	private void fireConceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {

		// TODO when to overlap here we should somehow indicate on UI that they
		// will be linked when dropped
		conceptMovingListeners.forEach(l -> l.conceptMoving(x, y, rotate, cv, u));
	}

	private void fireEditRequested(User u) {
		conceptEditListeners.forEach(l -> l.conceptEditRequested(this, this.colBinding, u));
	}

	public Bounds getBoundsInScene() {
		return fourUserTouchEditable.getLocalToSceneTransform().transform(fourUserTouchEditable.getBoundsInLocal());
	}

	public Point2D getCenterAsSceneCoordinates() {
		Point2D p = new Point2D(fourUserTouchEditable.getWidth() / 2, fourUserTouchEditable.getHeight() / 2);
		return fourUserTouchEditable.getLocalToSceneTransform().transform(p);
	}

	public Concept getConcept() {
		return concept;
	}

	public double getHeight() {
		return fourUserTouchEditable.getHeight();
	}

	public Point2D getOrigin() {
		double x = this.fourUserTouchEditable.getLayoutX() + fourUserTouchEditable.getTranslateX();
		double y = this.fourUserTouchEditable.getLayoutY() + fourUserTouchEditable.getTranslateY();
		return new Point2D(x, y);

	}

	public double getRotate() {
		return fourUserTouchEditable.getRotate();
	}

	public double getWidth() {
		return fourUserTouchEditable.getWidth();
	}

	public ReadOnlyDoubleProperty heightProperty() {
		return this.fourUserTouchEditable.heightProperty();
	}

	@FXML
	public void initialize() {

		this.addConceptMovingListener(this);

		showSelectedMenuTransition = new PauseTransition(Duration.millis(1500));
		showSelectedMenuTransition.setOnFinished((l) -> {
			fourUserTouchEditable.toRotateState();
		});
	}

	public void inputClosed(User u) {
		setUserEnabled(participants.indexOf(u), false);
	}

	public boolean intersects(ConceptViewController other) {
		Bounds myParentBounds = this.fourUserTouchEditable.getBoundsInParent();
		Bounds otherParentBounds = other.fourUserTouchEditable.getBoundsInParent();

		return myParentBounds.intersects(otherParentBounds);
	}

	private void setStartCoordinates(double x, double y) {
		this.dragX = x;
		this.dragY = y;
	}

	@FXML
	public void onMousePressed(MouseEvent evt) {
		if (!evt.isSynthesized())
			onPressed(evt.getX(), evt.getY());
	}

	@FXML
	public void onTouchPressed(TouchEvent evt) {

		onPressed(evt.getTouchPoint().getX(), evt.getTouchPoint().getY());
	}

	private void onPressed(double x, double y) {
		touchEventsActive++;
		if (touchEventsActive != 1)
			return;

		if (isPressed)
			return;

		showSelectedMenuTransition.play();

		clickInTime = System.currentTimeMillis();

		setStartCoordinates(x, y);
		isPressed = true;
	}

	@FXML
	public void onMouseMoving(MouseEvent evt) {
		this.onMoving(evt.getX(), evt.getY(), fourUserTouchEditable.getRotate());
	}

	@FXML
	public void onMouseRotate(ScrollEvent l) {
		double rotation = l.getDeltaY() / 40;
		if (l.getTouchCount() == 0)
			onRotate(rotation);
	}

	@FXML
	public void onTouchRotate(RotateEvent e) {
		onRotate(e.getAngle());
	}

	private void onRotate(double rotation) {
		this.rotate(this.getRotate() + rotation);
		fireConceptMoving(0, 0, this.getRotate() + rotation, this, null);
	}

	@FXML
	public void onTouchMoving(TouchEvent evt) {
		TouchPoint p = evt.getTouchPoint();
		onMoving(p.getX(), p.getY(), fourUserTouchEditable.getRotate());
		evt.consume();
	}

	private void onMoving(double x, double y, double r) {
		showSelectedMenuTransition.stop();
		if (fourUserTouchEditable.getState() == State.UNSELECTED)
			fourUserTouchEditable.toMovingState();
		if (fourUserTouchEditable.getState() == State.MOVING)
			this.fireConceptMoving(x - dragX, y - dragY, fourUserTouchEditable.getRotate(), this, null);
	}

	@FXML
	public void onMouseReleased(MouseEvent evt) {
		if (!evt.isSynthesized())
			onReleased();
	}

	@FXML
	public void onTouchReleased(TouchEvent evt) {
		onReleased();
	}

	private void onReleased() {

		touchEventsActive--;

		if (touchEventsActive >= 1)
			return;

		isPressed = false;

		showSelectedMenuTransition.stop();

		long delta = System.currentTimeMillis() - clickInTime;

		State state = fourUserTouchEditable.getState();

		if (delta < 1000 && state != State.MOVING && state != State.SELECTED) {
			fourUserTouchEditable.toSelectedState();
		} else
			fourUserTouchEditable.toUnselectedState();

		this.fireConceptMoved();

	}

	@FXML
	public void onRotateStarted() {
		LOG.info("starting rotate");
	}

	@FXML
	public void onRotateFinished() {

	}

	public void rotate(double d) {
		fourUserTouchEditable.setRotate(d);
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
		this.colBinding = new CollaborativeStringTextFieldBinding(concept.getName(),
				fourUserTouchEditable.textProperty());

	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;

		fourUserTouchEditable.setTopToggleText(participants.get(0).getName());
		fourUserTouchEditable.topSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(0));
		});

		fourUserTouchEditable.setLeftToggleText(participants.get(1).getName());
		fourUserTouchEditable.leftSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(1));
		});

		fourUserTouchEditable.setBottomToggleText(participants.get(2).getName());
		fourUserTouchEditable.bottomSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(2));
		});

		fourUserTouchEditable.setRightToggleText(participants.get(3).getName());
		fourUserTouchEditable.rightSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(3));
		});
	}

	public void setRotate(double rotate) {
		this.fourUserTouchEditable.setRotate(rotate);
	}

	public void setUserEnabled(User owner, boolean b) {
		int index = participants.indexOf(owner);

		setUserEnabled(index, b);

	}

	private void setUserEnabled(int index, boolean b) {
		switch (index) {
		case 0:
			fourUserTouchEditable.setTopSelected(b);
			break;
		case 1:
			fourUserTouchEditable.setLeftSelected(b);
			break;
		case 2:
			fourUserTouchEditable.setBottomSelected(b);
			break;
		case 3:
			fourUserTouchEditable.setRightSelected(b);
			break;
		}
	}

	public void translate(double x, double y) {
		double rotation = Math.toRadians(fourUserTouchEditable.getRotate());

		// if we have a rotation we have to convert to the rotated coordinate
		// system
		double xRotated = x * Math.cos(rotation) - y * Math.sin(rotation);
		double yRotated = x * Math.sin(rotation) + y * Math.cos(rotation);
		translateRelative(xRotated, yRotated);
	}

	private void translateRelative(double xRotated, double yRotated) {
		translateAbsolute(fourUserTouchEditable.getTranslateX() + xRotated,
				fourUserTouchEditable.getTranslateY() + yRotated);
	}

	public void translateAbsolute(double x, double y) {
		fourUserTouchEditable.setTranslateX(x);
		fourUserTouchEditable.setTranslateY(y);
	}

	public ReadOnlyDoubleProperty widthProperty() {
		return this.fourUserTouchEditable.widthProperty();
	}
}
