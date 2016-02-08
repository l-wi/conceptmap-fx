package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.input.TouchPoint.State;
import javafx.stage.Stage;
import jfxtras.util.NodeUtil;

public class TUIOWrapper implements TuioListener {

	private static final Logger LOG = LoggerFactory.getLogger(TUIOWrapper.class);
	private static final int FRAME_RATE = 90;

	private int eventSetId = 0;

	private Scene scene;

	private Stage stage;

	private Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
	private Queue<Event> queue = new LinkedList<Event>();

	Runnable emptyQueue = () -> {
		while (!queue.isEmpty()) {
			Event e = queue.poll();
			Event.fireEvent(e.getTarget(), e);
		}
	};
	private TuioTime lastTime;

	public TUIOWrapper(Scene scene, Stage stage) {
		this.scene = scene;
		this.stage = stage;
		this.queue = new LinkedList<Event>();

	}

	public void start() {
		TuioClient client = new TuioClient();
		client.addTuioListener(this);
		client.connect();
	}

	@Override
	public void addTuioCursor(TuioCursor c) {
		lastTime = c.getTuioTime();

		Optional<Event> evt = pressed(c);
		if (evt.isPresent())
			queue.add(evt.get());

		updateOnApplicationThread(c);
	}

	@Override
	public void removeTuioCursor(TuioCursor c) {
		Optional<Event> evt = released(c);
		if (evt.isPresent())
			queue.add(evt.get());

		updateOnApplicationThread(c);

	}

	@Override
	public void updateTuioCursor(TuioCursor c) {

		Optional<Event> evt = moved(c);
		if (evt.isPresent())
			queue.add(evt.get());

		updateOnApplicationThread(c);

	}

	private void updateOnApplicationThread(TuioCursor c) {
		long delta = c.getTuioTime().subtract(lastTime).getTotalMilliseconds() / 1000;

		if (delta < 1d / FRAME_RATE)
			Platform.runLater(emptyQueue);

		lastTime = c.getTuioTime();
	}

	private Optional<Event> moved(TuioCursor c) {
		Node src = nodeMap.get(c.getCursorID());
		return getEventForCursor(src, c, TouchEvent.TOUCH_MOVED);
	}

	private Optional<Event> pressed(TuioCursor c) {
		Node src = pick(c);
		nodeMap.put(c.getCursorID(), src);

		if (src == null) {
			src = scene.getRoot();
		}

		EventType<TouchEvent> type = TouchEvent.TOUCH_PRESSED;
		return getEventForCursor(src, c, type);
	}

	private Optional<Event> released(TuioCursor c) {
		Node src = nodeMap.remove(c.getCursorID());
		return getEventForCursor(src, c, TouchEvent.TOUCH_RELEASED);
	}

	private Optional<Event> getEventForCursor(Node src, TuioCursor c, EventType<TouchEvent> type) {

		if (src == null)
			return Optional.empty();

		boolean shiftDown = false;
		boolean controlDown = false;
		boolean altDown = false;
		boolean metaDown = false;

		List<TouchPoint> touchPoints = createTouchPoints(src, c);
		TouchPoint touchPoint = touchPoints.get(0);

		Event event = new TouchEvent(this, src, type, touchPoint, touchPoints, eventSetId++, shiftDown, controlDown,
				altDown, metaDown);

		return Optional.of(event);
	}

	private List<TouchPoint> createTouchPoints(Node src, TuioCursor c) {

		List<TouchPoint> results = new ArrayList<>();

		results.add(createTouchPoint(src, c));

		return results;
	}

	private TouchPoint createTouchPoint(Node n, TuioCursor c) {
		TouchPoint.State state = TouchPoint.State.STATIONARY;

		if (c.getTuioState() == TuioCursor.TUIO_ADDED)
			state = State.PRESSED;
		else if (c.getTuioState() == TuioCursor.TUIO_REMOVED)
			state = State.RELEASED;
		else if (c.getTuioState() == TuioCursor.TUIO_STOPPED)
			state = State.STATIONARY;
		else
			state = State.MOVED;

		return new TouchPoint(c.getCursorID(), state, getSceneX(c), getSceneY(c), getScreenX(c), getScreenY(c), pick(c),
				null);
	}

	private Node pick(TuioCursor c) {
		double sceneX = getSceneX(c);
		double sceneY = getSceneY(c);

		//find control component
		Node n = NodeUtil.getNode(scene.getRoot(), sceneX, sceneY, Control.class);
		// only if there is no control component, pick container
		if (n == null)
			NodeUtil.getNode(scene.getRoot(), sceneX, sceneY, Node.class);
		return n;
	}

	private double getScreenX(TuioCursor c) {
		return c.getX() * stage.getWidth();
	}

	private double getScreenY(TuioCursor c) {
		return c.getY() * stage.getWidth();
	}

	private double getSceneX(TuioCursor c) {
		return c.getX() * scene.getWidth();

	}

	private double getSceneY(TuioCursor c) {
		return c.getY() * scene.getHeight();

	}

	@Override
	public void addTuioObject(TuioObject o) {
	}

	@Override
	public void refresh(TuioTime t) {
	}

	@Override
	public void removeTuioObject(TuioObject o) {
	}

	@Override
	public void updateTuioObject(TuioObject o) {
	}

}
