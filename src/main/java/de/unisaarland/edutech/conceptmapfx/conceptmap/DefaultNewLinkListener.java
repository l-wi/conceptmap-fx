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
package de.unisaarland.edutech.conceptmapfx.conceptmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.datalogging.InteractionLogger;
import de.unisaarland.edutech.conceptmapfx.event.NewLinkListener;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable;
import de.unisaarland.edutech.conceptmapfx.link.LinkViewBuilder;
import de.unisaarland.edutech.conceptmapfx.link.LinkViewController;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import javafx.geometry.Point2D;

public class DefaultNewLinkListener implements NewLinkListener {

	private static final InteractionLogger INTERACTION_LOGGER = InteractionLogger.getInstance();

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

		LinkViewBuilder builder = new LinkViewBuilder(conceptMap, conceptMapPane, cv1, cv2);
		builder.withDirectionListener(controller).forNewLink().withDeletedListener(controller);

		controller.getInputControllers().forEach((l) -> {
			builder.withEditListener(l);
			builder.withDeletedListener(l);
		});
		LinkViewController lvc = builder.buildUndirectedAndAdd();
		controller.addLinkController(lvc);

		INTERACTION_LOGGER.newLinkData(cv1.getConcept(), cv2.getConcept(), lvc.getLink());

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

		// TODO CODE for debug printing that might be useful one time again.

		// Path debugPath = new Path();
		// debugPath.setStroke(javafx.scene.paint.Color.YELLOW);
		// debugPath.setStrokeWidth(3);
		// MoveTo debugMove = new MoveTo(translateX, translateY);
		// debugPath.getElements().add(debugMove);
		// this.controller.getView().getChildren().add(debugPath);

		final int circleStep = 12;
		for (double r = heightResting; r < 20 * heightResting; r += 0.25 * heightResting) {

			for (double i = 0; i < circleStep; i++) {

				double angle = 2 * Math.PI * i / circleStep;

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
				
				// TODO CODE for debug printing that might be useful one time
				// again.

				// LOG.warn("adding line at" + x + " / " + y);
				// LineTo debugLine = new LineTo(translateX + x,translateY + y);

				// debugPath.getElements().add(debugLine);
			}
		}

		LOG.warn("found no non-overlaping spot for node!");
	}

}
