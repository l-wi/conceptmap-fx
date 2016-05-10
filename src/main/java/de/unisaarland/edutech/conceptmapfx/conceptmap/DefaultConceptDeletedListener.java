package de.unisaarland.edutech.conceptmapfx.conceptmap;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
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
		transition.getChildren().addAll(fadeTrans,scaleTrans);
		
		transition.setOnFinished((e) -> {
			conceptMapController.getView().remove(cv.getView());
			conceptMapController.getMap().removeConcept(cv.getConcept());
			conceptMapController.remove(cv);
		});

		transition.play();
	}

}
