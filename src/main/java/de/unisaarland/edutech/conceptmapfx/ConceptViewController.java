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
import javafx.fxml.FXML;
import javafx.geometry.Bounds;

public class ConceptViewController implements ConceptMovingListener, InputClosedListener {

	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewController.class);

	private List<ConceptEditRequestedListener> conceptEditListeners = new ArrayList<ConceptEditRequestedListener>();
	private List<ConceptMovingListener> conceptMovingListeners = new ArrayList<ConceptMovingListener>();
	private List<ConceptMovedListener> conceptMovedListeners = new ArrayList<ConceptMovedListener>();

	@FXML
	private FourUserTouchEditable conceptCaption;

	private Concept concept;

	private List<User> participants;

	private CollaborativeStringTextFieldBinding colBinding;

	public void addConceptEditRequestedListener(ConceptEditRequestedListener l) {
		conceptEditListeners.add(l);
	}

	public void addConceptMovedListener(ConceptMovedListener l) {
		conceptMovedListeners.add(l);
	}

	public void addConceptMovingListener(ConceptMovingListener l) {
		conceptMovingListeners.add(l);
	}

	public void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		conceptCaption.setRotate(rotate);
		cv.translate(x, y);

	}

	private void fireConceptMoved() {
		conceptMovedListeners.forEach(l -> l.conceptMoved(this));
	}

	private void fireConceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {

		// TODO when to overlap here we should somehow indicate on UI that they
		// will be linked when dropped
		conceptMovingListeners.forEach(l -> l.conceptMoving(x, y, rotate, cv, u));
	}

	private void fireEditRequested(User u) {
		conceptEditListeners.forEach(l -> l.conceptEditRequested(this, this.colBinding, u));
	}

	public Concept getConcept() {
		return concept;
	}

	@FXML
	public void initialize() {

		this.addConceptMovingListener(this);

		conceptCaption.setOnMoving((x, y, r) -> {
			this.fireConceptMoving(x, y, r, this, null);
		});

		conceptCaption.setOnMoved(() -> this.fireConceptMoved());
	}

	public void inputClosed(User u) {
		setUserEnabled(participants.indexOf(u), false);
	}

	public boolean intersects(ConceptViewController other) {
		Bounds myParentBounds = this.conceptCaption.getBoundsInParent();
		Bounds otherParentBounds = other.conceptCaption.getBoundsInParent();

		return myParentBounds.intersects(otherParentBounds);
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
		this.colBinding = new CollaborativeStringTextFieldBinding(concept.getName(),
				conceptCaption.textProperty());

	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;

		conceptCaption.setTopToggleText(participants.get(0).getName());
		conceptCaption.topSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(0));
		});

		conceptCaption.setLeftToggleText(participants.get(1).getName());
		conceptCaption.leftSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(1));
		});

		conceptCaption.setBottomToggleText(participants.get(2).getName());
		conceptCaption.bottomSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(2));
		});

		conceptCaption.setRightToggleText(participants.get(3).getName());
		conceptCaption.rightSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(3));
		});
	}

	public void setUserEnabled(User owner, boolean b) {
		int index = participants.indexOf(owner);

		setUserEnabled(index, b);

	}

	private void setUserEnabled(int index, boolean b) {
		switch (index) {
		case 0:
			conceptCaption.setTopSelected(b);
			break;
		case 1:
			conceptCaption.setLeftSelected(b);
			break;
		case 2:
			conceptCaption.setBottomSelected(b);
			break;
		case 3:
			conceptCaption.setRightSelected(b);
			break;
		}
	}

	public void translate(double x, double y) {
		double rotation = Math.toRadians(conceptCaption.getRotate());

		// if we have a rotation we have to convert to the rotated coordinate
		// system
		double xRotated = x * Math.cos(rotation) - y * Math.sin(rotation);
		double yRotated = x * Math.sin(rotation) + y * Math.cos(rotation);
		translateRelative(xRotated, yRotated);
	}

	private void translateRelative(double xRotated, double yRotated) {
		translateAbsolute(conceptCaption.getTranslateX() + xRotated,
				conceptCaption.getTranslateY() + yRotated);
	}

	public void translateAbsolute(double x, double y) {
		conceptCaption.setTranslateX(x);
		conceptCaption.setTranslateY(y);
	}

	public FourUserTouchEditable getView() {
		return this.conceptCaption;
	}
}
