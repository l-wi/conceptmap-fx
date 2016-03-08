package de.unisaarland.edutech.conceptmapfx.conceptmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.FourUserTouchEditable;
import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.event.NewLinkListener;
import de.unisaarland.edutech.conceptmapfx.link.LinkViewBuilder;
import de.unisaarland.edutech.conceptmapfx.link.LinkViewController;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import javafx.geometry.Point2D;

public class DefaultNewLinkListener implements NewLinkListener {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultNewLinkListener.class);

	private ConceptMapViewController controller;

	public DefaultNewLinkListener(ConceptMapViewController controller) {
		this.controller = controller;
	}

	@Override
	public void newLink(ConceptViewController cv1, ConceptViewController cv2) {
		moveToFreeSpot(cv1, cv2);

		ConceptMap conceptMap = controller.getMap();
		ConceptMapView conceptMapPane = controller.getView();

		if (conceptMap.isAnyLinkExisting(cv1.getConcept(), cv2.getConcept())) {
			LOG.warn("there is already a link between:" + cv1.getConcept() + " and " + cv2.getConcept());
			return;
		}

		LOG.info("adding new link between:\t" + cv1.getConcept().getName().getContent() + " <-> "
				+ cv2.getConcept().getName().getContent());

		LinkViewBuilder builder = new LinkViewBuilder(conceptMap, conceptMapPane, cv1, cv2);
		builder.withDirectionListener(controller).forNewLink().withDeletedListener(controller);

		controller.getInputControllers().forEach((l) -> {
			builder.withEditListener(l);
			builder.withDeletedListener(l);
		});
		LinkViewController lvc = builder.buildUndirectedAndAdd();
		controller.addLinkController(lvc);

	}

	private void moveToFreeSpot(ConceptViewController cvToMove, ConceptViewController cvResting) {
		FourUserTouchEditable viewToMove = cvToMove.getView();
		double widthToMove = viewToMove.getWidth();
		double heightToMove = viewToMove.getHeight();
		double xToMove = viewToMove.getLayoutX() + viewToMove.getTranslateX() + widthToMove / 2;
		double yToMove = viewToMove.getLayoutY() + viewToMove.getTranslateY() + heightToMove / 2;
		Point2D pMove = new Point2D(xToMove, yToMove);

		FourUserTouchEditable viewResting = cvResting.getView();
		double widthResting = viewResting.getWidth();
		double heightResting = viewResting.getHeight();
		double xResting = viewResting.getLayoutX() + viewResting.getTranslateX() + widthResting / 2;
		double yResting = viewResting.getLayoutY() + viewResting.getTranslateY() + heightResting / 2;

		Point2D pResting = new Point2D(xResting, yResting);

		Point2D pDelta = pMove.subtract(pResting).normalize();

		double translateX = viewToMove.getTranslateX();
		double translateY = viewToMove.getTranslateY();

		for (double r = heightResting; r < 20 * heightResting; r++) {
			for (double i = 0; i < 300; i++) {

				double angle = 2 * Math.PI * i / 300;

				double x = pDelta.getX() * Math.cos(angle) - pDelta.getY() * Math.sin(angle);
				double y = pDelta.getX() * Math.sin(angle) + pDelta.getY() * Math.cos(angle);

				x = x * r;
				y = y * r;

				viewToMove.setTranslateX(translateX + x);
				viewToMove.setTranslateY(translateY + y);

				if (!controller.hasIntersections(viewToMove)) {
					controller.getLinkControllers().forEach((l) -> l.layout());
					return;
				}

			}
		}

		LOG.warn("found no non-overlaping spot for node!");
	}

}
