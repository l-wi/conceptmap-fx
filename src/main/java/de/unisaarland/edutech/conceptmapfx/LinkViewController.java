package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener.Direction;
import de.unisaarland.edutech.conceptmapfx.event.LinkEditRequestedListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class LinkViewController implements ConceptMovingListener, InputClosedListener {

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

	private Pane cmv;

	private boolean isSelected = false;

	private double userRotationFactor = 0;
	
	public LinkViewController(List<User> participants, Pane cmv, ConceptViewController cv1, ConceptViewController cv2) {

		this.cmv = cmv;
		this.cv1 = cv1;
		this.cv2 = cv2;
		this.start = new MoveTo();
		this.end = new LineTo();

		this.linkingPath = new Path();
		linkingPath.setStrokeWidth(5);
		linkingPath.setStroke(Color.WHITE);

		aStart = new AnchorView(this, Color.WHITE, 25, 25);
		aEnd = new AnchorView(this, Color.WHITE, 25, 25);

		this.linkingPath.getElements().add(start);
		this.linkingPath.getElements().add(end);

		this.linkingPath.getStyleClass().add("linkPath");
		this.participants = participants;

	}

	public void initialize() {
		initEditorComponent();
		cmv.getChildren().add(aStart);
		cmv.getChildren().add(aEnd);

		cmv.getChildren().add(linkingPath);
		cmv.getChildren().add(linkCaption);

		FourUserTouchEditable view = cv1.getView();

		view.widthProperty().addListener((c, o, n) -> this.layout());
		view.heightProperty().addListener((c, o, n) -> this.layout());
		view.widthProperty().addListener((c, o, n) -> this.layout());
		view.heightProperty().addListener((c, o, n) -> this.layout());

		linkingPath.setOnMouseClicked((e) -> {
			// TODO check if that works on touch device
			if (e.getClickCount() == 2) {
				this.remove();
			} else if (e.getClickCount() == 1) {
				toggleState();
			}
		});

		layout();
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
		int aEndIndex = workingCollection.indexOf(aEnd);
		int end = workingCollection.size() - 1;
		Collections.rotate(workingCollection.subList(aStartIndex, end), -1);
		Collections.rotate(workingCollection.subList(aEndIndex, end), -1);
		cmv.getChildren().setAll(workingCollection);
	}

	private void onRotate(Double rotate) {
		userRotationFactor = (userRotationFactor + 180) % 360;
		
		double r = (this.linkCaption.getRotate() + 180) % 360;
		this.linkCaption.setRotate(r);

	}

	public void setLink(Link link) {
		this.link = link;
	}

	private void initEditorComponent() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LinkView.fxml"));

			this.linkCaption = loader.load();

			this.editable = new CollaborativeStringTextFieldBinding(this.link.getCaption(),
					this.linkCaption.textProperty());

			linkCaption.setTopToggleText(participants.get(0).getName());
			linkCaption.setLeftToggleText(participants.get(1).getName());
			linkCaption.setBottomToggleText(participants.get(2).getName());
			linkCaption.setRightToggleText(participants.get(3).getName());

			linkCaption.selectionChangedProperty().addListener((l, o, n) -> {
				if (n.isSelected)
					this.fireEditRequested(participants.get(n.index));
			});

			linkCaption.setOnMoving((x, y, r) -> {
				this.onRotate(r);
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
			// FIXME indicate that this is not possible on the UI!
			a.toCircle();
			return;
		} else if (aStart.isDirected() && !aEnd.isDirected())
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

		linkCaption.setTranslateX(startAnchorPoint.getX() + betweenAnchors.getX() / 2 - linkCaption.getWidth() / 2);
		linkCaption.setTranslateY(startAnchorPoint.getY() + betweenAnchors.getY() / 2 + 15);

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

		double angleDiff = angleX - linkCaption.getRotate();

		LOG.info("angle Difference: " + angleDiff);

		if (angleY < 90) {
			aStart.setRotate(angleX);
			aEnd.setRotate(angleX + 180);
			linkCaption.setRotate(+angleX + userRotationFactor);
		} else {
			aStart.setRotate(-angleX);
			aEnd.setRotate(-angleX + 180);
			linkCaption.setRotate(-angleX + userRotationFactor );
		}

	}

	private void fireEditRequested(User u) {
		linkEditListeners.forEach(l -> l.linkEditRequested(this, this.editable, u));
	}

	public void remove() {
		removeFromView();
		// TODO do we get a user here?
		fireLinkDeletion(getActiveUser());
	}

	public void removeFromView() {
		cmv.getChildren().remove(aStart);
		cmv.getChildren().remove(aEnd);
		cmv.getChildren().remove(linkingPath);
		cmv.getChildren().remove(linkCaption);
	}

	public User getActiveUser() {
		int index = linkCaption.getSelected();
		if (index == -1)
			return null;
		else
			return participants.get(index);
	}
}
