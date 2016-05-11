package de.unisaarland.edutech.conceptmapfx.conceptmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.InputViewController;
import de.unisaarland.edutech.conceptmapfx.InteractionLogger;
import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewBuilder;
import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewLinkListener;
import de.unisaarland.edutech.conceptmapfx.link.LinkViewBuilder;
import de.unisaarland.edutech.conceptmapfx.link.LinkViewController;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;

public class ConceptMapViewController implements LinkDeletedListener, LinkDirectionUpdatedListener {

	private static final Logger LOG = LoggerFactory.getLogger(ConceptMapViewController.class);

	private static final InteractionLogger INTERACTION_LOGGER = InteractionLogger.getInstance();

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
		INTERACTION_LOGGER.deleteLinkData(lv.getStart(), lv.getEnd(), lv.getLink());
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

	public boolean hasIntersections(Node conceptView) {

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

		boolean isConceptCreationPhase = true;
		ArrayList<ConceptViewController> existingConceptViewAccumulator = new ArrayList<ConceptViewController>();

		int conceptCount = conceptMap.getConceptCount();

		for (int i = 0; i < conceptCount; i++) {
			for (int j = 0; j < conceptCount; j++) {

				if (isConceptCreationPhase)
					loadConceptView(existingConceptViewAccumulator, j);

				Direction linkDirection = null;

				if (conceptMap.isLinkedDirectedStartToEnd(i, j))
					linkDirection = Direction.START_TO_END;
				else if (conceptMap.isLinkedUndirected(i, j) && i < j)
					linkDirection = Direction.NOT_DIRECTED;

				if (linkDirection != null) {
					LinkViewBuilder builder = prepareLinkViewBuilder(existingConceptViewAccumulator, i, j);
					LinkViewController lvc = builder.buildWithDirectionAndAdd(linkDirection);
					linkControllers.add(lvc);
				}
			}

			isConceptCreationPhase = false;
		}

	}

	private void loadConceptView(ArrayList<ConceptViewController> existingConcepts, int j) {
		Concept c = conceptMap.getConcept(j);
		ConceptViewController cv = createConceptView(c);
		this.conceptControllers.add(cv);
		existingConcepts.add(cv);
	}

	private LinkViewBuilder prepareLinkViewBuilder(ArrayList<ConceptViewController> existingConcepts, int i, int j) {
		ConceptViewController conceptView1 = existingConcepts.get(i);
		ConceptViewController conceptView2 = existingConcepts.get(j);

		LinkViewBuilder builder = initLinkViewBuilderWithListeners(conceptView1, conceptView2);

		builder.withLink(conceptMap.getLink(i, j));
		return builder;
	}

	private LinkViewBuilder initLinkViewBuilderWithListeners(ConceptViewController conceptView1,
			ConceptViewController conceptView2) {
		LinkViewBuilder builder = new LinkViewBuilder(conceptMap, conceptMapPane, conceptView1, conceptView2);
		builder.withDirectionListener(this).withDeletedListener(this);

		inputControllers.forEach((l) -> {
			builder.withEditListener(l);
			builder.withDeletedListener(l);
		});
		return builder;
	}

	private ConceptViewController createConceptView(Concept c) {
		conceptViewBuilder.forConcept(c);

		InputViewController ownerController = inputControllers.stream()
				.filter((in) -> in.getUser().equals(c.getOwner())).findFirst().get();

		// conceptViewBuilder.withConceptEmptyListener(ownerController);

		ConceptViewController cv = conceptViewBuilder.buildControllerAndAddView(this.conceptMapPane);
		return cv;
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

		INTERACTION_LOGGER.directionUpdateLinkData(lv.getStart(), lv.getEnd(), lv.getLink());
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
		return Collections.unmodifiableList(linkControllers);
	}

	public List<ConceptViewController> getConceptViewControllers() {
		return Collections.unmodifiableList(conceptControllers);
	}

	public List<InputViewController> getInputControllers() {
		return Collections.unmodifiableList(inputControllers);
	}

	public void addLinkController(LinkViewController lvc) {
		this.linkControllers.add(lvc);
	}
}
