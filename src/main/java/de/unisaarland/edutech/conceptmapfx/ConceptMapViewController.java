package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewLinkListener;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;

//TODO Refactor: extract some listeners into separate classes
public class ConceptMapViewController implements NewLinkListener, LinkDeletedListener, LinkDirectionUpdatedListener {

	private static final Logger LOG = LoggerFactory.getLogger(ConceptMapViewController.class);

	private List<ConceptDeletedListener> conceptDeletedListners = new ArrayList<ConceptDeletedListener>();
	private List<LinkDeletedListener> linkDeletedListeners = new ArrayList<LinkDeletedListener>();
	private List<NewLinkListener> newLinkListeners = new ArrayList<NewLinkListener>();

	@FXML
	private ConceptMapView conceptMapPane;

	private ConceptMap conceptMap;
	private List<InputViewController> inputControllers = new ArrayList<InputViewController>();
	private List<LinkViewController> linkControllers = new ArrayList<LinkViewController>();
	private List<ConceptViewController> conceptControllers = new ArrayList<>();

	private DoubleProperty sceneWidth = new SimpleDoubleProperty(0);
	private DoubleProperty sceneHeight = new SimpleDoubleProperty(0);

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

		conceptViewBuilder.withMap(conceptMap);

		loadMap();

	}

	private void clearConcepts() {

		List<LinkViewController> workingListLinks = new ArrayList<>(linkControllers);

		workingListLinks.forEach((lv) -> {
			lv.remove();
		});

		List<ConceptViewController> copy = new ArrayList<>(conceptControllers);
		copy.forEach((cv) -> cv.fireConceptDeleted());

		linkControllers.clear();
		conceptControllers.clear();
	}

	private void loadMap() {

		boolean first = true;
		ArrayList<ConceptViewController> tempList = new ArrayList<ConceptViewController>();

		for (int i = 0; i < conceptMap.getConceptCount(); i++) {

			for (int j = 0; j < conceptMap.getConceptCount(); j++) {
				if (first) {
					Concept c = conceptMap.getConcept(j);

					conceptViewBuilder.forConcept(c);

					InputViewController ownerController = inputControllers.stream()
							.filter((in) -> in.getUser().equals(c.getOwner())).findFirst().get();

					conceptViewBuilder.withConceptEmptyListener(ownerController);

					ConceptViewController cv = conceptViewBuilder.buildControllerAndAddView(this.conceptMapPane);
					this.conceptControllers.add(cv);
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

		for (ConceptViewController cv : conceptControllers) {
			Concept c = cv.getConcept();
			double x = c.getX() * this.sceneWidth.doubleValue();
			double y = c.getY() * this.sceneHeight.doubleValue();
			cv.translateAbsolute(x, y);
			cv.getView().setRotate(c.getRotate());
		}

		for (LinkViewController lvc : linkControllers) {
			lvc.layout();
		}

		// otherwise the textfield does not rescale and links do not end at the
		// right spot
		this.conceptMapPane.applyCss();
		this.conceptMapPane.layout();
	}

	public void fireNewLinkListener(ConceptViewController cv, ConceptViewController intersected) {
		newLinkListeners.forEach(l -> l.newLink(cv, intersected));
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

	public DoubleProperty getWidth() {
		return sceneWidth;
	}

	public DoubleProperty getHeight() {
		return sceneHeight;
	}

	public ConceptMapView getView() {
		return conceptMapPane;
	}

	public void add(ConceptViewController cv) {
		this.conceptControllers.add(cv);
	}

	public ConceptMap getMap() {
		return this.conceptMap;
	}

	public void remove(ConceptViewController cv) {
		conceptControllers.remove(cv);
	}

	public List<LinkViewController> getLinkControllers() {
		return linkControllers;
	}

	public List<ConceptViewController> getConceptViewControllers() {
		return conceptControllers;
	}
}
