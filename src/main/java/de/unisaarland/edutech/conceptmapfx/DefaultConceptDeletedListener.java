package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;

public class DefaultConceptDeletedListener implements ConceptDeletedListener {

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
		conceptMapController.getView().remove(cv.getView());
		conceptMapController.getMap().removeConcept(cv.getConcept());
		conceptMapController.remove(cv);
	}

}
