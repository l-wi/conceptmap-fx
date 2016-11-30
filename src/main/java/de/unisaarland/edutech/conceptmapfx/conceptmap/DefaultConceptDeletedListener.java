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

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.datalogging.InteractionLogger;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.link.LinkViewController;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class DefaultConceptDeletedListener implements ConceptDeletedListener {

	private static final int FADE_DURATION = 300;
	private ConceptMapViewController conceptMapController;

	private static final InteractionLogger INTERACTION_LOGGER = InteractionLogger.getInstance();

	public DefaultConceptDeletedListener(ConceptMapViewController controller) {
		this.conceptMapController = controller;
	}

	@Override
	public void conceptDeleted(ConceptViewController cv, User u) {
		Concept concept = cv.getConcept();

		List<LinkViewController> workingList = new ArrayList<>(conceptMapController.getLinkControllers());

		for (LinkViewController l : workingList) {
			if (l.getStart().equals(concept) || l.getEnd().equals(concept)) {
				l.remove();
			}
		}

		deleteConcept(cv);
	}

	private void deleteConcept(ConceptViewController cv) {
		FadeTransition fadeTrans = new FadeTransition(Duration.millis(FADE_DURATION), cv.getView());
		fadeTrans.setFromValue(1);
		fadeTrans.setToValue(0);
		ScaleTransition scaleTrans = new ScaleTransition(Duration.millis(FADE_DURATION), cv.getView());
		scaleTrans.setToX(0);
		scaleTrans.setToY(0);

		ParallelTransition transition = new ParallelTransition();
		transition.getChildren().addAll(fadeTrans, scaleTrans);

		transition.setOnFinished((e) -> {
			conceptMapController.getView().remove(cv.getView());
			conceptMapController.getMap().removeConcept(cv.getConcept());
			conceptMapController.remove(cv);
			INTERACTION_LOGGER.deleteConceptData(cv.getConcept());
		});

		transition.play();
	}

}
