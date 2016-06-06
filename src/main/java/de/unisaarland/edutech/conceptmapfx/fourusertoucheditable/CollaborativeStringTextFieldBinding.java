package de.unisaarland.edutech.conceptmapfx.fourusertoucheditable;

import de.unisaarland.edutech.conceptmapfx.Main;
import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.datalogging.InteractionLogger;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class CollaborativeStringTextFieldBinding {

	private static final InteractionLogger INTERACTION_LOGGER = InteractionLogger.getInstance();

	private TextFlow caption;
	private CollaborativeString collaborativeString;
	private Concept c;

	private Link l;

	private ConceptViewController controller;

	private StringProperty textBinding;

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

		textBinding = new SimpleStringProperty(collaborativeString.getContent());
	}

	public void append(User u, char c) {
		collaborativeString.append(u, String.valueOf(c));
		update(u, c);
		logInteraction(u, false);

		textBinding.set(collaborativeString.getContent());
	}

	public void removeLast(User u) {
		if (collaborativeString.length() > 0) {
			collaborativeString.removeLast(1);
			removeFromCaption();
			logInteraction(u, true);
			textBinding.set(collaborativeString.getContent());
		}
	}

	private void removeFromCaption() {
		ObservableList<Node> children = caption.getChildren();
		Text text = (Text) children.get(children.size() - 1);

		// case: text length > 1
		String content = text.getText();
		if (content.length() > 1)
			text.setText(content.substring(0, content.length() - 1));
		// case: text length == 1
		else
			children.remove(children.size() - 1);

	}

	public boolean isLinkEditing() {
		return this.c == null;
	}

	private void update(User u, char c) {

		ObservableList<Node> children = caption.getChildren();

		Text t = null;
		// case: no text or text of another user
		if (children.size() == 0 || !children.get(children.size() - 1).getUserData().equals(u)) {
			t = new Text(String.valueOf(c));
			t.setFill(Main.userColors.get(u));
			t.setStrokeWidth(0.2);
			t.setStroke(javafx.scene.paint.Color.BLACK);
			t.setUserData(u);
			children.add(t);
		} else {
			t = (Text) children.get(children.size() - 1);
			t.setText(t.getText() + c);
		}

		if (controller != null)
			controller.adjustFontSizeToVotes();

	}

	private void logInteraction(User u, boolean isDeletion) {
		if (this.c != null)
			INTERACTION_LOGGER.contentConceptData(this.c, u, isDeletion);
		else
			INTERACTION_LOGGER.contentLinkData(this.l, u, isDeletion);
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

	public TextFlow getCaption() {
		return caption;
	}

	public StringProperty textProperty() {
		return textBinding;
	}

}
