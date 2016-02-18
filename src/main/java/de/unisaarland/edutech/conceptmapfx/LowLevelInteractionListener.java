package de.unisaarland.edutech.conceptmapfx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.FourUserTouchEditable.State;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.util.Duration;

public class LowLevelInteractionListener {

	private static final Logger LOG = LoggerFactory.getLogger(LowLevelInteractionListener.class);

	private double dragX;

	private double dragY;

	private boolean isPressed;

	private int touchEventsActive;

	private PauseTransition showSelectedMenuTransition;

	private long clickInTime;

	private FourUserTouchEditable fourUserTouchEditable;

	private OnMovedInterface movedFunction;

	private OnMovingInterface<Double, Double, Double> movingFunction;

	@FunctionalInterface
	public interface OnMovedInterface {
		public void apply();
	}

	@FunctionalInterface
	public interface OnMovingInterface<D1, D2, D3> {
		public void apply(D1 t, D2 u, D3 v);
	}

	public LowLevelInteractionListener(FourUserTouchEditable fourUserTouchEditable) {
		this.fourUserTouchEditable = fourUserTouchEditable;

		showSelectedMenuTransition = new PauseTransition(Duration.millis(1500));
		showSelectedMenuTransition.setOnFinished((l) -> {
			fourUserTouchEditable.toRotateState();
		});
	}

	public void setOnMoved(OnMovedInterface moved) {
		this.movedFunction = moved;
	}

	public void setOnMoving(OnMovingInterface<Double, Double, Double> movingFunction) {
		this.movingFunction = movingFunction;
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

	private void setStartCoordinates(double x, double y) {
		this.dragX = x;
		this.dragY = y;
	}

	public void onMouseMoving(MouseEvent evt) {

		this.onMoving(evt.getX(), evt.getY(), 0);
	}

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
		if (fourUserTouchEditable.getState() == State.ROTATING)
			moving(0, 0, rotation);
	}

	@FXML
	public void onTouchMoving(TouchEvent evt) {
		TouchPoint p = evt.getTouchPoint();
		onMoving(p.getX(), p.getY(), 0);
		evt.consume();
	}

	private void onMoving(double x, double y, double r) {
		if (movedFunction == null)
			return;

		showSelectedMenuTransition.stop();
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

		this.moved();

	}

	private void moved() {
		if (movedFunction != null)
			movedFunction.apply();
	}

	public void onRotateStarted() {

	}

	public void onRotateFinished() {

	}

}
