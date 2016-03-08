package de.unisaarland.edutech.conceptmapfx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import de.unisaarland.edutech.conceptmapfx.InputViewController.Position;
import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapView;
import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapViewBuilder;
import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapViewController;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptFactory;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.FocusQuestion;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

public class ConceptMapViewControllerTests extends ApplicationTest {

	private static final String THIRD_CONCEPT = "hamster";
	private static final String FIRST_CONCEPT = "Dog";
	private static final String SECOND_CONCEPT = "Cat";
	private ConceptMapViewController controller;
	private ConceptMapView conceptMapView;
	private ConceptMap map;

	private static Scene scene = null;
	private Stage stage;

	@Override
	public void start(Stage stage) throws IOException {
		this.stage = stage;
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
		ObservableConceptFactory conceptFactory = new ObservableConceptFactory();
		ConceptViewBuilder conceptBuilder = new ConceptViewBuilder(map, conceptFactory);

		
		
		scene = builder.withConceptViewBuilder(conceptBuilder).withConceptMap(map).build();

		controller = builder.getController();

		conceptMapView = (ConceptMapView) scene.getRoot();
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
		Set<Node> newButtons = conceptMapView.lookupAll(".newBtnTop");
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
		Set<Node> newButtons = conceptMapView.lookupAll(".newBtnTop");
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
		sleep(500);
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
		c.setPosition(x,y,0);

		map.addConcept(c);

		// when
		super.interact(() -> {
			controller.setConceptMap(map);
			controller.layout();
		});

		// then

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

	@Test
	public void testLink() {
		// given
		map.clear();

		Concept c1 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(2), FIRST_CONCEPT));
		c1.setPosition(0.8,0.5,0);

