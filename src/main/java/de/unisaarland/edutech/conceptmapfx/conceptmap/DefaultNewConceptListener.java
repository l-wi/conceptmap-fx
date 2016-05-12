package de.unisaarland.edutech.conceptmapfx.conceptmap;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewBuilder;
import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.datalogging.InteractionLogger;
import de.unisaarland.edutech.conceptmapfx.event.NewConceptListener;
import de.unisaarland.edutech.conceptmapfx.input.InputViewController;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class DefaultNewConceptListener implements NewConceptListener {

	private static final InteractionLogger INTERACTION_LOGGER = InteractionLogger.getInstance();
	
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

		// conceptViewBuilder.withConceptEmptyListener(controller);

		ConceptViewController cv = conceptViewBuilder.buildControllerAndAddView(controller,
				this.mapController.getView());

		updateConceptPosition(cv);

		this.mapController.add(cv);

		INTERACTION_LOGGER.newConceptData(cv.getConcept(), user);

		animateNew(cv);
	}

	private void animateNew(ConceptViewController cv) {
		FadeTransition fadeTrans = new FadeTransition(Duration.millis(300), cv.getView());
		fadeTrans.setFromValue(0);
		fadeTrans.setToValue(1);
		ScaleTransition scaleTrans = new ScaleTransition(Duration.millis(300), cv.getView());
		scaleTrans.setFromX(0);
		scaleTrans.setFromY(0);
		scaleTrans.setToX(1);
		scaleTrans.setToY(1);

		ParallelTransition transition = new ParallelTransition();
		transition.getChildren().addAll(fadeTrans, scaleTrans);

		transition.play();
	}

	private void updateConceptPosition(ConceptViewController cv) {
		double x = cv.getView().getTranslateX() / this.mapController.getWidth().doubleValue();
		double y = cv.getView().getTranslateY() / this.mapController.getHeight().doubleValue();
		double r = cv.getView().getRotate();

		cv.getConcept().setPosition(x, y, r);
	}

}
