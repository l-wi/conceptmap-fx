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