		Concept c2 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(1), SECOND_CONCEPT));
		c2.setPosition(0.5,0.3,30);


		map.addConcept(c1);
		map.addConcept(c2);

		super.interact(() -> {
			controller.setConceptMap(map);
			controller.layout();
		});

		Set<Node> concepts = conceptMapView.lookupAll(".concept");

		Iterator<Node> iterator = concepts.iterator();

		Node firstConceptView = iterator.next();
		Node secondConceptView = iterator.next();

		// when
		moveTo(firstConceptView).press(MouseButton.PRIMARY).moveTo(secondConceptView).release(MouseButton.PRIMARY);

		// then
		assertTrue(map.isAnyLinkExisting(c1, c2));
		assertFalse(firstConceptView.getBoundsInParent().intersects(secondConceptView.getBoundsInParent()));

	}

	@Test
	public void testRotateLinkCaption() {
		this.map.clear();

		User user = map.getExperiment().getParticipants().get(2);
		Concept c1 = new Concept(new CollaborativeString(user, FIRST_CONCEPT));
		c1.setPosition(0.2,0.5,0);


		Concept c2 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(1), SECOND_CONCEPT));
		c2.setPosition(0.4,0.4,0);


		map.addConcept(c1);
		map.addConcept(c2);

		map.addUndirectedLink(c1, c2).getCaption().append(user, "hunts");

		interact(() -> {
			controller.setConceptMap(map);
			controller.layout();
		});

		Node linkCaption = conceptMapView.lookup(".link");

		double rotate = linkCaption.getRotate();
		int scrollAmount = 100;

		// when
		moveTo(linkCaption).sleep(1000).press(MouseButton.PRIMARY).sleep(5000).scroll(scrollAmount)
				.release(MouseButton.PRIMARY);

		// then
		assertEquals(rotate + 180, linkCaption.getRotate(), 0.0);

	}

	@Test
	public void testRotateLinkLabelMoveConceptRotationPersists() {
		// given
		testRotateLinkCaption();

		Node linkCaption = conceptMapView.lookup(".link");
		Node concept = conceptMapView.lookup(".concept");
		double rotate = linkCaption.getRotate();

		// when
		moveTo(concept).press(MouseButton.PRIMARY).moveBy(2, 0).release(MouseButton.PRIMARY);

		// then
		assertEquals(rotate, linkCaption.getRotate(), 20.0);

	}

	@Test
	public void testNoDuplicateLinkOnLayoutUndirected() {
		this.map.clear();

		Concept c1 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(2), FIRST_CONCEPT));
		c1.setPosition(0.3,0.3,0);

		Concept c2 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(1), SECOND_CONCEPT));
		c1.setPosition(0.6,0.6,30);

		map.addConcept(c1);
		map.addConcept(c2);

		map.addUndirectedLink(c1, c2);

		// when

		interact(() -> {
			controller.setConceptMap(map);
			controller.layout();
		});

		Set<Node> linkCaptions = conceptMapView.lookupAll(".link");

		assertEquals(1, linkCaptions.size());

	}

	@Test
	public void testConceptDelete() {
		// given
		map.clear();

		Concept c1 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(2), FIRST_CONCEPT));
		c1.setPosition(0.5,0.5,0);

		Concept c2 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(1), SECOND_CONCEPT));
		c1.setPosition(0.7,0.7,30);


		map.addConcept(c1);
		map.addConcept(c2);
		map.addUndirectedLink(c1, c2);

		super.interact(() -> {
			controller.setConceptMap(map);
			controller.layout();
		});

		Set<Node> concepts = conceptMapView.lookupAll(".concept");

		Iterator<Node> iterator = concepts.iterator();

		Node firstConceptView = iterator.next();

		// when
		moveTo(firstConceptView).doubleClickOn(MouseButton.PRIMARY);

		// then
		concepts = conceptMapView.lookupAll(".concept");

		iterator = concepts.iterator();

		assertEquals(map.getConceptCount(), 1);
		assertEquals(c2, map.getConcept(0));
		assertEquals(concepts.size(), 1);

	}

	@Test
	public void testLinkDelete() {
		// given
		map.clear();

		Concept c1 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(2), FIRST_CONCEPT));
		c1.setPosition(0.5,0.5,0);


		Concept c2 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(1), SECOND_CONCEPT));
		c2.setPosition(0.7,0.7,30);


		Concept c3 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(3), THIRD_CONCEPT));
		c3.setPosition(0.7,0.2,0);


		map.addConcept(c1);
		map.addConcept(c2);
		map.addConcept(c3);

		map.addUndirectedLink(c1, c2);
		map.addDirectedLink(c1, c3);

		super.interact(() -> {
			controller.setConceptMap(map);
			controller.layout();
		});

		Set<Node> concepts = conceptMapView.lookupAll(".concept");
		Set<Node> links = conceptMapView.lookupAll(".linkPath");

		// when
		for (Node link : links)
			moveTo(link).doubleClickOn(MouseButton.PRIMARY);

		// then
		concepts = conceptMapView.lookupAll(".concept");

		assertEquals(3, map.getConceptCount());
		assertEquals(3, concepts.size());
		assertNull(conceptMapView.lookup(".link"));
		assertNull(map.getLink(c1, c2));
		assertNull(map.getLink(c2, c1));
		assertNull(map.getLink(c1, c3));

	}

	@Test
	public void testBugSecondNewNotShown(){
		//given
		Set<Node> newButtons = conceptMapView.lookupAll(".newBtnTop");
		Node firstNewButton = newButtons.iterator().next();

		//when
		moveTo(firstNewButton).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);
		Node concept = conceptMapView.lookup(".concept");
		moveTo(concept).doubleClickOn(MouseButton.PRIMARY);
		assertSame(0,conceptMapView.lookupAll(".concept").size());
		moveTo(firstNewButton).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);
		
		//then
		assertSame(1,conceptMapView.lookupAll(".concept").size());
	}
	
	@Test
	public void testDelete(){
		//given
		map.clear();

		Concept c1 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(2), FIRST_CONCEPT));
		c1.setPosition(0.5,0.5,0);

		map.addConcept(c1);
		
		super.interact(() -> {
			controller.setConceptMap(map);
			controller.layout();
		});
		
		
		Node concept = conceptMapView.lookup(".concept");

		//when 
		moveTo(concept).doubleClickOn(MouseButton.PRIMARY);
		
		Set<Node> concepts = conceptMapView.lookupAll(".concept");
		
		//then
		assertSame(0,concepts.size());

	}
	@Test
	public void testDisableNew(){
		//given
		Set<Node> newButtons = conceptMapView.lookupAll(".newBtnTop");
		Node firstNewButton = newButtons.iterator().next();

		//when
		moveTo(firstNewButton).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);	
		
		assertTrue(firstNewButton.isDisabled());
		
		//Delete Concept again
		Node concept = conceptMapView.lookup(".concept");
		moveTo(concept).doubleClickOn(MouseButton.PRIMARY);
		
		assertFalse(firstNewButton.isDisabled());
		
		moveTo(firstNewButton).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);
		concept = conceptMapView.lookup(".concept");

		assertTrue(firstNewButton.isDisabled());

		moveTo(concept).clickOn(MouseButton.PRIMARY);
		
		// right Keyboard
		Node rightToggle = concept.lookup("#fourUserEditable-topToggle");
		moveTo(rightToggle).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);

		//press a key
		moveTo(1200,140).clickOn(MouseButton.PRIMARY);
		
		assertFalse(firstNewButton.isDisabled());

	}
	
	@Test
	public void testLinkAnchorSelection(){
		//given
		map.clear();

		Concept c1 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(2), FIRST_CONCEPT));
		c1.setPosition(0.5,0.5,0);


		Concept c2 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(1), SECOND_CONCEPT));
		c2.setPosition(0.3,0.5,30);


		Concept c3 = new Concept(new CollaborativeString(map.getExperiment().getParticipants().get(3), THIRD_CONCEPT));
		c3.setPosition(0.7,0.2,0);

		map.addConcept(c1);
		map.addConcept(c2);
		map.addConcept(c3);

		map.addDirectedLink(c1, c2);
		map.addUndirectedLink(c2, c3);

		map.addDirectedLink(c1, c3);
				
		super.interact(() -> {
			controller.setConceptMap(map);
			controller.layout();
		});
		
		Set<Node> links = conceptMapView.lookupAll(".linkPath");
		
		Iterator<Node> iterator = links.iterator();
		Node link1 = iterator.next();
		Node link2 = iterator.next();
		
		//when
		moveTo(link1).clickOn(MouseButton.PRIMARY);
		
		ObservableList<Node> children = conceptMapView.getChildren();
		Node lookupAnchor = children.get(children.size()-1);
		
		moveTo(lookupAnchor).clickOn(MouseButton.PRIMARY);
		
		moveTo(link1).clickOn(MouseButton.PRIMARY);
		
		//select second link
		moveTo(link2).clickOn(MouseButton.PRIMARY);
		
		children = conceptMapView.getChildren();
		lookupAnchor = children.get(children.size()-1);
		
		moveTo(lookupAnchor).clickOn(MouseButton.PRIMARY);
		
		assertSame(0, conceptMapView.lookupAll(".anchorPolygon").size());
		
	}
	
	@Test
	public void testConceptNewJump(){
		//given
		Set<Node> newButtons = conceptMapView.lookupAll(".newBtnTop");
		Node firstNewButton = newButtons.iterator().next();

		moveTo(firstNewButton).press(MouseButton.PRIMARY).release(MouseButton.PRIMARY);
		Node concept = conceptMapView.lookup(".concept");
		
		double expectedX = concept.getLayoutX()+ concept.getTranslateX();
		double expectedY = concept.getLayoutY()+ concept.getTranslateY();
		
		//when
		
		super.interact(() -> {
			stage.setFullScreen(false);
			stage.setWidth(stage.getWidth()-40);
			stage.setHeight(stage.getHeight()-40);
		});
		
		//then
		
		double actualX = concept.getLayoutX()+ concept.getTranslateX();
		double actualY = concept.getLayoutY()+ concept.getTranslateY();
		
		assertEquals(expectedX, actualX,50 );
		assertEquals(expectedY, actualY,50 );

	}
	
	// TODO test undo logic
	/*
	 * 1. create map with some concepts by using UI
	 * 2. undo all
	 * 3. create new concept
	 * 4. remove new concept
	 * 5. undo
	 */

	// TODO too many touch points reported exception kills whole multitouch
	// process, find out if this is a javafx bug and how to fix i


}
