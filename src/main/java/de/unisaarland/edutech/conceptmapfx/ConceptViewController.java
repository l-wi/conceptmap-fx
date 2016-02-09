package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ConceptViewController implements ConceptMovingListener, InputClosedListener, UserToggleEnabledListener {

	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewController.class);

	private List<ConceptEditRequestedListener> conceptEditListeners = new ArrayList<ConceptEditRequestedListener>();
	private List<ConceptMovingListener> conceptMovingListeners = new ArrayList<ConceptMovingListener>();
	private List<ConceptMovedListener> conceptMovedListeners = new ArrayList<ConceptMovedListener>();

	@FXML
	private Pane conceptPane;
	@FXML
	private Label txtConcept;
	// @FXML
	// private VBox vboxTools;
	@FXML
	private ToggleButton btnExpand;
	@FXML
	private ToggleButton btnToogleUser1;
	@FXML
	private ToggleButton btnToogleUser2;
	@FXML
	private ToggleButton btnToogleUser3;
	@FXML
	private ToggleButton btnToogleUser4;

	@FXML
	private ToggleGroup toggleGroup;

	private Concept concept;
	private List<User> participants;

	private double dragX;

	private double dragY;

	private Editable editable;

	private InputToggleGroup inputToggleGroup;

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

	private void constructResizableTextfield(Label txt) {
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
//				txt.positionCaret(txt.getCaretPosition()); // If you remove this
															// line, it flashes
															// a little bit
			});
		});
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
		conceptEditListeners.forEach(l -> l.conceptEditRequested(this, this.editable, u));
	}

	public Bounds getBoundsInScene() {
		return conceptPane.getLocalToSceneTransform().transform(conceptPane.getBoundsInLocal());
	}

	public Point2D getCenterAsSceneCoordinates() {
		Point2D p = new Point2D(conceptPane.getWidth() / 2, conceptPane.getHeight() / 2);
		return conceptPane.getLocalToSceneTransform().transform(p);
	}

	public Concept getConcept() {
		return concept;
	}

	public double getHeight() {
		return conceptPane.getHeight();
	}

	public Point2D getOrigin() {
		double x = this.conceptPane.getLayoutX() + conceptPane.getTranslateX();
		double y = this.conceptPane.getLayoutY() + conceptPane.getTranslateY();
		return new Point2D(x, y);

	}

	public double getRotate() {
		return conceptPane.getRotate();
	}

	public double getWidth() {
		return conceptPane.getWidth();
	}

	public ReadOnlyDoubleProperty heightProperty() {
		return this.conceptPane.heightProperty();
	}

	public void highlightEmpty() {
		FadeTransition ft = new FadeTransition(Duration.millis(300), conceptPane);
		ft.setFromValue(1.0);
		ft.setToValue(0.5);
		ft.setAutoReverse(true);
		ft.setCycleCount(2);
		ft.play();

	}

	@FXML
	public void initialize() {

		this.addConceptMovingListener(this);

		this.inputToggleGroup = new InputToggleGroup(this, btnToogleUser1, btnToogleUser2, btnToogleUser3,
				btnToogleUser4);

		constructResizableTextfield(txtConcept);

//		txtConcept.caretPositionProperty().addListener((c, o, n) -> {
//			this.editable.setCaretPosition(n.intValue());
//
//		});

	}

	public void inputClosed(User u) {
		inputToggleGroup.setUserEnabled(participants.indexOf(u), false);
	}

	public boolean intersects(ConceptViewController other) {
		Bounds myParentBounds = this.conceptPane.getBoundsInParent();
		Bounds otherParentBounds = other.conceptPane.getBoundsInParent();

		return myParentBounds.intersects(otherParentBounds);
	}

	private void movingStarted(double x, double y) {
		this.dragX = x;
		this.dragY = y;
	}

	@FXML
	public void onMouseMoved(MouseEvent evt) {
		this.fireConceptMoved();
	}

	@FXML
	public void onMouseMoving(MouseEvent evt) {
		this.fireConceptMoving(evt.getX() - dragX, evt.getY() - dragY, conceptPane.getRotate(), this, null);
	}

	@FXML
	public void onMouseMovingStarted(MouseEvent evt) {
		movingStarted(evt.getX(), evt.getY());
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
	public void onTouchMoved(TouchEvent evt) {
		this.fireConceptMoved();
		evt.consume();
	}

	@FXML
	public void onTouchMoving(TouchEvent evt) {
		TouchPoint p = evt.getTouchPoint();
		this.fireConceptMoving(p.getX() - dragX, p.getY() - dragY, conceptPane.getRotate(), this, null);
		evt.consume();
	}

	@FXML
	public void onTouchMovingStarted(TouchEvent evt) {
		movingStarted(evt.getTouchPoint().getX(), evt.getTouchPoint().getY());
		evt.consume();
	}

	@FXML
	public void onTxtMousePressed(MouseEvent evt) {
		txtPressed(evt.getX(), evt.getY());
	}

	@FXML
	public void onTxtMouseReleased(MouseEvent evt) {
		onTxtReleased();

	}

	private void onTxtReleased() {
		showTools(false);
		this.fireConceptMoved();

	}

	@FXML
	public void onTxtTouchPressed(TouchEvent evt) {
		txtPressed(evt.getTouchPoint().getX(), evt.getTouchPoint().getY());
	}

	@FXML
	public void onTxtTouchReleased(TouchEvent evt) {
		onTxtReleased();
	}

	public void rotate(double d) {
		conceptPane.setRotate(d);
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
		this.editable = new Editable(concept.getName(), txtConcept);

	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;
	}

	public void setRotate(double rotate) {
		this.conceptPane.setRotate(rotate);
	}

	public void setUserEnabled(User owner, boolean b) {
		inputToggleGroup.setUserEnabled(participants.indexOf(owner), b);
	}

	private void showTools(boolean b) {

		Set<Node> lookupAll = conceptPane.lookupAll(".pBtn");
		for (Node n : lookupAll) {
			n.setVisible(b);
			n.setManaged(b);
			n.setDisable(!b);
		}

		if (b)
			translateRelative(-btnToogleUser1.getWidth(), -btnToogleUser1.getHeight());
		else
			translateRelative(btnToogleUser1.getWidth(), btnToogleUser1.getHeight());

	}

	public void translate(double x, double y) {
		double rotation = Math.toRadians(conceptPane.getRotate());

		// if we have a rotation we have to convert to the rotated coordinate
		// system
		double xRotated = x * Math.cos(rotation) - y * Math.sin(rotation);
		double yRotated = x * Math.sin(rotation) + y * Math.cos(rotation);
		translateRelative(xRotated, yRotated);
	}

	public void translateAbsolute(double x, double y) {
		conceptPane.setTranslateX(x);
		conceptPane.setTranslateY(y);
	}

	private void translateRelative(double xRotated, double yRotated) {
		translateAbsolute(conceptPane.getTranslateX() + xRotated, conceptPane.getTranslateY() + yRotated);
	}

	private void txtPressed(double x, double y) {
		showTools(true);
		movingStarted(x, y);
	}

	public void userToggleEnabled(int buttonID) {
		this.txtConcept.setDisable(false);
		this.fireEditRequested(participants.get(buttonID));
	}

	public ReadOnlyDoubleProperty widthProperty() {
		return this.conceptPane.widthProperty();
	}
}
