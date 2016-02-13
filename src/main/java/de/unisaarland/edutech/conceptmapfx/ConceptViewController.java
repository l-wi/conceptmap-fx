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
	private FourUserTouchEditable fourUserTouchEditable;

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
		fourUserTouchEditable.setRotate(rotate);
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

		fourUserTouchEditable.setOnMoving((x, y, r) -> {
			this.fireConceptMoving(x, y, r, this, null);
		});

		fourUserTouchEditable.setOnMoved(() -> this.fireConceptMoved());
	}

	public void inputClosed(User u) {
		setUserEnabled(participants.indexOf(u), false);
	}

	public boolean intersects(ConceptViewController other) {
		Bounds myParentBounds = this.fourUserTouchEditable.getBoundsInParent();
		Bounds otherParentBounds = other.fourUserTouchEditable.getBoundsInParent();

		return myParentBounds.intersects(otherParentBounds);
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
		this.colBinding = new CollaborativeStringTextFieldBinding(concept.getName(),
				fourUserTouchEditable.textProperty());

	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;

		fourUserTouchEditable.setTopToggleText(participants.get(0).getName());
		fourUserTouchEditable.topSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(0));
		});

		fourUserTouchEditable.setLeftToggleText(participants.get(1).getName());
		fourUserTouchEditable.leftSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(1));
		});

		fourUserTouchEditable.setBottomToggleText(participants.get(2).getName());
		fourUserTouchEditable.bottomSelectedProperty().addListener((l, o, n) -> {
			if (n)
				this.fireEditRequested(participants.get(2));
		});

		fourUserTouchEditable.setRightToggleText(participants.get(3).getName());
		fourUserTouchEditable.rightSelectedProperty().addListener((l, o, n) -> {
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
			fourUserTouchEditable.setTopSelected(b);
			break;
		case 1:
			fourUserTouchEditable.setLeftSelected(b);
			break;
		case 2:
			fourUserTouchEditable.setBottomSelected(b);
			break;
		case 3:
			fourUserTouchEditable.setRightSelected(b);
			break;
		}
	}

	public void translate(double x, double y) {
		double rotation = Math.toRadians(fourUserTouchEditable.getRotate());

		// if we have a rotation we have to convert to the rotated coordinate
		// system
		double xRotated = x * Math.cos(rotation) - y * Math.sin(rotation);
		double yRotated = x * Math.sin(rotation) + y * Math.cos(rotation);
		translateRelative(xRotated, yRotated);
	}

	private void translateRelative(double xRotated, double yRotated) {
		translateAbsolute(fourUserTouchEditable.getTranslateX() + xRotated,
				fourUserTouchEditable.getTranslateY() + yRotated);
	}

	public void translateAbsolute(double x, double y) {
		fourUserTouchEditable.setTranslateX(x);
		fourUserTouchEditable.setTranslateY(y);
	}

	public FourUserTouchEditable getView() {
		return this.fourUserTouchEditable;
	}
}
