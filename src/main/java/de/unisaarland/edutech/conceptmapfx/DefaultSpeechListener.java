package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.unisaarland.edutech.conceptmapfx.event.SpeechRecognitionListner;
import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapping.User;
import de.unisaarland.edutech.nuanceclient.AudioRecorder;
import de.unisaarland.edutech.nuanceclient.NuanceClient;
import de.unisaarland.edutech.nuanceclient.NuanceClient.Result;
import de.unisaarland.edutech.nuanceclient.NuanceCredentials;
import de.unisaarland.edutech.nuanceclient.RecordingException;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;

public class DefaultSpeechListener implements SpeechRecognitionListner {

	private static final String CODEC = "audio/x-wav;codec=pcm;bit=16;rate=16000";

	private static final int MILLIS_ANIMATION_FRAME = 500;
	private ToggleButton btnSpeak;
	private SequentialTransition listenTransition;
	private SequentialTransition recordingTransition;
	private User user;

	private AudioRecorder recorder = new AudioRecorder();

	private NuanceClient nuanceClient;

	private File recording;

	private CollaborativeStringTextFieldBinding binding;

	public DefaultSpeechListener(User u, ToggleButton btnSpeak) {
		this.btnSpeak = btnSpeak;
		this.user = u;

		initListenTransition();
		initRecordingTransition();
		initSpeech();
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

	private void initSpeech() {
		try {
			NuanceCredentials creds = NuanceCredentials.construct();
			nuanceClient = new NuanceClient(creds);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void speechRecognitionStarted(User u) {
		if (!u.equals(user))
			this.btnSpeak.setDisable(true);
		else
			startRecording();

	}

	@Override
	public void speechRecognitionFinished(User u) {
		if (u.equals(user)) {
			recordingTransition.stop();
			stopRecording();
			requestRecognition(u);

		} else
			btnSpeak.setDisable(false);
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
			binding.append(u, result.charAt(i));
	}

	private void stopRecording() {
		try {
			recorder.stop();
		} catch (RecordingException e) {
			// TODO exception handling
			e.printStackTrace();
		}
	}

	public void setBinding(CollaborativeStringTextFieldBinding binding) {
		this.binding = binding;
	}
}
