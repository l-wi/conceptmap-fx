package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
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
	private AnchorPane conceptPane;
	@FXML
	private TextField txtConcept;
	@FXML
	private VBox vboxTools;
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

	private int caretPosition;

	private double dragX;

	private double dragY;

	public void addConceptEditRequestedListener(ConceptEditRequestedListener l) {
		conceptEditListeners.add(l);
	}

	public void addConceptMovingListener(ConceptMovingListener l) {
		conceptMovingListeners.add(l);
	}

	public void addConceptMovedListener(ConceptMovedListener l) {
		conceptMovedListeners.add(l);
	}

	public void adjustCaret() {
		caretPosition = txtConcept.getText().length();
		this.txtConcept.positionCaret(caretPosition);
	}

	public void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		cv.rotate(rotate);
		cv.translate(x, y);
	}

	public void rotate(double d) {
		conceptPane.setRotate(d);
	}

	public void translate(double x, double y) {
		double rotation = Math.toRadians(conceptPane.getRotate());

		// if we have a rotation we have to convert to the rotated coordinate
		// system
		double xRotated = x * Math.cos(rotation) - y * Math.sin(rotation);
		double yRotated = x * Math.sin(rotation) + y * Math.cos(rotation);

		conceptPane.setTranslateX(conceptPane.getTranslateX() + xRotated);
		conceptPane.setTranslateY(conceptPane.getTranslateY() + yRotated);
	}

	public int getCaretPosition() {
		return caretPosition;
	}

	public Concept getConcept() {
		return concept;
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

		new InputToggleGroup(this, btnToogleUser1, btnToogleUser2, btnToogleUser3, btnToogleUser4);

		constructResizableTextfield(txtConcept);
		toggleGroup.selectedToggleProperty().addListener((c, o, n) -> {
			if (n == null)
				this.txtConcept.setDisable(true);

		});

		txtConcept.caretPositionProperty().addListener((c, o, n) -> {
			this.caretPosition = n.intValue();

		});

		conceptPane.setOnMousePressed((evt) -> {
			this.dragX = evt.getX();
			this.dragY = evt.getY();
		});

		conceptPane.setOnMouseReleased((evt) -> {
			this.fireConceptMoved(evt);
		});
		conceptPane.setOnMouseDragged((evt) -> {
			this.fireConceptMoving(evt.getX() - dragX, evt.getY() - dragY, conceptPane.getRotate(), this, null);
		});

		conceptPane.setOnScroll(l -> {
			this.rotate(this.getRotate() + l.getDeltaY() / 40);
			fireConceptMoving(0, 0, this.getRotate() + l.getDeltaY() / 40, this, null);

		});
	}

	private void fireConceptMoved(MouseEvent evt) {
		conceptMovedListeners.forEach(l -> l.conceptMoved(this));
	}

	private void fireConceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		conceptMovingListeners.forEach(l -> l.conceptMoving(x, y, rotate, cv, u));
	}

	public void inputClosed(User u) {
		setUserEnabled(u, false);
	}

	public void insert(User u, int index, char c) {
		String str = String.valueOf(c);
		concept.getName().insert(u, index, str);
		txtConcept.insertText(index, str);

	}

	@FXML
	public void onExpand() {
		vboxTools.setManaged(btnExpand.isSelected());
		vboxTools.setVisible(btnExpand.isSelected());
	}

	public void remove(int index) {
		if (index >= 0) {
			concept.getName().remove(index, 1);
			txtConcept.deletePreviousChar();
		}
	}

	// TODO write component for multifocus carets
	public void requestTextFieldFocus() {
		if (txtConcept.isFocused())
			txtConcept.requestFocus();
	}

	public AnchorPane getConceptPane() {
		return conceptPane;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;
	}

	public void setUserEnabled(User u, boolean state) {
		int index = participants.indexOf(u);
		switch (index) {
		case 0:
			btnToogleUser1.setSelected(state);
			break;
		case 1:
			btnToogleUser2.setSelected(state);
			break;
		case 2:
			btnToogleUser3.setSelected(state);
			break;
		case 3:
			btnToogleUser4.setSelected(state);
			break;
		}
	}

	public Bounds getBoundsInScene() {
		return conceptPane.getLocalToSceneTransform().transform(conceptPane.getBoundsInLocal());
	}

	public boolean intersects(ConceptViewController other) {
		Bounds myParentBounds = this.conceptPane.getBoundsInParent();
		Bounds otherParentBounds = other.conceptPane.getBoundsInParent();

		return myParentBounds.intersects(otherParentBounds);
	}

	private void constructResizableTextfield(TextField txt) {
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
				txt.positionCaret(txt.getCaretPosition()); // If you remove this
															// line, it flashes
															// a little bit
			});
		});
	}

	public void userToggleEnabled(int buttonID) {
		this.txtConcept.setDisable(false);
		this.fireEditRequested(participants.get(buttonID));
	}

	private void fireEditRequested(User u) {
		conceptEditListeners.forEach(l -> l.conceptEditRequested(this, this, u));
	}

	public Point2D getCenterAsSceneCoordinates() {
		Point2D p = new Point2D(conceptPane.getWidth() / 2, conceptPane.getHeight() / 2);
		return conceptPane.getLocalToSceneTransform().transform(p);
	}

	public double getRotate() {
		return conceptPane.getRotate();
	}

	public double getWidth() {
		return conceptPane.getWidth();
	}

	public double getHeight() {
		return conceptPane.getHeight();
	}
}
