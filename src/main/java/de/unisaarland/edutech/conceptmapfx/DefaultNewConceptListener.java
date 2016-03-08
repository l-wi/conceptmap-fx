package de.unisaarland.edutech.conceptmapfx;

import de.unisaarland.edutech.conceptmapfx.event.NewConceptListener;
import de.unisaarland.edutech.conceptmapping.User;

public class DefaultNewConceptListener implements NewConceptListener {

	private ConceptViewBuilder conceptViewBuilder;
	private ConceptMapViewController mapController;

	public DefaultNewConceptListener(ConceptViewBuilder builder, ConceptMapViewController mapController) {
		this.conceptViewBuilder = builder;
		this.mapController = mapController;

	}

	@Override
	public void newConcept(InputViewController controller) {

		User user = controller.getUser();

		conceptViewBuilder.withNewConcept(user);

		conceptViewBuilder.withConceptEmptyListener(controller);

		ConceptViewController cv = conceptViewBuilder.buildControllerAndAddView(controller,
				this.mapController.getView());

		updateConceptPosition(cv);

		this.mapController.add(cv);
	}

	private void updateConceptPosition(ConceptViewController cv) {
		double x = cv.getView().getTranslateX() / this.mapController.getWidth().doubleValue();
		double y = cv.getView().getTranslateY() / this.mapController.getHeight().doubleValue();
		double r = cv.getView().getRotate();

		cv.getConcept().setPosition(x, y, r);
	}

}
