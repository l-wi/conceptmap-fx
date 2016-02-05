package de.unisaarland.edutech.conceptmapfx;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.scene.control.TextField;

public class Editable {

	private TextField textField;
	private CollaborativeString collaborativeString;
	private int caretPosition;

	public Editable(CollaborativeString string, TextField textField) {
		this.textField = textField;
		this.collaborativeString = string;
	}

	public void remove(int index) {
		if (index >= 0) {
			collaborativeString.remove(index, 1);
			textField.deletePreviousChar();
		}
	}

	// TODO write component for multifocus carets
	public void requestTextFieldFocus() {
		if (textField.isFocused())
			textField.requestFocus();
	}

	public void adjustCaret() {
		caretPosition = textField.getText().length();
		this.textField.positionCaret(caretPosition);
	}

	public void insert(User u, int index, char c) {
		String str = String.valueOf(c);
		collaborativeString.insert(u, index, str);
		textField.insertText(index, str);
	}

	public int getCaretPosition() {
		return caretPosition;
	}

	public void setCaretPosition(int pos) {
		this.caretPosition = pos;
	}

}
