package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener.Direction;
import de.unisaarland.edutech.conceptmapfx.event.LinkEditRequestedListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class LinkViewController implements ConceptMovingListener, InputClosedListener, UserToggleEnabledListener {

	private static final Logger LOG = LoggerFactory.getLogger(LinkViewController.class);

	private List<LinkDirectionUpdatedListener> linkDirectionListeners = new ArrayList<LinkDirectionUpdatedListener>();
	private List<LinkEditRequestedListener> linkEditListeners = new ArrayList<LinkEditRequestedListener>();

	private ToggleButton btnToogleUser1;
	private ToggleButton btnToogleUser2;
	private ToggleButton btnToogleUser3;
	private ToggleButton btnToogleUser4;
	private TextField txtLink;
	private Pane linkViewEditor;

	private Path linkingPath;
	private MoveTo start;
	private LineTo end;
	private ConceptViewController cv1;
	private ConceptViewController cv2;
	private AnchorView aStart;
	private AnchorView aEnd;

	private List<User> participants = new ArrayList<User>();

	private Editable editable;

	private Link link;

	/*
	 * TODO Hier weitermachen: Implementieren eines Edits auf link - Dazu
	 * bbrauchen wir einen View + Userauswahl - Das egentliche Link Element hier
	 * irgendwo
	 * 
	 * 
	 */
	public LinkViewController(Link link,  List<User> participants, Pane cmv, ConceptViewController cv1, ConceptViewController cv2) {
		this.link = link;
		initEditorComponent();
		this.cv1 = cv1;
		this.cv2 = cv2;
		this.start = new MoveTo();
		this.end = new LineTo();
		this.linkingPath = new Path();
		

		aStart = new AnchorView(this, Color.BLUE, 25, 25);
		aEnd = new AnchorView(this, Color.BLUE, 25, 25);

		cmv.getChildren().add(aStart);
		cmv.getChildren().add(aEnd);

		this.linkingPath.getElements().add(start);
		this.linkingPath.getElements().add(end);

		cmv.getChildren().add(linkingPath);
		cmv.getChildren().add(linkViewEditor);
		
		this.participants = participants;

	}

	public void initEditorComponent() {
		try {
			Pane view = (Pane) FXMLLoader.load(getClass().getResource("LinkView.fxml"));
			this.linkViewEditor = view;
			this.btnToogleUser1 = (ToggleButton) view.lookup("#p1");
			this.btnToogleUser2 = (ToggleButton) view.lookup("#p2");
			this.btnToogleUser3 = (ToggleButton) view.lookup("#p3");
			this.btnToogleUser4 = (ToggleButton) view.lookup("#p4");
			this.txtLink = (TextField) view.lookup("#txtLink");
			this.editable = new Editable(link.getCaption(), txtLink);
			new InputToggleGroup(this, btnToogleUser1, btnToogleUser2, btnToogleUser3, btnToogleUser4);

		} catch (IOException e) {
			//should never happen (FXML broken)
			throw new RuntimeException(e);
		}
	}

	public void addLinkDirectionUpdatedListener(LinkDirectionUpdatedListener l) {
		linkDirectionListeners.add(l);
	}

	public void addLinkEditRequestedListener(LinkEditRequestedListener l) {
		linkEditListeners.add(l);
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

	public void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		if (cv1.equals(cv) || cv2.equals(cv))
			updateLinkBetween();

	}

	public Concept getEnd() {
		return cv2.getConcept();
	}

	public Concept getStart() {
		return cv1.getConcept();
	}

	public void inputClosed(User u) {
		// TODO implement input closed on link

	}

	private Point2D computeCenterAnchorTranslation(ConceptViewController controller, Point2D betweenVector) {
		// FIXME when nodes are close together lines go through the nodes not
		// taking best anchor point

		Point2D xAxis = new Point2D(1, 0);
		Point2D yAxis = new Point2D(0, 1);

		double rotation = Math.toRadians(controller.getRotate());

		xAxis = rotatePoint2D(xAxis, rotation);
		yAxis = rotatePoint2D(yAxis, rotation);

		double angleX = xAxis.angle(betweenVector) - 90;
		double angleY = yAxis.angle(betweenVector) - 90;

		double directionX = (angleX >= 0) ? -1 : 1;
		double directionY = (angleY >= 0) ? -1 : 1;

		if (Math.abs(angleX) > Math.abs(angleY))
			return xAxis.normalize().multiply(directionX * controller.getWidth() / 2);

		else
			return yAxis.normalize().multiply(directionY * controller.getHeight() / 2);
	}

	private void fireLinkDirectionUpdate(LinkDirectionUpdatedListener.Direction d) {
		LOG.info("firing LinkDirectionUodated events");
		linkDirectionListeners.forEach((l) -> l.linkDirectionUpdated(this, d, null));
	}

	private Point2D rotatePoint2D(Point2D xAxis, double rotation) {
		double xRotated = xAxis.getX() * Math.cos(rotation) - xAxis.getY() * Math.sin(rotation);
		double yRotated = xAxis.getX() * Math.sin(rotation) + xAxis.getY() * Math.cos(rotation);

		return new Point2D(xRotated, yRotated);
	}

	private void updateLinkBetween() {

		Point2D centerStart = cv1.getCenterAsSceneCoordinates();
		Point2D centerEnd = cv2.getCenterAsSceneCoordinates();

		Point2D betweenCenters = centerEnd.subtract(centerStart);

		Point2D pTranslateCV1 = computeCenterAnchorTranslation(cv1, betweenCenters);
		Point2D pTranslateCV2 = computeCenterAnchorTranslation(cv2, betweenCenters);

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

		linkViewEditor.setTranslateX(startAnchorPoint.getX() + betweenAnchors.getX() / 2 - linkViewEditor.getWidth()/2);
		linkViewEditor.setTranslateY(startAnchorPoint.getY() + betweenAnchors.getY() / 2);

		angleX = Math.toDegrees(angleX);
		angleY = Math.toDegrees(angleY);

		aStart.setTranslateX(start.getX() - aStart.getWidth() / 2);
		aStart.setTranslateY(start.getY() - aStart.getHeight() / 2);

		aEnd.setTranslateX(end.getX() - aEnd.getWidth() / 2);
		aEnd.setTranslateY(end.getY() - aEnd.getHeight() / 2);

		if (angleY < 90) {
			aStart.setRotate(angleX);
			aEnd.setRotate(angleX + 180);
			linkViewEditor.setRotate(angleX);
		} else {
			aStart.setRotate(-angleX);
			aEnd.setRotate(-angleX + 180);
			linkViewEditor.setRotate(-angleX);
		}

	}

	@Override
	public void userToggleEnabled(int buttonID) {
		this.txtLink.setDisable(false);
		this.fireEditRequested(participants.get(buttonID));
	}

	private void fireEditRequested(User u) {
		linkEditListeners.forEach(l -> l.linkEditRequested(this, this.editable, u));
	}

	

}
