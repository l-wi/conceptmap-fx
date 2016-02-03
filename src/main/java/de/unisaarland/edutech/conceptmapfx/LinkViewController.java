package de.unisaarland.edutech.conceptmapfx;

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
import de.unisaarland.edutech.conceptmapping.User;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class LinkViewController implements ConceptMovingListener, InputClosedListener {

	private static final Logger LOG = LoggerFactory.getLogger(LinkViewController.class);

	private List<LinkDirectionUpdatedListener> linkDirectionListener = new ArrayList<LinkDirectionUpdatedListener>();
	private List<LinkEditRequestedListener> linkEditListners = new ArrayList<LinkEditRequestedListener>();

	private Path link;
	private MoveTo start;
	private LineTo end;
	private ConceptViewController cv1;
	private ConceptViewController cv2;
	private Anchor aStart;
	private Anchor aEnd;

	public LinkViewController(ConceptMapViewController cmv, ConceptViewController cv1, ConceptViewController cv2) {
		this.cv1 = cv1;
		this.cv2 = cv2;
		this.start = new MoveTo();
		this.end = new LineTo();
		this.link = new Path();

		aStart = new Anchor(this, Color.BLUE, 25, 25);
		aEnd = new Anchor(this, Color.BLUE, 25, 25);

		cmv.addAnchor(aStart);
		cmv.addAnchor(aEnd);

		this.link.getElements().add(start);
		this.link.getElements().add(end);

		updateLinkBetween();
	}

	public Path getLink() {
		return link;
	}

	private void updateLinkBetween() {
		Bounds cv1Bounds = cv1.getBoundsInScene();
		Bounds cv2Bounds = cv2.getBoundsInScene();

		double xCenterStart = cv1Bounds.getMinX() + cv1Bounds.getWidth() / 2;
		double yCenterStart = cv1Bounds.getMinY() + cv1Bounds.getHeight() / 2;

		Point2D centerStart = new Point2D(xCenterStart, yCenterStart);

		double xCenterEnd = cv2Bounds.getMinX() + cv2Bounds.getWidth() / 2;
		double yCenterEnd = cv2Bounds.getMinY() + cv2Bounds.getHeight() / 2;

		Point2D centerEnd = new Point2D(xCenterEnd, yCenterEnd);
		Point2D betweenCenters = centerEnd.subtract(centerStart);

		Point2D pTranslateCV1 = computeCenterAnchorTranslation(cv1Bounds, betweenCenters);
		Point2D pTranslateCV2 = computeCenterAnchorTranslation(cv2Bounds, betweenCenters);

		Point2D startAnchorPoint = centerStart.add(pTranslateCV1);
		Point2D endAnchorPoint = centerEnd.subtract(pTranslateCV2);

		start.setX(startAnchorPoint.getX());
		start.setY(startAnchorPoint.getY());

		end.setX(endAnchorPoint.getX());
		end.setY(endAnchorPoint.getY());

		Point2D betweenAnchors = endAnchorPoint.subtract(startAnchorPoint);
		double angleX = Math.acos(betweenAnchors.normalize().dotProduct(new Point2D(1, 0)));
		double angleY = Math.acos(betweenAnchors.normalize().dotProduct(new Point2D(0, 1)));

		angleX = Math.toDegrees(angleX);
		angleY = Math.toDegrees(angleY);

		aStart.setTranslateX(start.getX() - aStart.getWidth() / 2);
		aStart.setTranslateY(start.getY() - aStart.getHeight() / 2);

		aEnd.setTranslateX(end.getX() - aEnd.getWidth() / 2);
		aEnd.setTranslateY(end.getY() - aEnd.getHeight() / 2);

		LOG.info("Angle:" + angleX);
		if (angleY < 90) {
			aStart.setRotate(angleX);
			aEnd.setRotate(angleX + 180);
		} else {
			aStart.setRotate(-angleX);
			aEnd.setRotate(-angleX + 180);
		}

	}

	private Point2D computeCenterAnchorTranslation(Bounds cvBounds, Point2D betweenVector) {
		// FIXME when nodes are close together lines go through the nodes not
		// taking best anchor point
		double translationXCenter;
		double translationYCenter;

		double direction = 0;

		if (Math.abs(betweenVector.getX()) > Math.abs(betweenVector.getY())) {
			direction = (betweenVector.getX() > 0) ? 1 : -1;
			translationXCenter = direction * cvBounds.getWidth() / 2;
			translationYCenter = 0;
		} else {
			direction = (betweenVector.getY() > 0) ? 1 : -1;
			translationXCenter = 0;
			translationYCenter = direction * cvBounds.getHeight() / 2;
		}

		return new Point2D(translationXCenter, translationYCenter);
	}

	public void addLinkEditRequestedListener(LinkEditRequestedListener l) {
		linkEditListners.add(l);
	}

	public void addLinkDirectionUpdatedListener(LinkDirectionUpdatedListener l) {
		linkDirectionListener.add(l);
	}

	public void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		if (cv1.equals(cv) || cv2.equals(cv))
			updateLinkBetween();

	}

	public void inputClosed(User u) {
		// TODO implement input closed on link

	}

	public void anchorAltered(Anchor a) {
		Anchor b = (a.equals(aStart)) ? aEnd : aStart;

		LinkDirectionUpdatedListener.Direction d = Direction.NOT_DIRECTED;

		a.toggle();
		
		if (a.isDirected() && b.isDirected()) {
			//FIXME indicate that this is not possible on the UI!
			a.toCircle();
			return;
		} else if (aStart.isDirected() && !aEnd.isDirected())
			d = Direction.END_TO_START;
		else if (!aStart.isDirected() && aEnd.isDirected())
			d = Direction.START_TO_END;

		fireLinkDirectionUpdate(d);

	}

	private void fireLinkDirectionUpdate(LinkDirectionUpdatedListener.Direction d) {
		linkDirectionListener.forEach((l) -> l.linkDirectionUpdated(this, d, null));
	}

	public Concept getStart() {
		return cv1.getConcept();
	}

	public Concept getEnd() {
		return cv2.getConcept();
	}

}
