package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewLinkListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ConceptViewController implements ConceptMovedListener, InputClosedListener {

	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewController.class);

	private List<ConceptEditRequestedListener> conceptEditListeners = new ArrayList<ConceptEditRequestedListener>();
	private List<ConceptMovedListener> conceptMovedListeners = new ArrayList<ConceptMovedListener>();
	private List<NewLinkListener> newLinkListeners = new ArrayList<NewLinkListener>();

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

	public void addConceptEditRequestedListener(ConceptEditRequestedListener l) {
		conceptEditListeners.add(l);
	}

	public void addConceptMovedListener(ConceptMovedListener l) {
		conceptMovedListeners.add(l);
	}

	public void addNewLinkListener(NewLinkListener l) {
		newLinkListeners.add(l);
	}

	public void adjustCaret() {
		caretPosition = txtConcept.getText().length();
		this.txtConcept.positionCaret(caretPosition);
	}

	public void conceptMoved(ConceptViewController cv, User u) {
		// TODO Auto-generated method stub

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

		addToggleListener(btnToogleUser1, 0);
		addEventFilterToPreventUntoggle(btnToogleUser1);
		addToggleListener(btnToogleUser2, 1);
		addEventFilterToPreventUntoggle(btnToogleUser2);
		addToggleListener(btnToogleUser3, 2);
		addEventFilterToPreventUntoggle(btnToogleUser3);
		addToggleListener(btnToogleUser4, 3);
		addEventFilterToPreventUntoggle(btnToogleUser4);

		constructResizableTextfield(txtConcept);
		toggleGroup.selectedToggleProperty().addListener((c, o, n) -> {
			if (n == null)
				this.txtConcept.setDisable(true);

		});

		txtConcept.caretPositionProperty().addListener((c, o, n) -> {
			this.caretPosition = n.intValue();

		});
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
		if (txtConcept.isFocused()) {
			txtConcept.requestFocus();

		}

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

	private void addEventFilterToPreventUntoggle(ToggleButton b) {
		b.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (b.isSelected()) {
					LOG.info("stopping untoggle event as at least one button has to be toggled!");
					mouseEvent.consume();
				}
			}
		});
	}

	private void addToggleListener(ToggleButton b, int participant) {
		b.selectedProperty().addListener((c, o, n) -> {
			if (n.booleanValue())
				enableAndFireEditRequest(participants.get(participant));

		});
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

	private void enableAndFireEditRequest(User u) {
		this.txtConcept.setDisable(false);
		this.fireEditRequested(u);
	}

	private void fireEditRequested(User u) {
		conceptEditListeners.forEach(l -> l.conceptEditRequested(this, this, u));
	}

}
