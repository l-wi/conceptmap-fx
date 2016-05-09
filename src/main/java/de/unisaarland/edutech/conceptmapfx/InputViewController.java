package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.comtel2000.keyboard.control.KeyButton;
import org.comtel2000.keyboard.control.KeyboardPane;
import org.comtel2000.keyboard.control.KeyboardType;
import org.comtel2000.keyboard.event.KeyButtonEvent;
import org.comtel2000.keyboard.robot.IRobot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.event.AlignListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptContentChangeListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.ConceptEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkEditRequestedListener;
import de.unisaarland.edutech.conceptmapfx.event.NewConceptListener;
import de.unisaarland.edutech.conceptmapfx.event.SpeechRecognitionListner;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapfx.link.LinkViewController;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.User;
import de.unisaarland.edutech.nuanceclient.AudioRecorder;
import de.unisaarland.edutech.nuanceclient.NuanceClient;
import de.unisaarland.edutech.nuanceclient.NuanceClient.Result;
import de.unisaarland.edutech.nuanceclient.NuanceCredentials;
import de.unisaarland.edutech.nuanceclient.RecordingException;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class InputViewController implements ConceptEditRequestedListener, LinkEditRequestedListener,
		LinkDeletedListener, ConceptDeletedListener, ConceptContentChangeListener, SpeechRecognitionListner {

	private static final int MILLIS_ANIMATION_FRAME = 500;

	private static final Logger LOG = LoggerFactory.getLogger(InputViewController.class);

	private User user;

	private static final String CODEC = "audio/x-wav;codec=pcm;bit=16;rate=16000";

	private InputClosedListener closedListener;
	private List<NewConceptListener> conceptListners = new ArrayList<NewConceptListener>();
	private List<SpeechRecognitionListner> speechListeners = new ArrayList<SpeechRecognitionListner>();

	File recording;

	public enum Position {
		TOP, BOTTOM, RIGHT, LEFT
	}

	@FXML
	private KeyboardPane keyboard;
	@FXML
	private VBox inputControls;
	@FXML
	private AnchorPane inputPane;
	@FXML
	private Button btnNewConcept;
	@FXML
	private Button btnUndo;
	@FXML
	private Label owner;
	@FXML
	private Pane ownerIcon;
	@FXML
	private Label question;
	@FXML
	private Button btnAlign;
	@FXML
	private ToggleButton btnSpeak;

	private Position position;

	private UserRobotHandler currentRobotHandler;

	private CollaborativeStringTextFieldBinding collaborativeStringBinding;

	private int emptyConceptCount = 0;

	private UndoHistory undoHistory;

	private AlignListener alignListener;

	private AudioRecorder recorder = new AudioRecorder();

	private NuanceClient nuanceClient;

	private SequentialTransition recordingTransition;

	private SequentialTransition listenTransition;

	@FXML
	public void initialize() {
		try {
			initKeyboard();
			initButtons();
			initQuestion();
			initSpeech();
			hideInput();

		} catch (IOException | URISyntaxException e) {
			LOG.error("Program cannot run!", e);
			throw new RuntimeException("Program cannot run!", e);
		}
	}

	private void initSpeech() {
		try {
			NuanceCredentials creds = NuanceCredentials.construct();
			nuanceClient = new NuanceClient(creds);
			initRecordingTransition();
			initListenTransition();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initButtons() {

		btnAlign.setOnTouchPressed(e -> setTouchHighlight(btnAlign));
		btnUndo.setOnTouchPressed(e -> setTouchHighlight(btnUndo));

		btnAlign.setOnTouchReleased(e -> removeTouchHighlight(btnAlign));
		btnUndo.setOnTouchReleased(e -> removeTouchHighlight(btnUndo));

	}

	private void initKeyboard() throws MalformedURLException, IOException, URISyntaxException {
		keyboard.setKeyBoardStyle(getClass().getResource("/css/input.css").toString());
		keyboard.setSpaceKeyMove(false);
		keyboard.setLayerPath(new File("./keyboardLayout").toPath());
		keyboard.load();

		/*
		 * FIXME: Workaround to allow multitouch input while moving an element:
		 * - this only works for short press events, it does not work for long
		 * press (e.g. Ä)
		 */
		addTouchListenerToKeyboard();
		keyboard.setKeyboardType(KeyboardType.TEXT_SHIFT);
		addTouchListenerToKeyboard();
		keyboard.setKeyboardType(KeyboardType.TEXT);

		// remove the default handler
		IRobot defaultHandler = keyboard.getRobotHandler().get(0);
		keyboard.removeRobotHandler(defaultHandler);
		keyboard.setDisable(true);
	}

	private void initQuestion() {
		question.setWrapText(true);
		question.widthProperty().addListener((c, o, n) -> {
			double rotate = question.getRotate();
			int cycleCount = 4;
			double timeframe = 700;
			float angle = 3f;

			RotateTransition rotateTransition = new RotateTransition(Duration.millis(timeframe / cycleCount), question);

			rotateTransition.setFromAngle(-angle);
			rotateTransition.setToAngle(angle);

			rotateTransition.setCycleCount(cycleCount);
			rotateTransition.setAutoReverse(true);
			rotateTransition.setDelay(Duration.seconds(4));
			rotateTransition.setOnFinished((e) -> question.setRotate(rotate));
			rotateTransition.play();
		});

		btnNewConcept.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(btnNewConcept, Priority.ALWAYS);
	}

	private void addTouchListenerToKeyboard() {
		Set<Node> keys = keyboard.lookupAll(".key-button");

		keys.forEach(n -> {
			KeyButton key = (KeyButton) n;
			key.setOnTouchPressed(e -> {
				if (e.getTouchCount() > 1) {
					key.fireEvent(new KeyButtonEvent(KeyButtonEvent.SHORT_PRESSED));
					setTouchHighlight(key);
				}

			});
			key.setOnTouchReleased(e -> removeTouchHighlight(key));
		});
	}

	private void setTouchHighlight(Button b) {
		b.setStyle("-fx-background-color: #dcdcdc");
	}

	private void removeTouchHighlight(Button b) {
		PauseTransition wait = new PauseTransition(Duration.millis(300));
		wait.setOnFinished((e) -> {
			b.setStyle("-fx-background-color: #3c4250");
		});
		wait.play();
	}

	@FXML
	public void onCloseAction() {

		hideInput();

		releaseInput();
	}

	@FXML
	public void onCloseTouchPressed(TouchEvent e) {
		if (e.getTouchCount() > 1)
			onCloseAction();
	}

	private void hideInput() {
		Set<Node> toHide = inputControls.lookupAll(".hideable");

		for (Node n : toHide) {
			n.setVisible(false);
		}
	}

	@FXML
	public void onNewAction() {
		btnNewConcept.setDisable(true);
		emptyConceptCount++;
		fireNew();
	}

	@FXML
	public void onNewTouchPressed(TouchEvent e) {
		if (e.getTouchCount() > 1)
			onNewAction();

	}

	public void addNewConceptListener(NewConceptListener l) {
		conceptListners.add(l);
	}

	public void addSpeechListener(SpeechRecognitionListner l) {
		speechListeners.add(l);
	}

	private void fireNew() {
		conceptListners.forEach(l -> l.newConcept(this));
	}

	private void setInputClosedListener(InputClosedListener l) {
		closedListener = l;
	}

	public void conceptEditRequested(InputClosedListener l, CollaborativeStringTextFieldBinding editable, User u) {
		editRequested(l, editable, u);
	}

	private void editRequested(InputClosedListener l, CollaborativeStringTextFieldBinding editable, User u) {
		// does somebody else want to work on our current node
		if (editable.equals(this.collaborativeStringBinding) && !u.equals(this.user))
			releaseInput();

		// it is not an edit request for us.
		if (!this.user.equals(u))
			return;

		// for us, release current and acquire new
		releaseInput();

		setInputClosedListener(l);
		acquireInput(editable, u);
		keyboard.setDisable(false);

	}

	@Override
	public void linkEditRequested(InputClosedListener l, CollaborativeStringTextFieldBinding cv, User u) {
		editRequested(l, cv, u);
	}

	private void acquireInput(CollaborativeStringTextFieldBinding cv, User u) {

		LOG.info("acquiring input for user:\t" + this.user);

		currentRobotHandler = new UserRobotHandler(cv, u);
		this.keyboard.addRobotHandler(currentRobotHandler);
		this.collaborativeStringBinding = cv;

		Set<Node> hiddenNodes = inputControls.lookupAll(".hideable");

		unhideInputControl(hiddenNodes);

	}

	private void unhideInputControl(Set<Node> hiddenNodes) {
		for (Node n : hiddenNodes) {
			n.setVisible(true);
		}
	}

	private void releaseInput() {

		if (currentRobotHandler != null && closedListener != null) {
			LOG.info("releasing input for user:\t" + this.user);
			this.keyboard.removeRobotHandler(currentRobotHandler);
			closedListener.inputClosed(this.user);
			currentRobotHandler = null;
			closedListener = null;
			keyboard.setDisable(true);
		}

	}

	public void conceptDeleted(ConceptViewController cv, User u) {

		Concept concept = cv.getConcept();

		boolean belongsToUser = concept.getOwner().equals(this.user);
		boolean isEmpty = concept.getName().getContent().isEmpty();
		if (belongsToUser && isEmpty) {
			emptyConceptCount--;
			btnNewConcept.setDisable(emptyConceptCount > 0);
		}

		if (u == null || !u.equals(this.user))
			return;

		releaseInput();
	}

	public void linkDeleted(LinkViewController lv, User u) {
		if (u == null || !u.equals(this.user))
			return;

		releaseInput();
	}

	public void setUser(User u) {
		this.user = u;
		owner.setText(u.getName());

	}

	public User getUser() {
		return user;
	}

	public Bounds getSceneBounds() {
		return inputControls.localToScene(inputControls.getBoundsInLocal());
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
		adjustNewButton();
		adjustIcon();
	}

	private void adjustIcon() {
		ObservableList<String> styleClasses = ownerIcon.getStyleClass();

		((Label) ownerIcon.lookup(".letter")).setText(user.getName().subSequence(0, 1).toString());

		String css = "background-";
		switch (position) {
		case BOTTOM:
			styleClasses.add(css + "bottom");
			break;
		case TOP:
			styleClasses.add(css + "top");
			break;
		case LEFT:
			styleClasses.add(css + "left");
			break;
		case RIGHT:
			styleClasses.add(css + "right");
			break;
		}
		return;
	}

	private void adjustNewButton() {
		ObservableList<String> styleClasses = btnNewConcept.getStyleClass();
		String css = "newBtn";
		switch (position) {
		case BOTTOM:
			styleClasses.add(css + "Bottom");
			break;
		case TOP:
			styleClasses.add(css + "Top");
			break;
		case LEFT:
			styleClasses.add(css + "Left");
			break;
		case RIGHT:
			styleClasses.add(css + "Right");
			break;
		}
	}

	public Point2D transformLocalToScene(Point2D pLocalToInput) {
		return inputControls.getLocalToSceneTransform().transform(pLocalToInput);
	}

	public double getRotate() {
		return inputPane.getRotate();
	}

	@Override
	public void conceptContentChanged(ConceptViewController source, String oldContent, String newContent) {
		if (newContent.isEmpty() && !oldContent.isEmpty())
			emptyConceptCount++;
		else if (!newContent.isEmpty() && oldContent.isEmpty())
			emptyConceptCount--;

		btnNewConcept.setDisable(emptyConceptCount > 0);

	}

	@FXML
	public void onUndoAction() {
		undoHistory.undo();
		if (undoHistory.isEmpty()) {
			btnUndo.setDisable(true);
		}
	}

	@FXML
	void onAlignAction() {
		alignListener.align();
	}

	public void setAlignListener(AlignListener l) {
		this.alignListener = l;
	}

	public void setUndoHistory(UndoHistory undo) {
		this.undoHistory = undo;
		this.undoHistory.addUndoButton(btnUndo);
	}

	public void setFocusQuestion(String question) {
		this.question.setText(question);
	}

	@FXML
	public void onSpeechAction() {
		if (btnSpeak.isSelected())
			fireSpeechRecognitionStarted();
		else
			fireSpeechRecognitionEnded();
	}

	private void fireSpeechRecognitionStarted() {
		speechListeners.forEach((l) -> l.speechRecognitionStarted(getUser()));
	}

	private void fireSpeechRecognitionEnded() {
		speechListeners.forEach((l) -> l.speechRecognitionFinished(getUser()));
	}

	@Override
	public void speechRecognitionStarted(User u) {
		if (!u.equals(getUser()))
			this.btnSpeak.setDisable(true);
		else
			startRecording();

	}

	private void startRecording() {
		try {
			recording = File.createTempFile("conceptMapRecording", ".wav");
			recorder.record(recording);

			recordingTransition.play();

		} catch (IOException e) {
			// TODO exception handling
			e.printStackTrace();
		}
	}

	private void initListenTransition() {
		PauseTransition p1 = new PauseTransition(Duration.millis(MILLIS_ANIMATION_FRAME));

		p1.setOnFinished((e) -> {
			btnSpeak.setStyle("-fx-background-image: url(\"/gfx/listen1.png\");");
		});

		PauseTransition p2 = new PauseTransition(Duration.millis(MILLIS_ANIMATION_FRAME));
		p2.setOnFinished((e) -> {
			btnSpeak.setStyle("-fx-background-image: url(\"/gfx/listen2.png\");");
		});

		listenTransition = new SequentialTransition();
		listenTransition.setCycleCount(SequentialTransition.INDEFINITE);
		listenTransition.getChildren().addAll(p1, p2);
	}

	private void initRecordingTransition() {
		PauseTransition p1 = new PauseTransition(Duration.millis(MILLIS_ANIMATION_FRAME));

		p1.setOnFinished((e) -> {
			btnSpeak.setStyle("-fx-background-image: url(\"/gfx/recording1.png\");");
		});

		PauseTransition p2 = new PauseTransition(Duration.millis(MILLIS_ANIMATION_FRAME));
		p2.setOnFinished((e) -> {
			btnSpeak.setStyle("-fx-background-image: url(\"/gfx/recording2.png\");");
		});

		PauseTransition p3 = new PauseTransition(Duration.millis(MILLIS_ANIMATION_FRAME));
		p3.setOnFinished((e) -> {
			btnSpeak.setStyle("-fx-background-image: url(\"/gfx/recording3.png\");");
		});

		recordingTransition = new SequentialTransition();
		recordingTransition.setCycleCount(SequentialTransition.INDEFINITE);
		recordingTransition.getChildren().addAll(p1, p2, p3);
	}

	@Override
	public void speechRecognitionFinished(User u) {
		if (u.equals(getUser())) {
			recordingTransition.stop();
			stopRecording();
			requestRecognition(u);

		} else
			btnSpeak.setDisable(false);
	}

	private void requestRecognition(User u) {
		try {
			// TODO error handling

			listenTransition.play();
			nuanceClient.requestAsync(new FileInputStream(recording), CODEC, (c) -> {
				Platform.runLater(() -> {
					finishRecognition(u, c);
				});
			});
		} catch (FileNotFoundException e) {
			// TODO exception handling
			e.printStackTrace();
		}
	}

	private void finishRecognition(User u, Result c) {
		listenTransition.stop();

		PauseTransition p = new PauseTransition(Duration.millis(1000));
		p.setOnFinished((e) -> btnSpeak.setStyle(""));
		p.play();

		if (!c.isSuccessful()) {
			btnSpeak.setStyle("-fx-background-image: url(\"/gfx/error.png\");");
			return;
		}

		btnSpeak.setStyle("-fx-background-image: url(\"/gfx/success.png\");");

		String result = c.resultSet.get(0);
		for (int i = 0; i < result.length(); i++)
			collaborativeStringBinding.append(u, result.charAt(i));
	}

	private void stopRecording() {
		try {
			recorder.stop();
		} catch (RecordingException e) {
			// TODO exception handling
			e.printStackTrace();
		}
	}
}
