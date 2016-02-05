package de.unisaarland.edutech.conceptmapfx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.InputViewController.Position;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewConceptListener;
import de.unisaarland.edutech.conceptmapfx.event.NewLinkListener;
import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class ConceptMapViewController implements NewLinkListener, NewConceptListener, LinkDeletedListener,
		ConceptDeletedListener, ConceptMovedListener, LinkDirectionUpdatedListener {

	private static final Logger LOG = LoggerFactory.getLogger(ConceptMapViewController.class);

	private List<ConceptDeletedListener> conceptDeletedListners = new ArrayList<ConceptDeletedListener>();
	private List<LinkDeletedListener> linkDeletedListeners = new ArrayList<LinkDeletedListener>();
	private List<NewLinkListener> newLinkListeners = new ArrayList<NewLinkListener>();

	@FXML
	private AnchorPane conceptMapPane;

	private ConceptMap conceptMap;
	private List<InputViewController> inputControllers = new ArrayList<InputViewController>();
	private List<LinkViewController> linkControllers = new ArrayList<LinkViewController>();
	private Map<User, List<ConceptViewController>> userToConceptViewControllers = new HashMap<>();

	public void addConceptDeletedListener(ConceptDeletedListener l) {
		conceptDeletedListners.add(l);
	}

	public void addInputViewController(InputViewController inputController) {
		inputControllers.add(inputController);
	}

	public void addLinkDeletedListener(LinkDeletedListener l) {
		linkDeletedListeners.add(l);
	}

	public void addNewLinkListener(NewLinkListener l) {
		newLinkListeners.add(l);
	}

	public void conceptDeleted(ConceptViewController cv, User u) {
		// TODO implement concept deleted

	}

	public void linkDeleted(LinkViewController lv, User u) {
		// TODO implement link deleted

	}

	@FXML
	public void initialize() {
		// TODO load all the things that might already exit in that concept map
		// to UI
	}

	public void newConcept(InputViewController inputViewController) {
		try {

			// check if there is already an empty concept for the user
			User user = inputViewController.getUser();
			ConceptViewController emptyConceptViewController = getEmptyConceptViewController(user);

			if (emptyConceptViewController != null) {
				LOG.warn("there is still already an empty concept for user" + user);
				emptyConceptViewController.highlightEmpty();
				return;
			}

			newConceptInUIAndLogic(inputViewController, user);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void newLink(ConceptViewController cv1, ConceptViewController cv2) {
		if (conceptMap.isAnyLinkExisting(cv1.getConcept(), cv2.getConcept())) {
			LOG.warn("there is already a link between:" + cv1.getConcept() + " and " + cv2.getConcept());
			return;
		}

		LOG.info("adding new link between:\t" + cv1.getConcept().getName().getContent() + " <-> "
				+ cv2.getConcept().getName().getContent());

		Link link = conceptMap.addUndirectedLink(cv1.getConcept(), cv2.getConcept());

		LinkViewController controller = new LinkViewController(link, this.conceptMap.getExperiment().getParticipants(),
				this.conceptMapPane, cv1, cv2);

		cv1.addConceptMovingListener(controller);
		cv2.addConceptMovingListener(controller);

		linkControllers.add(controller);

		inputControllers.forEach(l -> {
			controller.addLinkEditRequestedListener(l);
		});

		controller.addLinkDirectionUpdatedListener(this);

	}

	public void setConceptMap(ConceptMap conceptMap) {
		this.conceptMap = conceptMap;
		for (User u : conceptMap.getExperiment().getParticipants())
			userToConceptViewControllers.put(u, new ArrayList<ConceptViewController>());
	}

	private ConceptViewController getEmptyConceptViewController(User user) {
		for (ConceptViewController controller : userToConceptViewControllers.get(user))
			if (controller.getConcept().getName().getContent().equals(""))
				return controller;
		return null;
	}

	private Concept initConcept(InputViewController inputViewController) {
		User owner = inputViewController.getUser();
		Concept concept = new Concept(new CollaborativeString(owner));
		conceptMap.addConcept(concept);
		return concept;
	}

	private ConceptViewController initConceptViewController(Concept concept, FXMLLoader loader) {
		ConceptViewController conceptViewController = loader.getController();

		conceptViewController.setConcept(concept);
		conceptViewController.setParticipants(conceptMap.getExperiment().getParticipants());

		for (InputViewController inputController : inputControllers) {
			conceptViewController.addConceptEditRequestedListener(inputController);
		}
		conceptViewController.addConceptMovedListener(this);

		return conceptViewController;
	}

	private void initConceptViewUI(InputViewController inputViewController, FXMLLoader loader) throws IOException {

		Pane conceptViewPane = loader.load();
		// force the conceptMapPane to calculate the size of the conceptViewPane
		conceptMapPane.getChildren().add(conceptViewPane);
		conceptMapPane.applyCss();
		conceptMapPane.layout(); // TODO load all the things that might already
									// exit in that concept map
		// to UI

		moveConceptToRightPosition(inputViewController, conceptViewPane);
	}

	private void moveConceptToRightPosition(InputViewController inputViewController, Pane conceptViewPane) {
		// TODO what if current space is occupied

		Point2D p = new Point2D(0, -50);
		Point2D pScene = inputViewController.transformLocalToScene(p);

		Pane n = conceptViewPane;

		double x = pScene.getX();
		double y = pScene.getY();

		if (inputViewController.getPosition() == Position.NORTH) {
			x = x - n.getWidth();
		} else if (inputViewController.getPosition() == Position.WEST) {
			x = x - n.getWidth() / 2 + n.getHeight() / 2;
			y = y + n.getWidth() / 2 - n.getHeight() / 2;
		} else if (inputViewController.getPosition() == Position.EAST) {
			x = x - n.getWidth() / 2 - n.getHeight() / 2;
			y = y - n.getWidth() / 2 - n.getHeight() / 2;
		} else if (inputViewController.getPosition() == Position.SOUTH) {
			y = y - n.getHeight();
		}

		conceptViewPane.setTranslateX(x);
		conceptViewPane.setTranslateY(y);

		conceptViewPane.setRotate(inputViewController.getRotate());

	}

	private void newConceptInUIAndLogic(InputViewController inputViewController, User user) throws IOException {
		// init logic
		Concept concept = initConcept(inputViewController);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("ConceptView.fxml"));

		// load and init UI
		initConceptViewUI(inputViewController, loader);

		// init UI event-logic
		ConceptViewController conceptViewController = initConceptViewController(concept, loader);
		requestInput(concept.getOwner(), conceptViewController);

		this.userToConceptViewControllers.get(user).add(conceptViewController);
	}

	private void requestInput(User owner, ConceptViewController conceptViewController) {
		LOG.info("requesting input for user:\t" + owner + " on new concept!");
		conceptViewController.setUserEnabled(owner, true);
	}

	@Override
	public void conceptMoved(ConceptViewController cv) {
		List<ConceptViewController> intersections = findIntersections(cv);

		for (ConceptViewController intersected : intersections) {
			fireNewLinkListener(cv, intersected);
		}

	}

	private void fireNewLinkListener(ConceptViewController cv, ConceptViewController intersected) {
		newLinkListeners.forEach(l -> l.newLink(cv, intersected));
	}

	private List<ConceptViewController> findIntersections(ConceptViewController cv) {
		List<ConceptViewController> result = new ArrayList<>();
		for (List<ConceptViewController> list : userToConceptViewControllers.values()) {
			for (ConceptViewController controller : list) {
				if (!cv.equals(controller) && cv.intersects(controller)) {
					result.add(controller);
				}
			}
		}
		return result;
	}

	@Override
	public void linkDirectionUpdated(LinkViewController lv, Direction d, User u) {
		LOG.info("changing link direction: " + d);
		if (d == Direction.START_TO_END)
			conceptMap.removeDirectedLink(lv.getEnd(), lv.getStart());
		else if (d == Direction.END_TO_START)
			conceptMap.removeDirectedLink(lv.getStart(), lv.getEnd());
		else
			conceptMap.setDirectedRelationToUndirected(lv.getStart(), lv.getEnd());
	}

}
