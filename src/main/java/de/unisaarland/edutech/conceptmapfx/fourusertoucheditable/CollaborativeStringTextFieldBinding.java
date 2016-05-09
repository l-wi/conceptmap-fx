package de.unisaarland.edutech.conceptmapfx.fourusertoucheditable;

import de.unisaarland.edutech.conceptmapfx.Main;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class CollaborativeStringTextFieldBinding {

	
	private TextFlow caption;
	private CollaborativeString collaborativeString;

	private CollaborativeStringTextFieldBinding(CollaborativeString string, TextFlow caption) {
		this.caption = caption;
		this.collaborativeString = string;

		for (int i = 0; i < collaborativeString.length(); i++)
			update(collaborativeString.getOwner(), collaborativeString.getContent().charAt(i));
	}

	public void append(User u, char c) {
		collaborativeString.append(u, String.valueOf(c));
		update(u, c);
	}

	public void removeLast() {
		if (collaborativeString.length() > 0) {
			collaborativeString.removeLast(1);
			caption.getChildren().remove(caption.getChildren().size()-1);
		}
	}

	private void update(User u, char c) {
		Text t = new Text(String.valueOf(c));
		
		t.setFill(Main.userColors.get(u));
		t.setStrokeWidth(0.2);
		t.setStroke(javafx.scene.paint.Color.BLACK);
		caption.getChildren().add(t);
	}

	public static CollaborativeStringTextFieldBinding createBinding(CollaborativeString source, TextFlow dest) {
		return new CollaborativeStringTextFieldBinding(source, dest);
	}

}
