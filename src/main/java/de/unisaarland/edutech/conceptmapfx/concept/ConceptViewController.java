package de.unisaarland.edutech.conceptmapfx.concept;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapfx.FourUserTouchEditable;
import de.unisaarland.edutech.conceptmapfx.event.ConceptContentChangeListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.transform.Transform;

public class ConceptViewController implements ConceptMovingListener, InputClosedListener {

	private static final Logger LOG = LoggerFactory.getLogger(ConceptViewController.class);

	private List<ConceptEditRequestedListener> conceptEditListeners = new ArrayList<ConceptEditRequestedListener>();
	private List<ConceptMovingListener> conceptMovingListeners = new ArrayList<ConceptMovingListener>();
	private List<ConceptMovedListener> conceptMovedListeners = new ArrayList<ConceptMovedListener>();
	private List<ConceptDeletedListener> conceptDeletedListeners = new ArrayList<ConceptDeletedListener>();
	private List<ConceptContentChangeListener> conceptContentChangedListeners = new ArrayList<ConceptContentChangeListener>();

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
		cv.translate(x, y);
		conceptCaption.setRotate(conceptCaption.getRotate() + rotate);

	}

	private void fireConceptMoved() {
		conceptMovedListeners.forEach(l -> l.conceptMoved(this));
	}

	private void fireConceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		conceptMovingListeners.forEach(l -> l.conceptMoving(x, y, rotate, cv, u));
	}

	private void fireEditRequested(User u) {
		conceptEditListeners.forEach(l -> l.conceptEditRequested(this, this.colBinding, u));
	}

	public Concept getConcept() {
		return concept;
	}

	public void fireConceptDeleted() {
		fireConceptDeleted(getActiveUser());
	}

	@FXML
	public void initialize() {

		this.addConceptMovingListener(this);

		conceptCaption.setOnMoving((x, y, r) -> {
			this.fireConceptMoving(x, y, r, this, null);
		});

		conceptCaption.setOnMoved(() -> this.fireConceptMoved());

		conceptCaption.setOnDoubleTapped(() -> fireConceptDeleted(getActiveUser()));

		conceptCaption.textProperty().addListener((c, o, n) ->

		{
			fireConceptContentChanged(o, n);
		});

	}

	private void fireConceptContentChanged(String oldContent, String newContent) {
		this.conceptContentChangedListeners.forEach((l) -> l.conceptContentChanged(this, oldContent, newContent));
	}

	private void fireConceptDeleted(User u) {
		this.conceptDeletedListeners.forEach((l) -> l.conceptDeleted(this, u));
	}

	private User getActiveUser() {
		int index = getView().getSelected();
		if (index == -1)
			return null;
		else
			return participants.get(index);
	}

	public void addConceptDeletedListener(ConceptDeletedListener l) {
		this.conceptDeletedListeners.add(l);
	}

	public void inputClosed(User u) {
		setUserEnabled(participants.indexOf(u), false);
	}

	public boolean intersects(ConceptViewController other) {
		// check by using separating axis theorem
		FourUserTouchEditable viewA = getView();
		FourUserTouchEditable viewB = other.getView();
		Transform tA = viewA.getLocalToParentTransform();
		Transform tB = viewB.getLocalToParentTransform();

		Bounds boundsA = viewA.getOverlayBounds();
		Bounds boundsB = viewB.getOverlayBounds();

		Point2D[] pointsA = getPolygon(tA, boundsA);

		Point2D[] pointsB = getPolygon(tB, boundsB);

		return checkIfAllAxisOverlap(pointsA, pointsB) && checkIfAllAxisOverlap(pointsB, pointsA);
	}

	private boolean checkIfAllAxisOverlap(Point2D[] pointsA, Point2D[] pointsB) {

		for (int i = 0; i < pointsA.length; i++) {
			int j = (i + 1) % pointsA.length;
			Point2D edge = pointsA[i].subtract(pointsA[j]);
			Point2D axis = new Point2D(-edge.getY(), edge.getX()).normalize();

			Point2D minMaxA = project(axis, pointsA);
			Point2D minMaxB = project(axis, pointsB);

			if (!overlapProjection(minMaxA, minMaxB))
				return false;
		}
		return true;
	}

	private Point2D project(Point2D axis, Point2D[] points) {

		double min = axis.dotProduct(points[0]);
		double max = min;

		for (int i = 1; i < points.length; i++) {
			double tmp = axis.dotProduct(points[i]);

			if (tmp < min)
				min = tmp;
			else if (tmp > max)
				max = tmp;
		}

		return new Point2D(min, max);
	}

	private Point2D[] getPolygon(Transform t, Bounds bounds) {

		Point2D p1 = t.transform(new Point2D(0, 0));
		Point2D p2 = t.transform(new Point2D(bounds.getWidth(), 0));
		Point2D p3 = t.transform(new Point2D(bounds.getWidth(), bounds.getHeight()));
		Point2D p4 = t.transform(new Point2D(0, bounds.getHeight()));

		Point2D[] points = { p1, p2, p3, p4 };
		return points;
	}

	private boolean overlapProjection(Point2D minMaxA, Point2D minMaxB) {
		if (minMaxA.getX() < minMaxB.getX())
			return minMaxB.getX() - minMaxA.getY() < 0;
		else
			return minMaxA.getX() - minMaxB.getY() < 0;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
		this.colBinding = CollaborativeStringTextFieldBinding.createBinding(concept.getName(),
				conceptCaption.textProperty());

		int index = participants.indexOf(concept.getOwner());
		String result = getCSSClassForIndex(index);

		this.getView().lookup("#captionPane").getStyleClass().add(result);
	}

	private String getCSSClassForIndex(int index) {
		String result = "belongsTo";

		switch (index) {
		case 0:
			result += "Top";
			break;
		case 1:
			result += "Left";
			break;
		case 2:
			result += "Bottom";
			break;
		case 3:
			result += "Right";
			break;
		}
		return result;
	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;

		conceptCaption.selectionChangedProperty().addListener((l, o, n) -> {
			if (n.isSelected) {
				this.fireEditRequested(participants.get(n.index));

			}
		});

	}

	public void setUserEnabled(User owner, boolean b) {
		int index = participants.indexOf(owner);

		setUserEnabled(index, b);

	}

	private void setUserEnabled(int index, boolean b) {
		conceptCaption.setSelected(index, b);
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
		translateAbsolute(conceptCaption.getTranslateX() + xRotated, conceptCaption.getTranslateY() + yRotated);
	}

	public void translateAbsolute(double x, double y) {
		conceptCaption.setTranslateX(x);
		conceptCaption.setTranslateY(y);
	}

	public FourUserTouchEditable getView() {
		return this.conceptCaption;
	}

	public void addConceptEmptyListener(ConceptContentChangeListener usersController) {
		this.conceptContentChangedListeners.add(usersController);
	}
}
