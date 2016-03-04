package de.unisaarland.edutech.conceptmapfx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import de.unisaarland.edutech.conceptmapfx.FourUserTouchEditable.State;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ConceptViewTests extends ApplicationTest {

	private static final String FIRST_CONCEPT = "Dog";
	private ConceptViewController cv1;
	private Pane conceptView1;

	private static Scene scene = null;

	@Override
	public void start(Stage stage) throws IOException {
		List<User> p = new ArrayList<User>();

		User u1 = new User("alfred", "a@test.de");
		User u2 = new User("björn", "b@test.de");
		User u3 = new User("klaus", "k@test.de");
		User u4 = new User("lukas", "l@test.de");

		p.add(u1);
		p.add(u2);
		p.add(u3);
		p.add(u4);

		Concept c1 = new Concept(new CollaborativeString(u1, FIRST_CONCEPT));

		AnchorPane pane = new AnchorPane();

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConceptView.fxml"));
		conceptView1 = loader.load();
		cv1 = loader.getController();
		cv1.setParticipants(p);
		cv1.setConcept(c1);

		conceptView1.setLayoutX(400);
		conceptView1.setLayoutY(400);

		pane.getChildren().add(conceptView1);

		scene = new Scene(pane, 800, 600);
		stage.setScene(scene);
		stage.show();
	}

	@Test
	public void testConceptInitialization() {
		// when
		Label caption = (Label) conceptView1.lookup("#caption");

		// then
		assertEquals(FIRST_CONCEPT, caption.getText());
		assertEquals(FIRST_CONCEPT, cv1.getConcept().getName().getContent());
	}

	@Test
	public void testMoving() {
		// when
		int translateX = 100;
		int translateY = 100;
		double expectedX = conceptView1.getLayoutX() + conceptView1.getTranslateX() + translateX;
		double expectedY = conceptView1.getLayoutY() + conceptView1.getTranslateY() + translateY;

		// when
		moveTo(conceptView1).press(MouseButton.PRIMARY).moveBy(100, 100).release(MouseButton.PRIMARY);

		// then
		double deltaX = Math.abs(expectedX - (conceptView1.getLayoutX() + conceptView1.getTranslateX()));
		double deltaY = Math.abs(expectedY - (conceptView1.getLayoutY() + conceptView1.getTranslateY()));

		assertEquals(deltaX, 0, 5);
		assertEquals(deltaY, 0, 5);

	}

	@Test
	public void testMoveNoRotate() {
		// given
		FourUserTouchEditable control = cv1.getView();

		// when
		moveTo(conceptView1).press(MouseButton.PRIMARY).moveBy(30, 30).sleep(3000);

		// then
		assertTrue(control.getState() == State.MOVING);

		release(MouseButton.PRIMARY);

		assertTrue(control.getState() == State.UNSELECTED);
	}

	@Test
	public void testSelectNoMove() {
		// given
		FourUserTouchEditable view = cv1.getView();

		int translateX = 100;
		int translateY = 10;
		double x = view.getLayoutX() + view.getTranslateX();
		double y = view.getLayoutY() + view.getTranslateY();

		// when
		clickOn(conceptView1);
		assertTrue(view.getState() == State.SELECTED);

		press(MouseButton.PRIMARY).moveBy(translateX, translateY).release(MouseButton.PRIMARY);

		// then
		assertEquals(x, view.getLayoutX() + view.getTranslateX(), 0);
		assertEquals(y, view.getLayoutY() + view.getTranslateY(), 0);
	}

	@Test
	public void testRotate() {
		// given

		int scrollAmount = 25;
		// when
		moveTo(conceptView1).press(MouseButton.PRIMARY).sleep(2000).scroll(scrollAmount).release(MouseButton.PRIMARY);

		// then
		assertEquals(-scrollAmount, conceptView1.getRotate(), 3);
	}

	@Test
	public void testRotateNoMove() {
		// given
		FourUserTouchEditable view = cv1.getView();

		int scrollAmount = 25;
		int translateX = 100;
		int translateY = 10;
		double x = view.getLayoutX() + view.getTranslateX();
		double y = view.getLayoutY() + view.getTranslateY();

		// when
		moveTo(conceptView1).press(MouseButton.PRIMARY).sleep(2000).scroll(scrollAmount).moveBy(translateX, translateY)
				.release(MouseButton.PRIMARY);

		// then
		assertEquals(x, view.getLayoutX() + view.getTranslateX(), 0);
		assertEquals(y, view.getLayoutY() + view.getTranslateY(), 0);
	}

	@Test
	public void testRotateWithoutRotationMode(){
		//given 
		int scrollAmount = 25;
		double rotate = conceptView1.getRotate();
		
		//when
		moveTo(conceptView1).scroll(scrollAmount);
		
		//then
		assertEquals(rotate, conceptView1.getRotate(),0);
	}
}
