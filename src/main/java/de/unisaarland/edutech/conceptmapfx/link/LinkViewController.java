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
package de.unisaarland.edutech.conceptmapfx.link;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapView;
import de.unisaarland.edutech.conceptmapfx.event.AnchorAlteredListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener.Direction;
import de.unisaarland.edutech.conceptmapfx.event.LinkEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class LinkViewController implements ConceptMovingListener, InputClosedListener, AnchorAlteredListener {

	private static final Logger LOG = LoggerFactory.getLogger(LinkViewController.class);

	private List<LinkDirectionUpdatedListener> linkDirectionListeners = new ArrayList<LinkDirectionUpdatedListener>();
	private List<LinkEditRequestedListener> linkEditListeners = new ArrayList<LinkEditRequestedListener>();
	private List<LinkDeletedListener> linkDeletedListeners = new ArrayList<LinkDeletedListener>();

	private FourUserTouchEditable linkCaption;

	private Path linkingPath;
	private MoveTo start;
	private LineTo end;
	private ConceptViewController cv1;
	private ConceptViewController cv2;
	private AnchorView aStart;
	private AnchorView aEnd;

	private List<User> participants = new ArrayList<User>();

	private CollaborativeStringTextFieldBinding editable;

	private Link link;

	private ConceptMapView cmv;

	private boolean isSelected = false;

	private double userRotationFactor = 0;

	private double rotateTreshold = 0;

	public LinkViewController(List<User> participants, ConceptMapView cmv, ConceptViewController cv1,
			ConceptViewController cv2) {

		this.cmv = cmv;
		this.cv1 = cv1;
		this.cv2 = cv2;
		this.start = new MoveTo();
		this.end = new LineTo();

		this.linkingPath = new Path();
		linkingPath.setStrokeWidth(10);
		linkingPath.setStroke(Color.WHITE);

		aStart = new AnchorView(this, Color.WHITE, 25, 25);
		aEnd = new AnchorView(this, Color.WHITE, 25, 25);

		this.linkingPath.getElements().add(start);
		this.linkingPath.getElements().add(end);

		this.linkingPath.getStyleClass().add("linkPath");

		this.linkingPath.setCache(true);
		this.linkingPath.setCacheHint(CacheHint.SPEED);

		this.participants = participants;

	}

	public void initialize() {
		initEditorComponent();
		cmv.add(aStart);
		cmv.add(aEnd);

		cmv.add(linkingPath);
		cmv.add(linkCaption);

		FourUserTouchEditable view = cv1.getView();

		view.widthProperty().addListener((c, o, n) -> this.layout());
		view.heightProperty().addListener((c, o, n) -> this.layout());
		view.widthProperty().addListener((c, o, n) -> this.layout());
		view.heightProperty().addListener((c, o, n) -> this.layout());

		linkingPath.setOnMouseClicked((e) -> {
			if (e.getClickCount() == 2) {
				this.remove();
			} else if (e.getClickCount() == 1) {
				toggleState();
			}
		});

		linkCaption.getCaption().setMinHeight(25);
		linkCaption.setMaxHeight(25);

		linkingPath.setCache(true);

		initToggleTexts();

		linkCaption.setUserCount(participants.size());

		layout();
	}

	private void initToggleTexts() {
		linkCaption.setTopToggleText(String.valueOf(participants.get(0).getName().charAt(0)));
		linkCaption.setBottomToggleText(String.valueOf(participants.get(1).getName().charAt(0)));

		if (participants.size() > 2)
			linkCaption.setLeftToggleText(String.valueOf(participants.get(2).getName().charAt(0)));
		if (participants.size() > 3)
			linkCaption.setRightToggleText(String.valueOf(participants.get(3).getName().charAt(0)));
	}

	private void toggleState() {
		isSelected = !isSelected;

		if (isSelected) {
			linkingPath.setStroke(Color.RED);
			bringAnchorsToFront();
		} else
			linkingPath.setStroke(Color.WHITE);

		aStart.setActive(isSelected);
		aEnd.setActive(isSelected);
	}

	private void bringAnchorsToFront() {
		ObservableList<Node> workingCollection = FXCollections.observableArrayList(cmv.getChildren());

		int aStartIndex = workingCollection.indexOf(aStart);
		int end = workingCollection.size();

		Collections.rotate(workingCollection.subList(aStartIndex, end), -1);
		cmv.getChildren().setAll(workingCollection);

		int aEndIndex = workingCollection.indexOf(aEnd);

		Collections.rotate(workingCollection.subList(aEndIndex, end), -1);
		cmv.getChildren().setAll(workingCollection);

	}

	private void onRotate(Double rotate) {
		rotateTreshold += rotate;
		if (Math.abs(rotateTreshold) < 70)
			return;
		rotateTreshold = 0;

		userRotationFactor = (userRotationFactor + 180) % 360;

		double r = (this.linkCaption.getRotate() + 180) % 360;
		this.linkCaption.setRotate(r);

	}

	public void setLink(Link link) {
		this.link = link;
	}

	private void initEditorComponent() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/LinkView.fxml"));

			this.linkCaption = loader.load();

			linkCaption.getCaption().setMaxHeight(25);

			this.editable = CollaborativeStringTextFieldBinding.createBinding(this.link, this.linkCaption.getCaption());

			linkCaption.selectionChangedProperty().addListener((l, o, n) -> {
				if (n.isSelected)
					this.fireEditRequested(participants.get(n.index));
			});

			linkCaption.setOnMoving((x, y, r) -> {
				this.onRotate(r);
			});

			linkCaption.setOnMoved(() -> {
				rotateTreshold = 0;
			});

		} catch (IOException e) {
			// should never happen (FXML broken)
			throw new RuntimeException(e);
		}
	}

	public void addLinkDirectionUpdatedListener(LinkDirectionUpdatedListener l) {
		linkDirectionListeners.add(l);
	}

	public void addLinkEditRequestedListener(LinkEditRequestedListener l) {
		linkEditListeners.add(l);
	}

	public void addLinkDeletionListener(LinkDeletedListener l) {
		linkDeletedListeners.add(l);
	}

	public void anchorAltered(AnchorView a) {
		AnchorView b = (a.equals(aStart)) ? aEnd : aStart;

		LinkDirectionUpdatedListener.Direction d = Direction.NOT_DIRECTED;

		a.toggle();

		if (a.isDirected() && b.isDirected()) {
			b.toCircle();
		}
		if (aStart.isDirected() && !aEnd.isDirected())
			d = Direction.END_TO_START;
		else if (!aStart.isDirected() && aEnd.isDirected())
			d = Direction.START_TO_END;

		fireLinkDirectionUpdate(d);

	}

	public void setDirected(Direction d) {
		if (d == Direction.START_TO_END) {
			aEnd.toArrow();
			aStart.toCircle();
		} else if (d == Direction.END_TO_START) {
			aEnd.toCircle();
			aStart.toArrow();
		} else {
			aStart.toCircle();
			aEnd.toCircle();
		}

		fireLinkDirectionUpdate(d);
	}

	public void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		if (cv1.equals(cv) || cv2.equals(cv))
			layout();

	}

	public Concept getEnd() {
		return cv2.getConcept();
	}

	public Concept getStart() {
		return cv1.getConcept();
	}

	public void inputClosed(User u) {
		setUserEnabled(participants.indexOf(u), false);
	}

	public void setUserEnabled(User owner, boolean b) {
		int index = participants.indexOf(owner);

		setUserEnabled(index, b);

	}

	private void setUserEnabled(int index, boolean b) {
		linkCaption.setSelected(index, b);
	}

	private Point2D computeCenterAnchorTranslation(FourUserTouchEditable conceptView, Point2D betweenVector) {
		Point2D xAxis = new Point2D(1, 0);
		Point2D yAxis = new Point2D(0, 1);

		double rotation = Math.toRadians(conceptView.getRotate());

		xAxis = rotatePoint2D(xAxis, rotation);
		yAxis = rotatePoint2D(yAxis, rotation);

		double angleX = xAxis.angle(betweenVector) - 90;
		double angleY = yAxis.angle(betweenVector) - 90;

		double directionX = (angleX >= 0) ? -1 : 1;
		double directionY = (angleY >= 0) ? -1 : 1;

		double width = conceptView.getWidth();
		double height = conceptView.getHeight();

		if (Math.abs(angleX) > Math.abs(angleY)) {

			return xAxis.normalize().multiply(directionX * width / 2);
		} else {

			return yAxis.normalize().multiply(directionY * height / 2);
		}
	}

	private void fireLinkDirectionUpdate(LinkDirectionUpdatedListener.Direction d) {
		LOG.info("firing LinkDirectionUodated events");
		linkDirectionListeners.forEach((l) -> l.linkDirectionUpdated(this, d, null));
	}

	private void fireLinkDeletion(User u) {
		linkDeletedListeners.forEach((l) -> l.linkDeleted(this, u));
	}

	private Point2D rotatePoint2D(Point2D xAxis, double rotation) {
		double xRotated = xAxis.getX() * Math.cos(rotation) - xAxis.getY() * Math.sin(rotation);
		double yRotated = xAxis.getX() * Math.sin(rotation) + xAxis.getY() * Math.cos(rotation);

		return new Point2D(xRotated, yRotated);
	}

	public void layout() {

		FourUserTouchEditable view1 = cv1.getView();
		FourUserTouchEditable view2 = cv2.getView();

		Point2D centerStart = view1.getCenterAsSceneCoordinates();
		Point2D centerEnd = view2.getCenterAsSceneCoordinates();

		Point2D betweenCenters = centerEnd.subtract(centerStart);

		Point2D pTranslateCV1 = computeCenterAnchorTranslation(cv1.getView(), betweenCenters);
		Point2D pTranslateCV2 = computeCenterAnchorTranslation(cv2.getView(), betweenCenters);

		Point2D startAnchorPoint = centerStart.add(pTranslateCV1);
		Point2D endAnchorPoint = centerEnd.subtract(pTranslateCV2);

		start.setX(startAnchorPoint.getX());
		start.setY(startAnchorPoint.getY());

		end.setX(endAnchorPoint.getX());
		end.setY(endAnchorPoint.getY());

		// calculate angle for anchors
		Point2D betweenAnchors = endAnchorPoint.subtract(startAnchorPoint);
		double angleX = Math.acos(betweenAnchors.normalize().dotProduct(new Point2D(1, 0)));
		double angleY = Math.acos(betweenAnchors.normalize().dotProduct(new Point2D(0, 1)));

		double centerOfLineX = startAnchorPoint.getX() + betweenAnchors.getX() / 2 - linkCaption.getWidth() / 2;
		double centerOfLineY = startAnchorPoint.getY() + betweenAnchors.getY() / 2 - linkCaption.getHeight() / 2;

		Point2D p = linkCaption.getLocalToParentTransform().deltaTransform(0, 30);

		linkCaption.setTranslateX(centerOfLineX + p.getX());
		linkCaption.setTranslateY(centerOfLineY + p.getY());

		angleX = Math.toDegrees(angleX);
		angleY = Math.toDegrees(angleY);

		DoubleBinding aStartXTranslate = start.xProperty().subtract(aStart.widthProperty().divide(2));
		DoubleBinding aStartYTranslate = start.yProperty().subtract(aStart.heightProperty().divide(2));

		aStart.translateXProperty().bind(aStartXTranslate);
		aStart.translateYProperty().bind(aStartYTranslate);

		DoubleBinding aEndXTranslate = end.xProperty().subtract(aEnd.widthProperty().divide(2));
		DoubleBinding aEndYTranslate = end.yProperty().subtract(aEnd.heightProperty().divide(2));

		aEnd.translateXProperty().bind(aEndXTranslate);
		aEnd.translateYProperty().bind(aEndYTranslate);

		if (angleY < 90) {
			aStart.setRotate(angleX);
			aEnd.setRotate(angleX + 180);
			linkCaption.setRotate(+angleX + userRotationFactor);
		} else {
			aStart.setRotate(-angleX);
			aEnd.setRotate(-angleX + 180);
			linkCaption.setRotate(-angleX + userRotationFactor);
		}

	}

	private void fireEditRequested(User u) {
		linkEditListeners.forEach(l -> l.linkEditRequested(this, this.editable, u));
	}

	public void remove() {
		removeFromView();
		fireLinkDeletion(getActiveUser());
	}

	public void removeFromView() {
		cmv.remove(aStart);
		cmv.remove(aEnd);
		cmv.remove(linkingPath);
		cmv.remove(linkCaption);
	}

	public User getActiveUser() {
		int index = linkCaption.getSelected();
		if (index == -1)
			return null;
		else
			return participants.get(index);
	}

	public void rotateAbsolute(int r) {
		this.linkCaption.setRotate(r);
	}

	public Link getLink() {
		return link;
	}
}
