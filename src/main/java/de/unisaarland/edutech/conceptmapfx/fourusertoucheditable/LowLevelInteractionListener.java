package de.unisaarland.edutech.conceptmapfx.fourusertoucheditable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable.State;
import javafx.animation.Animation.Status;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class LowLevelInteractionListener {

	private static final int ROTATE_DINSTANCE_TO_NODES_CENTER = 100;

	private static final int DOUBLE_CLICK_PENDING_TIME = 200;

	private static final int ENTER_ROTATE_STATE_TIME = 1500;

	private static final int JITTER_THRESHOLD = 30;

	private static final Logger LOG = LoggerFactory.getLogger(LowLevelInteractionListener.class);

	private double dragX;

	private double dragY;

	private double rotateX;

	private double rotateY;

	private boolean isPressed;

	private int touchEventsActive;

	private PauseTransition showRotateTransition;

	private PauseTransition showSelectedTransition;

	private long clickInTime;

	private FourUserTouchEditable fourUserTouchEditable;

	private VoidFunction movedFunction;

	private OnMovingInterface<Double, Double, Double> movingFunction;

	private VoidFunction doubleTapFunction;

	@FunctionalInterface
	public interface VoidFunction {
		public void apply();
	}

	@FunctionalInterface
	public interface OnMovingInterface<D1, D2, D3> {
		public void apply(D1 t, D2 u, D3 v);
	}

	public LowLevelInteractionListener(FourUserTouchEditable fourUserTouchEditable) {
		this.fourUserTouchEditable = fourUserTouchEditable;

		showRotateTransition = new PauseTransition(Duration.millis(ENTER_ROTATE_STATE_TIME));
		showRotateTransition.setOnFinished((l) -> {
			fourUserTouchEditable.toRotateState();
		});
		
	}

	public void setOnMoved(VoidFunction moved) {
		this.movedFunction = moved;
	}

	public void setOnMoving(OnMovingInterface<Double, Double, Double> movingFunction) {
		this.movingFunction = movingFunction;
	}

	@FXML
	public void onMousePressed(MouseEvent evt) {

		if (!evt.isSynthesized())
			onPressed(evt.getX(), evt.getY(), false);
	}

	@FXML
	public void onTouchPressed(TouchEvent evt) {

		onPressed(evt.getTouchPoint().getX(), evt.getTouchPoint().getY(), true);
	}

	private void onPressed(double x, double y, boolean isTouch) {
		touchEventsActive++;
		if (touchEventsActive != 1)
			return;

		if (isPressed)
			return;
		//

		if (!isTouch)
			showRotateTransition.play();

		clickInTime = System.currentTimeMillis();

		setStartCoordinates(x, y);
		isPressed = true;
	}

	private void setStartCoordinates(double x, double y) {
		this.dragX = x;
		this.dragY = y;
	}

	public void onMouseMoving(MouseEvent evt) {
		if (evt.isSynthesized())
			return;
		this.onMoving(evt.getX(), evt.getY(), 0);
	}

	public void onMouseRotate(ScrollEvent l) {
		double rotation = l.getDeltaY() / 40;
		if (l.getTouchCount() == 0)
			onRotate(rotation);
	}

	@FXML
	public void onTouchRotate(RotateEvent e) {
		Point2D outer = new Point2D(e.getSceneX(), e.getSceneY());
		Point2D inner = fourUserTouchEditable.getCenterAsSceneCoordinates();

		double d = inner.distance(outer);
		if (d > ROTATE_DINSTANCE_TO_NODES_CENTER)
			return;

		fourUserTouchEditable.toRotateState();

		onRotate(e.getAngle());

		e.consume();
	}

	public void onRotationStarted(RotateEvent e) {

		rotateY = e.getSceneY();
		rotateX = e.getSceneX();
		e.consume();
	}

	public void onRotationFinished(RotateEvent e) {
		fourUserTouchEditable.toMovingState();
		e.consume();
	}

	private void onRotate(double rotation) {
		if (fourUserTouchEditable.getState() == State.ROTATING)
			moving(0, 0, rotation);
	}

	@FXML
	public void onTouchMoving(TouchEvent evt) {
		TouchPoint p = evt.getTouchPoint();

		double d = new Point2D(p.getX(), p.getY()).distance(new Point2D(this.dragX, this.dragY));

		if (d < JITTER_THRESHOLD)
			return;

		onMoving(p.getX(), p.getY(), 0);
		evt.consume();
	}

	private void onMoving(double x, double y, double r) {
		if (!isPressed)
			return;

		if (movedFunction == null)
			return;

		showRotateTransition.stop();

		if (showSelectedTransition != null)
			showSelectedTransition.stop();

		if (fourUserTouchEditable.getState() == State.UNSELECTED)
			fourUserTouchEditable.toMovingState();
		if (fourUserTouchEditable.getState() == State.MOVING)
			this.moving(x - dragX, y - dragY, r);
	}

	private void moving(double d, double e, double rotate) {
		if (movingFunction != null)
			movingFunction.apply(d, e, rotate);

	}

	public void onMouseReleased(MouseEvent evt) {
		if (!evt.isSynthesized())
			onReleased();
	}

	public void onTouchReleased(TouchEvent evt) {
		onReleased();
		evt.consume();
	}

	private void onReleased() {

		touchEventsActive--;

		if (touchEventsActive >= 1)
			return;

		isPressed = false;

		showRotateTransition.stop();

		long delta = System.currentTimeMillis() - clickInTime;

		State state = fourUserTouchEditable.getState();

		if (isSingleClickEventPending()) {
			cancelSingleClickEvent();
			fireDoubleClickEvent();
		} else {
			initiateSingeClickEvent(delta, state);
		}

		if (state == State.MOVING)
			this.moved();

	}

	private void fireDoubleClickEvent() {
		if (doubleTapFunction != null && (this.fourUserTouchEditable.getState() == State.UNSELECTED
				|| this.fourUserTouchEditable.getState() == State.SELECTED))
			doubleTapFunction.apply();

	}

	private void cancelSingleClickEvent() {
		showSelectedTransition.stop();
		showSelectedTransition = null;
	}

	private void initiateSingeClickEvent(long delta, State state) {

		showSelectedTransition = new PauseTransition(Duration.millis(DOUBLE_CLICK_PENDING_TIME));
		showSelectedTransition.setOnFinished((l) -> {
			if (delta < ENTER_ROTATE_STATE_TIME && state != State.MOVING && state != State.SELECTED) {
				fourUserTouchEditable.toSelectedState();
			} else
				fourUserTouchEditable.toUnselectedState();
		});

		showSelectedTransition.play();
	}

	private boolean isSingleClickEventPending() {

		return (showSelectedTransition != null) && (showSelectedTransition.getStatus() == Status.RUNNING);
	}

	private void moved() {
		fourUserTouchEditable.toUnselectedState();
		if (movedFunction != null)
			movedFunction.apply();
	}

	public void setOnDoubleTapped(VoidFunction func) {
		this.doubleTapFunction = func;
	}

}
