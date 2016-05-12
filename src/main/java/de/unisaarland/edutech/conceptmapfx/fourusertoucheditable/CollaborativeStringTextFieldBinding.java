package de.unisaarland.edutech.conceptmapfx.fourusertoucheditable;

import de.unisaarland.edutech.conceptmapfx.Main;
import de.unisaarland.edutech.conceptmapfx.datalogging.InteractionLogger;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class CollaborativeStringTextFieldBinding {

	private static final InteractionLogger INTERACTION_LOGGER = InteractionLogger.getInstance();

	private TextFlow caption;
	private CollaborativeString collaborativeString;
	private Concept c;

	private Link l;

	private CollaborativeStringTextFieldBinding(Link l, TextFlow caption) {
		this.l = l;
		init(l.getCaption(), caption);
	}

	private CollaborativeStringTextFieldBinding(Concept c, TextFlow caption) {
		this.c = c;
		init(c.getName(), caption);
	}

	private void init(CollaborativeString c, TextFlow caption) {
		this.caption = caption;
		this.collaborativeString = c;

		for (int i = 0; i < collaborativeString.length(); i++)
			update(collaborativeString.getOwner(), collaborativeString.getContent().charAt(i));
	}

	public void append(User u, char c) {
		collaborativeString.append(u, String.valueOf(c));
		update(u, c);
		logInteraction(u);

	}

	public void removeLast(User u) {
		if (collaborativeString.length() > 0) {
			collaborativeString.removeLast(1);
			caption.getChildren().remove(caption.getChildren().size() - 1);
			logInteraction(u);

		}
	}

	private void update(User u, char c) {
		Text t = new Text(String.valueOf(c));

		t.setFill(Main.userColors.get(u));
		t.setStrokeWidth(0.2);
		t.setStroke(javafx.scene.paint.Color.BLACK);
		caption.getChildren().add(t);
	}

	private void logInteraction(User u) {
		if (this.c != null)
			INTERACTION_LOGGER.contentConceptData(this.c, u);
		else
			INTERACTION_LOGGER.contentLinkData(this.l, u);
	}

	public static CollaborativeStringTextFieldBinding createBinding(Concept source, TextFlow dest) {
		return new CollaborativeStringTextFieldBinding(source, dest);
	}

	public static CollaborativeStringTextFieldBinding createBinding(Link source, TextFlow dest) {
		return new CollaborativeStringTextFieldBinding(source, dest);
	}

}
