package de.unisaarland.edutech.conceptmapfx.conceptmap;

import de.unisaarland.edutech.conceptmapfx.event.AlignListener;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable;
import javafx.animation.RotateTransition;
import javafx.scene.CacheHint;
import javafx.util.Duration;

public class DefaultAlignListener implements AlignListener {

	private static final int ROTATE_DURATION = 500;
	ConceptMapViewController controller;
	private int rotate;

	public DefaultAlignListener(ConceptMapViewController controller) {
		this.controller = controller;
	}

	@Override
	public void align() {

		rotate = (rotate + 90) % 360;
		controller.getConceptViewControllers().forEach(c -> {
			FourUserTouchEditable view = c.getView();
			view.setCache(true);
			view.setCacheHint(CacheHint.SPEED);
			RotateTransition transition = new RotateTransition(Duration.millis(ROTATE_DURATION), view);
			transition.setOnFinished((e) -> {
				c.rotateAbsolute(rotate);
				controller.getLinkControllers().forEach(l -> l.rotateAbsolute(rotate));
				view.setCache(false);
			});
			transition.setToAngle(rotate);
			transition.play();
		});

	}

}
