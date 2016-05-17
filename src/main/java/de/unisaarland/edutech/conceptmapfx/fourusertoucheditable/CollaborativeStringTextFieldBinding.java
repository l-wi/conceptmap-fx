package de.unisaarland.edutech.conceptmapfx.fourusertoucheditable;

import de.unisaarland.edutech.conceptmapfx.Main;
import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
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

	private ConceptViewController controller;

	private CollaborativeStringTextFieldBinding(Link l, TextFlow caption) {
		this.l = l;

		init(l.getCaption(), caption);
	}

	private CollaborativeStringTextFieldBinding(Concept c, ConceptViewController controller) {
		this.c = c;
		this.controller = controller;
		init(c.getName(), controller.getTextFlow());
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

	public boolean isLinkEditing() {
		return this.c == null;
	}

	private void update(User u, char c) {
		Text t = new Text(String.valueOf(c));

		t.setFill(Main.userColors.get(u));
		t.setStrokeWidth(0.2);
		t.setStroke(javafx.scene.paint.Color.BLACK);
		caption.getChildren().add(t);

		if (controller != null)
			controller.adjustFontSizeToVotes();

	}

	private void logInteraction(User u) {
		if (this.c != null)
			INTERACTION_LOGGER.contentConceptData(this.c, u);
		else
			INTERACTION_LOGGER.contentLinkData(this.l, u);
	}

	public static CollaborativeStringTextFieldBinding createBinding(Concept source, ConceptViewController dest) {
		return new CollaborativeStringTextFieldBinding(source, dest);
	}

	public static CollaborativeStringTextFieldBinding createBinding(Link source, TextFlow dest) {
		return new CollaborativeStringTextFieldBinding(source, dest);
	}

	public void vote(User user, boolean hasVoted) {
		if (controller == null)
			return;
		INTERACTION_LOGGER.votingData(this.c, user);
		controller.onVote(user, hasVoted);
	}

	public boolean hasVoted(User u) {
		return (c != null) ? this.c.hasVoted(u) : false;
	}

	public Concept getConcept() {
		return c;
	}

}
