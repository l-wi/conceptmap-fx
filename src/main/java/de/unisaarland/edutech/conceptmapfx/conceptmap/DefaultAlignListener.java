package de.unisaarland.edutech.conceptmapfx.conceptmap;

import de.unisaarland.edutech.conceptmapfx.event.AlignListener;

public class DefaultAlignListener implements AlignListener {

	ConceptMapViewController controller;
	private int rotate;
	
	public DefaultAlignListener(ConceptMapViewController controller) {
		this.controller= controller;
	}
	
	@Override
	public void align() {
		rotate = (rotate + 90) % 360;
		controller.getConceptViewControllers().forEach( c -> c.rotateAbsolute(rotate));

		controller.getLinkControllers().forEach( l -> l.rotateAbsolute(rotate));
	}

}
