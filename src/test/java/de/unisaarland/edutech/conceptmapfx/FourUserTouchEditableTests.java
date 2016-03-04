package de.unisaarland.edutech.conceptmapfx;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import de.unisaarland.edutech.conceptmapfx.FourUserTouchEditable.State;
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

		// then
		assertTrue(root.getState() == State.SELECTED);

		clickOn(root);

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
