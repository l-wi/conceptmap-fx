package de.unisaarland.edutech.conceptmapfx.conceptmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class DefaultConceptMovementListener implements ConceptMovedListener, ConceptMovingListener {

	private Map<ConceptViewController, List<ConceptViewController>> conceptToIntersectedConcepts = new HashMap<>();

	private ConceptMapViewController controller;


	public DefaultConceptMovementListener(ConceptMapViewController controller) {
		this.controller = controller;
	}

	@Override
	public void conceptMoved(ConceptViewController cv) {
		List<ConceptViewController> intersections = findIntersections(cv);

		FourUserTouchEditable view = cv.getView();

		double x = (view.getOrigin().getX() / controller.getWidth().get());
		double y = (view.getOrigin().getY() / controller.getHeight().get());
		double r = view.getRotate();

		cv.getConcept().setPosition(x, y, r);

		for (ConceptViewController intersected : intersections) {
			controller.fireNewLinkListener(cv, intersected);
		}

		conceptToIntersectedConcepts.getOrDefault(cv, Collections.emptyList()).forEach((e) -> {
			scaleDown(e);
		});

		scaleDown(cv);

		conceptToIntersectedConcepts.remove(cv);
	}

	public void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		List<ConceptViewController> intersectedCVs = findIntersections(cv);

		removeHighlightingForLinking(cv, intersectedCVs);

		addHighlightingForLinking(cv, intersectedCVs);

	}

	private void removeHighlightingForLinking(ConceptViewController cv, List<ConceptViewController> intersectedCVs) {

		List<ConceptViewController> formerIntersected = conceptToIntersectedConcepts.getOrDefault(cv,
				Collections.emptyList());

		ArrayList<ConceptViewController> difference = new ArrayList<>(formerIntersected);
		difference.removeAll(intersectedCVs);

		difference.forEach((e) -> {
			scaleDown(e);
		});

		if (intersectedCVs.size() == 0)
			scaleDown(cv);

	}

	private void addHighlightingForLinking(ConceptViewController cv, List<ConceptViewController> intersectedCVs) {
		List<ConceptViewController> formerIntersected = conceptToIntersectedConcepts.getOrDefault(cv,
				Collections.emptyList());

		ArrayList<ConceptViewController> difference = new ArrayList<>(intersectedCVs);
		difference.removeAll(formerIntersected);

		difference.forEach((e) -> {
			scaleUp(e);
		});

		if (intersectedCVs.size() > 0)
			scaleUp(cv);

		conceptToIntersectedConcepts.put(cv, intersectedCVs);
	}

	private List<ConceptViewController> findIntersections(ConceptViewController cv) {
		List<ConceptViewController> result = new ArrayList<>();

		for (ConceptViewController controller : controller.getConceptViewControllers()) {
			if (!cv.equals(controller) && cv.intersects(controller)) {
				result.add(controller);
			}
		}

		return result;

	}

	private void scaleUp(ConceptViewController e) {
		ScaleTransition st = new ScaleTransition(Duration.millis(300), e.getView());
		st.setToX(1.2);
		st.setToY(1.2);
		st.play();
	}

	private void scaleDown(ConceptViewController e) {
		ScaleTransition st = new ScaleTransition(Duration.millis(300), e.getView());
		st.setToX(1);
		st.setToY(1);
		st.play();
	}
}
