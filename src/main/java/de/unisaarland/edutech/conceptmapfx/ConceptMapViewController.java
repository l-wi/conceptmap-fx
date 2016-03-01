package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptMovingListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewConceptListener;
import de.unisaarland.edutech.conceptmapfx.event.NewLinkListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

//TODO Refactor: extract some listeners into separate classes
public class ConceptMapViewController implements NewLinkListener, NewConceptListener, LinkDeletedListener,
		ConceptDeletedListener, ConceptMovedListener, LinkDirectionUpdatedListener, ConceptMovingListener {

	private static final String DROP_TARGET_STYLE = "dropTarget";

	private static final Logger LOG = LoggerFactory.getLogger(ConceptMapViewController.class);

	private List<ConceptDeletedListener> conceptDeletedListners = new ArrayList<ConceptDeletedListener>();
	private List<LinkDeletedListener> linkDeletedListeners = new ArrayList<LinkDeletedListener>();
	private List<NewLinkListener> newLinkListeners = new ArrayList<NewLinkListener>();

	@FXML
	private ConceptMapView conceptMapPane;

	private ConceptMap conceptMap;
	private List<InputViewController> inputControllers = new ArrayList<InputViewController>();
	private List<LinkViewController> linkControllers = new ArrayList<LinkViewController>();
	private Map<User, List<ConceptViewController>> userToConceptViewControllers = new HashMap<>();

	private DoubleProperty sceneWidth = new SimpleDoubleProperty(0);

	private DoubleProperty sceneHeight = new SimpleDoubleProperty(0);

	private Map<ConceptViewController, List<ConceptViewController>> conceptToIntersectedConcepts = new HashMap<>();

	private ConceptViewBuilder conceptViewBuilder;

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
		Concept concept = cv.getConcept();

		ListIterator<LinkViewController> listIterator = linkControllers.listIterator();

		while (listIterator.hasNext()) {
			LinkViewController l = listIterator.next();

			if (l.getStart().equals(concept) || l.getEnd().equals(concept)) {
				l.removeFromView();
				removeLinkFromMap(l);
				listIterator.remove();
			}
		}

		deleteConcept(cv);
	}

	public void linkDeleted(LinkViewController lv, User u) {
		removeLinkFromMap(lv);
		linkControllers.remove(lv);
	}

	public void removeLinkFromMap(LinkViewController lv) {
		Concept start = lv.getStart();
		Concept end = lv.getEnd();
		// works also for directed
		conceptMap.removeUndirectedLink(start, end);
	}

	@FXML
	public void initialize() {
		sceneWidth.addListener((c, o, n) -> layout());
		sceneHeight.addListener((c, o, n) -> layout());
	}

	public void newConcept(InputViewController inputViewController) {

		User user = inputViewController.getUser();

		conceptViewBuilder.withNewConcept(user);
		prepareConceptBuilder(user);
		ConceptViewController cv = conceptViewBuilder.buildControllerAndAddView(inputViewController,
				this.conceptMapPane);
		this.userToConceptViewControllers.get(user).add(cv);

	}

	private void prepareConceptBuilder(User user) {

		conceptViewBuilder.withMovedListener(this).withMovingListener(this).withDeletedListener(this);

		InputViewController usersController = null;

		for (InputViewController l : inputControllers) {
			conceptViewBuilder.withEditRequestedListener(l);
			conceptViewBuilder.withDeletedListener(l);
			if (l.getUser().equals(user))
				usersController = l;
		}

		conceptViewBuilder.withConceptEmptyListener(usersController);

	}

	public void setConceptViewBuilder(ConceptViewBuilder builder) {
		this.conceptViewBuilder = builder;
	}

	public void newLink(ConceptViewController cv1, ConceptViewController cv2) {
		moveToFreeSpot(cv1, cv2);

		if (conceptMap.isAnyLinkExisting(cv1.getConcept(), cv2.getConcept())) {
			LOG.warn("there is already a link between:" + cv1.getConcept() + " and " + cv2.getConcept());
			return;
		}

		LOG.info("adding new link between:\t" + cv1.getConcept().getName().getContent() + " <-> "
				+ cv2.getConcept().getName().getContent());

		LinkViewBuilder builder = new LinkViewBuilder(conceptMap, conceptMapPane, cv1, cv2);
		builder.withDirectionListener(this).forNewLink().withDeletedListener(this);
		inputControllers.forEach((l) -> {
			builder.withEditListener(l);
			builder.withDeletedListener(l);
		});
		LinkViewController lvc = builder.buildUndirectedAndAdd();
		linkControllers.add(lvc);

	}

	private void moveToFreeSpot(ConceptViewController cvToMove, ConceptViewController cvResting) {
		FourUserTouchEditable viewToMove = cvToMove.getView();
		double widthToMove = viewToMove.getWidth();
		double heightToMove = viewToMove.getHeight();
		double xToMove = viewToMove.getLayoutX() + viewToMove.getTranslateX() + widthToMove / 2;
		double yToMove = viewToMove.getLayoutY() + viewToMove.getTranslateY() + heightToMove / 2;
		Point2D pMove = new Point2D(xToMove, yToMove);

		FourUserTouchEditable viewResting = cvResting.getView();
		double widthResting = viewResting.getWidth();
		double heightResting = viewResting.getHeight();
		double xResting = viewResting.getLayoutX() + viewResting.getTranslateX() + widthResting / 2;
		double yResting = viewResting.getLayoutY() + viewResting.getTranslateY() + heightResting / 2;

		Point2D pResting = new Point2D(xResting, yResting);

		Point2D pDelta = pMove.subtract(pResting).normalize();

		double translateX = viewToMove.getTranslateX();
		double translateY = viewToMove.getTranslateY();

		for (double r = heightResting; r < 20 * heightResting; r++) {
			for (double i = 0; i < 300; i++) {

				double angle = 2 * Math.PI * i / 300;

				double x = pDelta.getX() * Math.cos(angle) - pDelta.getY() * Math.sin(angle);
				double y = pDelta.getX() * Math.sin(angle) + pDelta.getY() * Math.cos(angle);

				x = x * r;
				y = y * r;

				viewToMove.setTranslateX(translateX + x);
				viewToMove.setTranslateY(translateY + y);

				if (!hasIntersections(viewToMove)) {
					linkControllers.forEach((l) -> l.layout());
					return;
				}

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

		clearConcepts();

		this.conceptMap = conceptMap;
				
		for (User u : conceptMap.getExperiment().getParticipants())
			userToConceptViewControllers.put(u, new ArrayList<ConceptViewController>());

		loadMap();

	}

	public void clearConcepts() {

		linkControllers.forEach((lv) -> {
			lv.removeFromView();
		});

		linkControllers.clear();
		
		userToConceptViewControllers.values().forEach((list) -> {
			list.forEach((cv) -> {
				conceptMapPane.remove(cv.getView());
				conceptMap.removeConcept(cv.getConcept());
			});
		});

		userToConceptViewControllers.clear();
	}

	private void deleteConcept(ConceptViewController cv) {
		conceptMapPane.remove(cv.getView());
		conceptMap.removeConcept(cv.getConcept());

		userToConceptViewControllers.get(cv.getConcept().getOwner()).remove(cv);
	}

	private void loadMap() {

		boolean first = true;
		ArrayList<ConceptViewController> tempList = new ArrayList<ConceptViewController>();

		for (int i = 0; i < conceptMap.getConceptCount(); i++) {

			for (int j = 0; j < conceptMap.getConceptCount(); j++) {
				if (first) {
					Concept c = conceptMap.getConcept(j);

					conceptViewBuilder.forConcept(c);
					prepareConceptBuilder(c.getOwner());
					ConceptViewController cv = conceptViewBuilder.buildControllerAndAddView(this.conceptMapPane);
					this.userToConceptViewControllers.get(c.getOwner()).add(cv);
					tempList.add(cv);
				}

				LinkViewController lvc = null;
				LinkViewBuilder builder = new LinkViewBuilder(conceptMap, conceptMapPane, tempList.get(i),
						tempList.get(j));
				builder.withDirectionListener(this).withDeletedListener(this);

				inputControllers.forEach((l) -> {
					builder.withEditListener(l);
					builder.withDeletedListener(l);
				});

				if (conceptMap.isLinkedDirectedStartToEnd(i, j)) {
					lvc = builder.withLink(conceptMap.getLink(i, j)).buildWithDirectionAndAdd(Direction.START_TO_END);

				} else if (conceptMap.isLinkedUndirected(i, j) && i < j) {
					lvc = builder.withLink(conceptMap.getLink(i, j)).buildWithDirectionAndAdd(Direction.NOT_DIRECTED);
				}

				if (lvc != null) {
					linkControllers.add(lvc);
				}

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
				cv.translateAbsolute(x, y);
				cv.getView().setRotate(c.getRotate());
			}

		}

		for (LinkViewController lvc : linkControllers) {
			lvc.layout();
		}
		
		//otherwise the textfield does not rescale and links do not end at the right spot
		this.conceptMapPane.applyCss();
		this.conceptMapPane.layout();
	}

	@Override
	public void conceptMoved(ConceptViewController cv) {
		List<ConceptViewController> intersections = findIntersections(cv);

		FourUserTouchEditable view = cv.getView();

		double x = (view.getOrigin().getX() / sceneWidth.doubleValue());
		double y = (view.getOrigin().getY() / sceneHeight.doubleValue());
		double r = view.getRotate();

		cv.getConcept().setPosition(x, y, r);

		for (ConceptViewController intersected : intersections) {
			fireNewLinkListener(cv, intersected);
		}

		conceptToIntersectedConcepts.getOrDefault(cv, Collections.emptyList()).forEach((e) -> {
			// e.getView().getStyleClass().remove(DROP_TARGET_STYLE);
			scaleDown(e);
		});

		scaleDown(cv);

		conceptToIntersectedConcepts.remove(cv);
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

	@Override
	public void conceptMoving(double x, double y, double rotate, ConceptViewController cv, User u) {
		List<ConceptViewController> intersectedCVs = findIntersections(cv);

		removeHighlightingForLinking(cv, intersectedCVs);

		addHighlightingForLinking(cv, intersectedCVs);

	}

	private void removeHighlightingForLinking(ConceptViewController cv, List<ConceptViewController> intersectedCVs) {

		List<ConceptViewController> formerIntersected = conceptToIntersectedConcepts.getOrDefault(cv,
				Collections.emptyList());

		ArrayList<ConceptViewController> difference = new ArrayList<>(formerIntersected);
		difference.removeAll(intersectedCVs);

		difference.forEach((e) -> {
			scaleDown(e);
		});

		if (intersectedCVs.size() == 0)
			scaleDown(cv);

	}

	private void addHighlightingForLinking(ConceptViewController cv, List<ConceptViewController> intersectedCVs) {
		List<ConceptViewController> formerIntersected = conceptToIntersectedConcepts.getOrDefault(cv,
				Collections.emptyList());

		ArrayList<ConceptViewController> difference = new ArrayList<>(intersectedCVs);
		difference.removeAll(formerIntersected);

		difference.forEach((e) -> {
			scaleUp(e);
		});

		if (intersectedCVs.size() > 0)
			scaleUp(cv);

		conceptToIntersectedConcepts.put(cv, intersectedCVs);
	}

	private void scaleUp(ConceptViewController e) {
		ScaleTransition st = new ScaleTransition(Duration.millis(300), e.getView());
		st.setToX(1.2);
		st.setToY(1.2);
		st.play();
	}

	private void scaleDown(ConceptViewController e) {
		ScaleTransition st = new ScaleTransition(Duration.millis(300), e.getView());
		st.setToX(1);
		st.setToY(1);
		st.play();
	}

}
