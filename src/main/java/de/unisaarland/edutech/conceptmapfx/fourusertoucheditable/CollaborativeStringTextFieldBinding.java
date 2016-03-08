package de.unisaarland.edutech.conceptmapfx.fourusertoucheditable;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.beans.property.StringProperty;

public class CollaborativeStringTextFieldBinding {

	private StringProperty caption;
	private CollaborativeString collaborativeString;

	private CollaborativeStringTextFieldBinding(CollaborativeString string, StringProperty caption) {
		this.caption = caption;
		this.collaborativeString = string;
		this.caption.set(collaborativeString.getContent());
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
		caption.set(collaborativeString.getContent());
	}

	public static CollaborativeStringTextFieldBinding createBinding(CollaborativeString source, StringProperty dest) {
		return new CollaborativeStringTextFieldBinding(source, dest);
	}

}
