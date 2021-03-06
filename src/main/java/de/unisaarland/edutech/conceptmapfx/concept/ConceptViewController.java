/*******************************************************************************
 * conceptmap-fx a concept mapping prototype for research.
 * Copyright (C) Tim Steuer (master's thesis 2016)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, US
 *******************************************************************************/
package de.unisaarland.edutech.conceptmapfx.concept;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptContentChangeListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Transform;
import javafx.util.Duration;

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

	private Path crossOut;

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

		conceptCaption.setOnDoubleTapped(() -> {
			conceptDeletionInitiated(getActiveUser());
		});

		conceptCaption.textProperty().addListener((ListChangeListener.Change<? extends Node> l) -> {

			fireConceptContentChanged(conceptCaption.getText());
		});

	}

	private void conceptDeletionInitiated(User activeUser) {
		initCrossOut();

		animateToDeleteState();

		EventHandler<? super MouseEvent> onMousePressed = conceptCaption.getOnMousePressed();
		EventHandler<? super TouchEvent> onTouchPressed = conceptCaption.getOnTouchPressed();

		conceptCaption.setOnMousePressed(e -> {
			if (!e.isSynthesized()) {
				conceptCaption.setOnMouseClicked(null);
				fireConceptDeleted();
			}
		});
		conceptCaption.setOnTouchPressed(e -> {
			conceptCaption.setOnTouchPressed(null);
			fireConceptDeleted();
		});

		PauseTransition p = new PauseTransition(Duration.seconds(2));
		p.setOnFinished(e -> {
			conceptCaption.getChildren().remove(crossOut);
			conceptCaption.setOnMousePressed(onMousePressed);
			conceptCaption.setOnTouchPressed(onTouchPressed);
			conceptCaption.setOpacity(1);
		});

		p.play();

		this.conceptCaption.getChildren().add(crossOut);

	}

	private void animateToDeleteState() {
		FadeTransition fd = new FadeTransition(Duration.millis(200), conceptCaption);
		fd.setByValue(-0.5);

		RotateTransition rt = new RotateTransition(Duration.millis(100), conceptCaption);
		double rotate = conceptCaption.getRotate();
		int wiggle = 10;
		rt.setFromAngle(rotate - wiggle);
		rt.setToAngle(rotate + wiggle);
		rt.setCycleCount(4);
		rt.setAutoReverse(true);

		ParallelTransition pl = new ParallelTransition(fd, rt);
		pl.setOnFinished(f -> conceptCaption.setRotate(rotate));
		pl.play();
	}

	private void initCrossOut() {
		Bounds boundsInScene = conceptCaption.getBoundsInLocal();

		Point2D pStart = new Point2D(boundsInScene.getMinX(), boundsInScene.getMinY());
		Point2D pEnd = new Point2D(boundsInScene.getMaxX(), boundsInScene.getMaxY());

		MoveTo m = new MoveTo(pStart.getX(), pStart.getY());
		LineTo l = new LineTo(pEnd.getX(), pEnd.getY());

		MoveTo m2 = new MoveTo(boundsInScene.getMaxX(), boundsInScene.getMinY());
		LineTo l2 = new LineTo(boundsInScene.getMinX(), boundsInScene.getMaxY());

		crossOut = new Path(m, l, m2, l2);
		crossOut.setStroke(Color.RED);
		crossOut.setStrokeWidth(12);
	}

	private void fireConceptContentChanged(String newContent) {
		this.conceptContentChangedListeners.forEach((l) -> l.conceptContentChanged(this, newContent));
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
		this.colBinding = CollaborativeStringTextFieldBinding.createBinding(concept, this);

		int index = participants.indexOf(concept.getOwner());
		String result = conceptCaption.getCSSClassForIndex(index);

		this.getView().lookup("#captionPane").getStyleClass().add(result);
	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;

		conceptCaption.selectionChangedProperty().addListener((l, o, n) -> {
			if (n.isSelected) {
				this.fireEditRequested(participants.get(n.index));
				this.animateEditRequest();

			}
		});

		initToggleTexts(participants);

		conceptCaption.setUserCount(participants.size());
	}

	private void animateEditRequest() {

		RotateTransition rt = new RotateTransition(Duration.millis(100), conceptCaption);
		double rotate = conceptCaption.getRotate();
		int wiggle = 10;
		rt.setFromAngle(rotate - wiggle);
		rt.setToAngle(rotate + wiggle);
		rt.setCycleCount(4);
		rt.setAutoReverse(true);
		rt.setOnFinished(f -> conceptCaption.setRotate(rotate));
		rt.play();

		// ParallelTransition pl = new ParallelTransition(fd, rt);
		// pl.setOnFinished(f -> conceptCaption.setRotate(rotate));
		// pl.play();
	}

	private void initToggleTexts(List<User> participants) {
		conceptCaption.setTopToggleText(String.valueOf(participants.get(0).getName().charAt(0)));
		conceptCaption.setBottomToggleText(String.valueOf(participants.get(1).getName().charAt(0)));

		if (participants.size() > 2)
			conceptCaption.setLeftToggleText(String.valueOf(participants.get(2).getName().charAt(0)));
		if (participants.size() > 3)
			conceptCaption.setRightToggleText(String.valueOf(participants.get(3).getName().charAt(0)));
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

	public void rotateAbsolute(int r) {
		Concept c = this.getConcept();

		c.setPosition(c.getX(), c.getY(), r);
		this.getView().setRotate(r);
	}

	public TextFlow getTextFlow() {
		return conceptCaption.getCaption();
	}

	public void onVote(User user, boolean hasVoted) {

		concept.setVoted(user, hasVoted);
		adjustFontSizeToVotes();
	}

	public void adjustFontSizeToVotes() {
		ObservableList<Node> children = this.getTextFlow().getChildren();
		for (Node n : children) {
			Text t = (Text) n;
			Font f = new Font(20 + 10 * (concept.getVotes()));
			t.setFont(f);
		}
	}
}
