package de.unisaarland.edutech.conceptmapfx;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

public class Editable {

	private Label caption;
	private CollaborativeString collaborativeString;

	public Editable(CollaborativeString string, Label textField) {
		this.caption = textField;
		this.collaborativeString = string;
		this.caption.setText(collaborativeString.getContent());
		constructResizableTextfield(textField);
		textField.textProperty().addListener((l, o, n) -> {
			changeIfEmpty(n);
		});

		changeIfEmpty(textField.getText());
	}

	private void changeIfEmpty(String n) {
		if (n.length() == 0) {
			caption.getStyleClass().add("empty");
			caption.setMinWidth(70);
		} else
			caption.getStyleClass().remove("empty");

	}

	public void append(User u, char c) {
		collaborativeString.append(u, String.valueOf(c));
		update();
	}

	public void removeLast() {
		if (collaborativeString.length() > 0) {
			collaborativeString.removeLast(1);
			update();
		}
	}

	private void update() {
		caption.setText(collaborativeString.getContent());
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
				// txt.positionCaret(txt.getCaretPosition()); // If you remove
				// this
				// line, it flashes
				// a little bit
			});
		});
	}

}
