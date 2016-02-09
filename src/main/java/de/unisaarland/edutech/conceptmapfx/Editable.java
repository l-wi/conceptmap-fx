package de.unisaarland.edutech.conceptmapfx;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.scene.control.Label;

public class Editable {

	private Label caption;
	private CollaborativeString collaborativeString;

	public Editable(CollaborativeString string, Label textField) {
		this.caption = textField;
		this.collaborativeString = string;
		this.caption.setText(collaborativeString.getContent());
	}

	public void append(User u, char c) {
		collaborativeString.append(u, String.valueOf(c));
		update();
	}

	public void removeLast() {
		collaborativeString.removeLast(1);
		update();
	}

	private void update() {
		caption.setText(collaborativeString.getContent());
	}

}
