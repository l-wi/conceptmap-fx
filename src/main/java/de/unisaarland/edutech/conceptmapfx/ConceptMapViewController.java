package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewConceptListener;
import de.unisaarland.edutech.conceptmapfx.event.NewLinkListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

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

	private DoubleProperty sceneWidth = new SimpleDoubleProperty(0);

	private DoubleProperty sceneHeight = new SimpleDoubleProperty(0);

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
		sceneWidth.addListener((c, o, n) -> layout());
		sceneHeight.addListener((c, o, n) -> layout());
	}

	public void newConcept(InputViewController inputViewController) {

		User user = inputViewController.getUser();
		Optional<ConceptViewController> emptyConceptViewController = nextEmptyConcept(user);

		if (emptyConceptViewController.isPresent()) {
			LOG.warn("there is  already an empty concept for user" + user);

			return;
		}

		ConceptViewBuilder builder = new ConceptViewBuilder(this.conceptMap);
		builder = builder.withNewConcept(user).withMovedListener(this);

		for (ConceptEditRequestedListener l : inputControllers)
			builder.withEditRequestedListener(l);

		ConceptViewController controller = builder.buildControllerAndAddView(inputViewController, this.conceptMapPane);
		this.userToConceptViewControllers.get(user).add(controller);
	}

	public void newLink(ConceptViewController cv1, ConceptViewController cv2) {
		if (conceptMap.isAnyLinkExisting(cv1.getConcept(), cv2.getConcept())) {
			LOG.warn("there is already a link between:" + cv1.getConcept() + " and " + cv2.getConcept());
			return;
		}

		//TODO would be cool if we also try first the direction the link comes from
		moveToFreeSpot(cv1);
		
		LOG.info("adding new link between:\t" + cv1.getConcept().getName().getContent() + " <-> "
				+ cv2.getConcept().getName().getContent());

		LinkViewBuilder builder = new LinkViewBuilder(conceptMap, conceptMapPane, cv1, cv2);
		builder.withDirectionListener(this).forNewLink();
		inputControllers.forEach((l) -> builder.withEditListener(l));
		LinkViewController lvc = builder.buildUndirectedAndAdd();
		linkControllers.add(lvc);

		
		

	}

	private void moveToFreeSpot(ConceptViewController cv2) {
		FourUserTouchEditable view = cv2.getView();

		double width = view.getWidth();

		double translateX = view.getTranslateX();
		double translateY = view.getTranslateY();

		for (double r = width * 2.5 ; r < 10 * width; r += width / 2) {
	

			for (double i = 2 * Math.PI; i >= 0; i -= 0.1) {

				double x = r * Math.cos(i);
				double y = r * Math.sin(i);

				view.setTranslateX(translateX + x);
				view.setTranslateY(translateY + y);

				if (!hasIntersections(view))
					return;
			}
		}

		LOG.warn("found no non-overlaping spot for node!");
	}

	private boolean hasIntersections(Node conceptView) {

		Bounds bounds = conceptView.getBoundsInParent();
		
		for (Node n : this.conceptMapPane.getChildren()) {

			if (conceptView != n && bounds.intersects(n.getBoundsInParent())) {
				return true;
			}

		}
		
		return false;

	}

	public DoubleProperty sceneWidthProperty() {
		return sceneWidth;
	}

	public DoubleProperty sceneHeightProperty() {
		return sceneHeight;
	}

	public void setConceptMap(ConceptMap conceptMap) {
		this.conceptMap = conceptMap;

		for (User u : conceptMap.getExperiment().getParticipants())
			userToConceptViewControllers.put(u, new ArrayList<ConceptViewController>());

		loadMap();
	}

	private void loadMap() {

		boolean first = true;
		ArrayList<ConceptViewController> tempList = new ArrayList<ConceptViewController>();
		for (int i = 0; i < conceptMap.getConceptCount(); i++) {

			for (int j = 0; j < conceptMap.getConceptCount(); j++) {

				if (first) {
					Concept c = conceptMap.getConcept(j);
					ConceptViewController controller = buildForExistingConcept(c);
					this.userToConceptViewControllers.get(c.getOwner()).add(controller);
					tempList.add(controller);
				}

				LinkViewController lvc = null;
				LinkViewBuilder builder = new LinkViewBuilder(conceptMap, conceptMapPane, tempList.get(i),
						tempList.get(j));
				builder.withDirectionListener(this);
				inputControllers.forEach((l) -> builder.withEditListener(l));

				if (conceptMap.isLinkedDirectedStartToEnd(i, j)) {
					lvc = builder.withLink(conceptMap.getLink(i, j)).buildDirectedAndAdd(Direction.START_TO_END);
				} else if (conceptMap.isLinkedUndirected(i, j))
					lvc = builder.withLink(conceptMap.getLink(i, j)).buildDirectedAndAdd(Direction.NOT_DIRECTED);

				if (lvc != null)
					linkControllers.add(lvc);

			}

			first = false;
		}

	}

	public void layout() {

		for (List<ConceptViewController> list : userToConceptViewControllers.values()) {
			for (ConceptViewController cv : list) {
				Concept c = cv.getConcept();
				double x = c.getX() * this.sceneWidth.doubleValue();
				double y = c.getY() * this.sceneHeight.doubleValue();
				LOG.info("moving after rescale: x / y " + x + "/" + y);
				cv.translateAbsolute(x, y);
				cv.getView().setRotate(c.getRotate());
			}
		}

		for (LinkViewController lvc : linkControllers) {
			lvc.layout();
		}
	}

	private ConceptViewController buildForExistingConcept(Concept c) {
		ConceptViewBuilder builder = new ConceptViewBuilder(conceptMap);
		builder.forConcept(c).withMovedListener(this);

		for (ConceptEditRequestedListener l : inputControllers)
			builder.withEditRequestedListener(l);

		return builder.buildControllerAndAddView(this.conceptMapPane);
	}

	private Optional<ConceptViewController> nextEmptyConcept(User user) {

		for (ConceptViewController controller : userToConceptViewControllers.get(user))
			if (controller.getConcept().getName().getContent().equals(""))
				return Optional.of(controller);
		return Optional.ofNullable(null);
	}

	@Override
	public void conceptMoved(ConceptViewController cv) {
		List<ConceptViewController> intersections = findIntersections(cv);

		FourUserTouchEditable view = cv.getView();

		cv.getConcept().setX(view.getOrigin().getX() / sceneWidth.doubleValue());
		cv.getConcept().setY(view.getOrigin().getY() / sceneHeight.doubleValue());
		cv.getConcept().setRotate(view.getRotate());

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
