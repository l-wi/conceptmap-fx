package de.unisaarland.edutech.conceptmapfx.fourusertoucheditable;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.LowLevelInteractionListener.OnMovingInterface;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.LowLevelInteractionListener.VoidFunction;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class FourUserTouchEditable extends BorderPane {

	private static final int RIGHT_TOGGLE_INDEX = 3;
	private static final int BOTTOM_TOGGLE_INDEX = 1;
	private static final int LEFT_TOGGLE_INDEX = 2;
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
	private TextFlow caption;

	@FXML
	private Node captionPane;

	private Color rotationColor;

	private double rotationStrokeSize;

	private Circle rotateIndicator;

	private SimpleObjectProperty<State> state = new SimpleObjectProperty<>();
	private LowLevelInteractionListener lowLevelInteractionListener;
	private SimpleObjectProperty<SelectionChanged> selectionChangedProperty;

	private boolean underlineOnEditToggle = true;

	private Timeline underlineAnimation;
	private int userCount = 4;

	public FourUserTouchEditable() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/FourUserTouchEditable.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		this.setCache(true);
		this.setCacheShape(true);

		rotationColor = Color.RED;

		tryLoadingFXMLOrThrow(loader);

		constructRotateIndicator();
		// constructResizable(caption);

		keepAtLeastOneSelected();
		fixPositionsOnStateChange();

		initSelectionChangeProperty();

		initInteraction();

		initChangeIfEmpty();

		toUnselectedState();

		initInputHighlighting();

	}

	private void initInputHighlighting() {

		this.selectionChangedProperty.addListener((c, o, n) -> {
			this.highlightInput(n.index, n.isSelected);
		});
	}

	private void initInteraction() {
		this.lowLevelInteractionListener = new LowLevelInteractionListener(this);

		this.setOnMousePressed((evt) -> lowLevelInteractionListener.onMousePressed(evt));
		this.setOnTouchPressed((evt) -> lowLevelInteractionListener.onTouchPressed(evt));

		this.setOnMouseReleased((evt) -> lowLevelInteractionListener.onMouseReleased(evt));
		this.setOnTouchReleased(evt -> lowLevelInteractionListener.onTouchReleased(evt));

		this.setOnMouseDragged((evt) -> lowLevelInteractionListener.onMouseMoving(evt));
		this.setOnTouchMoved((evt) -> lowLevelInteractionListener.onTouchMoving(evt));

		this.setOnScroll((evt) -> lowLevelInteractionListener.onMouseRotate(evt));
		this.setOnRotate((evt) -> lowLevelInteractionListener.onTouchRotate(evt));
		this.setOnRotationFinished(evt -> lowLevelInteractionListener.onRotationFinished(evt));

	}

	private void initChangeIfEmpty() {
		caption.getChildren().addListener((ListChangeListener.Change<? extends Node> l) -> changeIfEmpty(l.getList()));
		// caption.textProperty().addListener((l, o, n) -> {
		// changeIfEmpty(n);
		// });

		changeIfEmpty(caption.getChildren());
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

	private void changeIfEmpty(ObservableList<? extends Node> observableList) {

		if (observableList.size() == 0) {

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
			if (userCount == 2)
				translateRelative(0, -height);
			else
				translateRelative(-width, -height);
		} else if (oldState == State.SELECTED) {
			if (userCount == 2)
				translateRelative(0, height);
			else
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
		setDisabledAndHidden(b, bottomToggle);

		setDisabledAndHidden(b, leftToggle);
		setDisabledAndHidden(b, rightToggle);

	}

	private void setDisabledAndHidden(boolean b, ToggleButton t) {
		t.setVisible(!b);
		t.setManaged(!b);
		t.setDisable(b);

	}

	public Bounds getOverlayBounds() {
		return this.captionPane.getLayoutBounds();
	}

	public void translateRelative(double x, double y) {
		this.setTranslateX(this.getTranslateX() + x);
		this.setTranslateY(this.getTranslateY() + y);
	}

	public void setText(String text) {
		caption.getChildren().clear();
		caption.getChildren().add(new Text(text));
		// caption.setText(text);
	}

	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (Node node : caption.getChildren()) {
			sb.append((((Text) node).getText()));
		}

		return sb.toString();
		
	}

	public ObservableList<? extends Node> textProperty(){
		return caption.getChildren();
	}
	
	public TextFlow getCaption() {
		return caption;
	}
//	public StringProperty textProperty() {
//		return caption.propertytextProperty();
//	}

	public int getSelected() {
		final int NO_SELECTION = -1;

		if (isBottomSelected())
			return BOTTOM_TOGGLE_INDEX;
		else if (isTopSelected())
			return TOP_TOGGLE_INDEX;
		else if (isLeftSelected())
			return LEFT_TOGGLE_INDEX;
		else if (isRightSelected())
			return RIGHT_TOGGLE_INDEX;

		return NO_SELECTION;
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

	public void setOnMoved(VoidFunction moved) {
		this.lowLevelInteractionListener.setOnMoved(moved);
	}

	public void setOnDoubleTapped(VoidFunction tap) {
		this.lowLevelInteractionListener.setOnDoubleTapped(tap);
	}

	public void setOnMoving(OnMovingInterface<Double, Double, Double> movingFunction) {
		this.lowLevelInteractionListener.setOnMoving(movingFunction);
	}

	public ObjectProperty<SelectionChanged> selectionChangedProperty() {
		return selectionChangedProperty;
	}

	public String getCSSClassForIndex(int index) {
		String result = "belongsTo";

		switch (index) {
		case TOP_TOGGLE_INDEX:
			result += "Top";
			break;
		case LEFT_TOGGLE_INDEX:
			result += "Left";
			break;
		case BOTTOM_TOGGLE_INDEX:
			result += "Bottom";
			break;
		case RIGHT_TOGGLE_INDEX:
			result += "Right";
			break;
		}
		return result;
	}

	private void highlightInput(int index, boolean inputEnabled) {
		final String cssClass = getCSSClassForIndex(index) + "Underline";

		Node caption = this.lookup("#caption");

		if (inputEnabled) {
			underlineOnEditToggle = true;
			removeUnderlineTimeline(cssClass, caption);
			underlineAnimation = new Timeline(new KeyFrame(Duration.millis(1000), new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					if (underlineOnEditToggle) {
						caption.getStyleClass().add(cssClass);
					} else {
						caption.getStyleClass().remove(cssClass);
					}
					underlineOnEditToggle = !underlineOnEditToggle;
				}
			}));
			underlineAnimation.setCycleCount(Timeline.INDEFINITE);
			underlineAnimation.play();
		}

		else {
			removeUnderlineTimeline(cssClass, caption);

		}
	}

	public void setUserCount(int count) {
		this.userCount = count;
		if (count == 2) {
			this.getChildren().remove(leftToggle);
			this.getChildren().remove(rightToggle);

		} else if (count == 3)
			this.getChildren().remove(rightToggle);

	}

	private void removeUnderlineTimeline(final String cssClass, Node caption) {
		if (underlineAnimation == null)
			return;
		underlineAnimation.stop();
		caption.getStyleClass().remove(cssClass);
	}
}
