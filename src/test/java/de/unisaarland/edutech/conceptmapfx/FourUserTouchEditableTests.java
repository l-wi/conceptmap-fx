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
package de.unisaarland.edutech.conceptmapfx;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable.State;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class FourUserTouchEditableTests extends ApplicationTest {

	private FourUserTouchEditable root;
	private static Scene scene = null;

	@Override
	public void start(Stage stage) {
		AnchorPane pane = new AnchorPane();
		root = new FourUserTouchEditable();
		root.setText("This is the test environment");

		root.setLayoutX(200);
		root.setLayoutY(200);

		pane.getChildren().add(root);
		scene = new Scene(pane, 800, 600);
		stage.setScene(scene);
		stage.show();
	}

	@Test
	public void testMouseClickOnEditable() {
		// when
		clickOn(root);
		sleep(500);
		// then
		assertTrue(root.getState() == State.SELECTED);

		clickOn(root);
		sleep(500);

		assertTrue(root.getState() == State.UNSELECTED);
	}

	@Test
	public void testMouseShowRotateEditable()  {
		// when
		press(MouseButton.PRIMARY).sleep(3000);

		// then
		assertTrue(root.getState() == State.ROTATING);

		release(MouseButton.PRIMARY);
	}
}
