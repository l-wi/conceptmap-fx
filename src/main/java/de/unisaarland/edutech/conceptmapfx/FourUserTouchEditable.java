package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.LowLevelInteractionListener.OnMovedInterface;
import de.unisaarland.edutech.conceptmapfx.LowLevelInteractionListener.OnMovingInterface;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class FourUserTouchEditable extends BorderPane {

	private static final int RIGHT_TOGGLE_INDEX = 3;
	private static final int BOTTOM_TOGGLE_INDEX = 2;
	private static final int LEFT_TOGGLE_INDEX = 1;
	private static final int TOP_TOGGLE_INDEX = 0;

	private static final Logger LOG = LoggerFactory.getLogger(FourUserTouchEditable.class);

	public class SelectionChanged {
		public final int index;
		public final boolean isSelected;

		public SelectionChanged(int index, boolean value) {
			this.index = index;
			this.isSelected = value;
		}
	}

	public enum State {
		UNSELECTED, SELECTED, MOVING, ROTATING
	}

	@FXML
	private ToggleGroup toggleGroup;
	@FXML
	private ToggleButton topToggle;
	@FXML
	private ToggleButton leftToggle;
	@FXML
	private ToggleButton rightToggle;
	@FXML
	private ToggleButton bottomToggle;
	@FXML
	private Label caption;

	private Color rotationColor;

	private double rotationStrokeSize;

	private Circle rotateIndicator;

	private SimpleObjectProperty<State> state = new SimpleObjectProperty<>();
	private LowLevelInteractionListener lowLevelInteractionListener;
	private SimpleObjectProperty<SelectionChanged> selectionChangedProperty;

	public FourUserTouchEditable() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("FourUserTouchEditable.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		rotationColor = Color.AQUA;

		tryLoadingFXMLOrThrow(loader);

		constructRotateIndicator();
		constructResizable(caption);

		keepAtLeastOneSelected();
		fixPositionsOnStateChange();

		initSelectionChangeProperty();

		initInteraction();

		initChangeIfEmpty();

		toUnselectedState();

	}

	private void initInteraction() {
		this.lowLevelInteractionListener = new LowLevelInteractionListener(this);

		// TODO other event binding
		this.setOnMousePressed((evt) -> lowLevelInteractionListener.onMousePressed(evt));
		this.setOnMouseReleased((evt) -> lowLevelInteractionListener.onMouseReleased(evt));
		this.setOnMouseDragged((evt) -> lowLevelInteractionListener.onMouseMoving(evt));
		this.setOnScroll((evt) -> lowLevelInteractionListener.onMouseRotate(evt));
		;
	}

	private void initChangeIfEmpty() {
		caption.textProperty().addListener((l, o, n) -> {
			changeIfEmpty(n);
		});

		changeIfEmpty(caption.getText());
	}

	private void initSelectionChangeProperty() {
		selectionChangedProperty = new SimpleObjectProperty<SelectionChanged>(new SelectionChanged(0, false));

		topToggle.selectedProperty()
				.addListener((l, o, n) -> selectionChangedProperty.set(new SelectionChanged(TOP_TOGGLE_INDEX, n)));
		leftToggle.selectedProperty()
				.addListener((l, o, n) -> selectionChangedProperty.set(new SelectionChanged(LEFT_TOGGLE_INDEX, n)));
		rightToggle.selectedProperty()
				.addListener((l, o, n) -> selectionChangedProperty.set(new SelectionChanged(RIGHT_TOGGLE_INDEX, n)));
		bottomToggle.selectedProperty()
				.addListener((l, o, n) -> selectionChangedProperty.set(new SelectionChanged(BOTTOM_TOGGLE_INDEX, n)));
	}

	private void changeIfEmpty(String n) {

		if (n.length() == 0) {

			caption.getStyleClass().add("empty");
			caption.setMinWidth(70);
		} else
			caption.getStyleClass().remove("empty");

	}

	private void constructRotateIndicator() {
		rotateIndicator = new Circle();
		rotateIndicator.setStroke(rotationColor);
		rotateIndicator.setFill(Color.TRANSPARENT);

		DoubleBinding radiusX = this.widthProperty().divide(2);
		DoubleBinding radiusY = this.heightProperty().divide(2);

		DoubleBinding x = radiusX;
		DoubleBinding y = radiusY;
		DoubleBinding r = radiusX.multiply(2);

		rotateIndicator.centerXProperty().bind(x);
		rotateIndicator.centerYProperty().bind(y);
		rotateIndicator.radiusProperty().bind(r);

		this.getChildren().add(rotateIndicator);

	}

	private void fixPositionsOnStateChange() {
		state.addListener((l, o, n) -> fixPositionOnStateChange(o, n));
	}

	private void fixPositionOnStateChange(State oldState, State newState) {

		this.applyCss();
		this.layout();

		double width = topToggle.getWidth();
		double height = topToggle.getHeight();

		if (newState == State.SELECTED) {
			translateRelative(-width, -height);
		} else if (oldState == State.SELECTED) {
			translateRelative(width, height);
		}

	}

	private void keepAtLeastOneSelected() {
		stopUnselecting(topToggle);
		stopUnselecting(leftToggle);
		stopUnselecting(rightToggle);
		stopUnselecting(bottomToggle);
	}



	private void stopUnselecting(ToggleButton b) {
		b.setOnTouchReleased((l) -> {
			if (!b.isSelected())
				b.setSelected(true);
		});
		b.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (b.isSelected()) {
					mouseEvent.consume();
				}
			}
		});
	}

	private void tryLoadingFXMLOrThrow(FXMLLoader loader) {
		try {
			loader.load();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void constructResizable(Label txt) {
		txt.setMaxWidth(Region.USE_PREF_SIZE);
		txt.textProperty().addListener((ov, prevText, currText) -> {
			// Do this in a Platform.runLater because of Textfield has no
			// padding at first time and so on
			Platform.runLater(() -> {
				Text text = new Text(currText);
				text.setFont(txt.getFont()); // Set the same font, so the size
												// is the same
				double width = text.getLayoutBounds().getWidth() // This big is
																	// the Text
																	// in the
																	// TextField
						+ txt.getPadding().getLeft() + txt.getPadding().getRight() // Add
																					// the
																					// padding
																					// of
																					// the
																					// TextField
						+ 2d; // Add some spacing
				txt.setPrefWidth(width); // Set the width
				// txt.positionCaret(txt.getCaretPosition()); // If you remove
				// this
				// line, it flashes
				// a little bit
			});
		});
	}

	public void toSelectedState() {
		if (state.get() == State.SELECTED)
			return;
		setDisableAndHiddenToToggles(false);
		setDisableAndHiddenToRotate(true);

		state.set(State.SELECTED);
	}

	public void toUnselectedState() {
		if (state.get() == State.UNSELECTED)
			return;
		setDisableAndHiddenToAll(true);
		state.set(State.UNSELECTED);
	}

	public void toRotateState() {
		if (state.get() == State.ROTATING)
			return;

		setDisableAndHiddenToToggles(true);
		setDisableAndHiddenToRotate(false);

		state.set(State.ROTATING);
	}

	public void toMovingState() {
		if (state.get() == State.MOVING)
			return;

		setDisableAndHiddenToAll(true);
		// TODO do we need to adapt for something when we are moving?
		state.set(State.MOVING);
	}

	private void setDisableAndHiddenToAll(boolean b) {
		setDisableAndHiddenToRotate(b);
		setDisableAndHiddenToToggles(b);
	}

	private void setDisableAndHiddenToRotate(boolean b) {
		rotateIndicator.setVisible(!b);
		rotateIndicator.setManaged(!b);
		rotateIndicator.setDisable(b);
	}

	private void setDisableAndHiddenToToggles(boolean b) {
		setDisabledAndHidden(b, topToggle);
		setDisabledAndHidden(b, leftToggle);
		setDisabledAndHidden(b, rightToggle);
		setDisabledAndHidden(b, bottomToggle);
	}

	private void setDisabledAndHidden(boolean b, ToggleButton t) {
		t.setVisible(!b);
		t.setManaged(!b);
		t.setDisable(b);
		
	}
	
	public Bounds getOverlayBounds(){
		return this.caption.getLayoutBounds();
	}

	public void translateRelative(double x, double y) {
		this.setTranslateX(this.getTranslateX() + x);
		this.setTranslateY(this.getTranslateY() + y);
	}

	public void setText(String text) {
		caption.setText(text);
	}

	public String getText() {
		return caption.getText();
	}

	public StringProperty textProperty() {
		return caption.textProperty();
	}

	public void setSelected(int index, boolean b) {
		switch (index) {
		case TOP_TOGGLE_INDEX:
			this.setTopSelected(b);
			break;
		case LEFT_TOGGLE_INDEX:
			this.setLeftSelected(b);
			break;
		case BOTTOM_TOGGLE_INDEX:
			this.setBottomSelected(b);
			break;
		case RIGHT_TOGGLE_INDEX:
			this.setRightSelected(b);
			break;
		}
	}

	public void setTopToggleText(String txt) {
		topToggle.setText(txt);
	}

	public String getTopToggleText() {
		return topToggle.getText();
	}

	public void setLeftToggleText(String txt) {
		leftToggle.setText(txt);
	}

	public String getLeftToggleText() {
		return leftToggle.getText();
	}

	public void setRightToggleText(String txt) {
		rightToggle.setText(txt);
	}

	public String getRightToggleText() {
		return rightToggle.getText();
	}

	public void setBottomToggleText(String txt) {
		bottomToggle.setText(txt);
	}

	public String getBottomToggleText() {
		return bottomToggle.getText();
	}

	public void setTopSelected(boolean isSelected) {
		topToggle.setSelected(isSelected);
	}

	public void setLeftSelected(boolean isSelected) {
		leftToggle.setSelected(isSelected);
	}

	public void setRightSelected(boolean isSelected) {
		rightToggle.setSelected(isSelected);
	}

	public void setBottomSelected(boolean isSelected) {
		bottomToggle.setSelected(isSelected);
	}

	public boolean isTopSelected() {
		return topToggle.isSelected();
	}

	public boolean isLeftSelected() {
		return leftToggle.isSelected();
	}

	public boolean isRightSelected() {
		return rightToggle.isSelected();
	}

	public boolean isBottomSelected() {
		return bottomToggle.isSelected();
	}

	public BooleanProperty topSelectedProperty() {
		return topToggle.selectedProperty();
	}

	public BooleanProperty leftSelectedProperty() {
		return leftToggle.selectedProperty();
	}

	public BooleanProperty rightSelectedProperty() {
		return rightToggle.selectedProperty();
	}

	public BooleanProperty bottomSelectedProperty() {
		return bottomToggle.selectedProperty();
	}

	public State getState() {
		return state.get();
	}

	public ReadOnlyObjectProperty<State> stateProperty() {
		return state;
	}

	public void setRotationColor(Color rotationColor) {
		this.rotationColor = rotationColor;
		// this.rotateIndicator.setStroke(rotationColor);
	}

	public double getRotationStrokeSize() {
		return rotationStrokeSize;
	}

	public void setRotationStrokeSize(double rotationStrokeSize) {
		this.rotationStrokeSize = rotationStrokeSize;
		rotateIndicator.setStrokeWidth(rotationStrokeSize);
	}

	public Bounds getBoundsInScene() {
		return this.getLocalToSceneTransform().transform(this.getBoundsInLocal());
	}

	public Point2D getCenterAsSceneCoordinates() {
		Point2D p = new Point2D(this.getWidth() / 2, this.getHeight() / 2);
		return this.getLocalToParentTransform().transform(p);
	}

	public Point2D getOrigin() {
		double x = getLayoutX() + getTranslateX();
		double y = getLayoutY() + getTranslateY();
		return new Point2D(x, y);

	}

	public void setOnMoved(OnMovedInterface moved) {
		this.lowLevelInteractionListener.setOnMoved(moved);
	}

	public void setOnMoving(OnMovingInterface<Double, Double, Double> movingFunction) {
		this.lowLevelInteractionListener.setOnMoving(movingFunction);
	}

	public ObjectProperty<SelectionChanged> selectionChangedProperty() {
		return selectionChangedProperty;
	}

}