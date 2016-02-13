package de.unisaarland.edutech.conceptmapfx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import de.unisaarland.edutech.conceptmapfx.InputViewController.Position;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.FocusQuestion;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ConceptMapViewTests extends ApplicationTest {

	private static final String FIRST_CONCEPT = "Dog";
	private ConceptMapViewController controller;
	private Pane conceptMapView;
	private ConceptMap map;

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

		Experiment experiment = new Experiment(u1, new FocusQuestion("How dare you?", u1));
		experiment.addParticipant(u1);
		experiment.addParticipant(u2);
		experiment.addParticipant(u3);
		experiment.addParticipant(u4);

		map = new ConceptMap(experiment);

		ConceptMapViewBuilder builder = new ConceptMapViewBuilder();
		scene = builder.withConceptMap(map).build();

		controller = builder.getController();

		conceptMapView = (AnchorPane) scene.getRoot();
		stage.setScene(scene);
		stage.setFullScreen(true);
		stage.show();

	}

	@Test
	public void testCreate() {
		// then
		Set<Node> inputs = conceptMapView.lookupAll(".input");
		assertEquals(4, inputs.size());
	}

	@Test
	public void testNewActionAndNotTwice() {
		// given
		Set<Node> newButtons = conceptMapView.lookupAll(".newBtn");
		Node firstNewButton = newButtons.iterator().next();

		// when
		moveTo(firstNewButton).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);

		// then
		Set<Node> concepts = conceptMapView.lookupAll(".concept");
		assertEquals(1, concepts.size());
		Node conceptParent = concepts.iterator().next();
		assertTrue(conceptParent.lookup("#caption").getStyleClass().contains("empty"));

		// when
		moveTo(firstNewButton).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);

		// then
		concepts = conceptMapView.lookupAll(".concept");
		assertEquals(1, concepts.size());
	}

	@Test
	public void testNewAndSelectKeyboard() {
		// given
		Set<Node> newButtons = conceptMapView.lookupAll(".newBtn");
		Node firstNewButton = newButtons.iterator().next();
		Node rightKeyboard = conceptMapView.lookup("#" + Position.RIGHT).lookup("#keyboard");
		Node leftKeyboard = conceptMapView.lookup("#" + Position.LEFT).lookup("#keyboard");
		;
		Node topKeyboard = conceptMapView.lookup("#" + Position.TOP).lookup("#keyboard");
		;
		Node bottomKeyboard = conceptMapView.lookup("#" + Position.BOTTOM).lookup("#keyboard");
		;

		assertTrue(rightKeyboard.isDisabled());
		assertTrue(leftKeyboard.isDisabled());
		assertTrue(topKeyboard.isDisabled());
		assertTrue(bottomKeyboard.isDisabled());

		// when
		moveTo(firstNewButton).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);

		Node concept = conceptMapView.lookup(".concept");
		moveTo(concept).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);

		Node rightToggle = concept.lookup("#fourUserEditable-rightToggle");
		moveTo(rightToggle).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);

		// then
		assertFalse(rightKeyboard.isDisabled());
		assertTrue(leftKeyboard.isDisabled());
		assertTrue(topKeyboard.isDisabled());
		assertTrue(bottomKeyboard.isDisabled());
	}

	@Test
	public void testLayoutWithConceptAdded() {
		int conceptCount = map.getConceptCount();

		Concept c = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(0), FIRST_CONCEPT));
		double x = 0.4;
		double y = 0.4;
		c.setX(x);
		c.setY(y);
		map.addConcept(c);

		// when
		super.interact(() -> {
			controller.setConceptMap(map);
			controller.layout();
		});

		//then
		
		Set<Node> concepts = conceptMapView.lookupAll(".concept");

		assertEquals(conceptCount + 1, concepts.size());
		assertEquals(conceptCount + 1, map.getConceptCount());
		Node addedConcept = concepts.iterator().next();

		double xScaled = addedConcept.getLayoutX() + addedConcept.getTranslateX();
		double yScaled = addedConcept.getLayoutY() + addedConcept.getTranslateY();

		assertEquals(x * scene.getWidth(), xScaled, 0.5);
		assertEquals(y * scene.getHeight(), yScaled, 0.5);

		Label caption = (Label) addedConcept.lookup("#caption");
		assertEquals(FIRST_CONCEPT, caption.getText());

	}

}
