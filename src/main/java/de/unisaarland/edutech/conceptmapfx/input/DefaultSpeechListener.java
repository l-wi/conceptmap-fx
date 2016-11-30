/*******************************************************************************
 * conceptmap-fx a concept mapping prototype for research.
 * Copyright (C) Tim Steuer (master's thesis 2016)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, US
 *******************************************************************************/
package de.unisaarland.edutech.conceptmapfx.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;

public class DefaultSpeechListener implements SpeechRecognitionListner {

	private static final int SHOW_RECOGNITION_RESULT_TIME = 2500;

	private static final String ERROR_MSG = "Cannot inialize speech recognition";

	private static final Logger LOG = LoggerFactory.getLogger(InputViewController.class);

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

	private boolean isProcessing;

	private Button btnNew;

	public DefaultSpeechListener(User u, ToggleButton btnSpeak, Button btnNew) {
		this.btnSpeak = btnSpeak;
		this.user = u;
		this.btnNew = btnNew;

		initListenTransition();
		initRecordingTransition();
		initSpeech();
	}

	private PauseTransition newPauseTransition(Duration d, EventHandler<ActionEvent> e) {
		PauseTransition p1 = new PauseTransition(d);
		p1.setOnFinished(e);
		return p1;

	}

	private void initListenTransition() {
		Duration d = Duration.millis(MILLIS_ANIMATION_FRAME);

		PauseTransition p1 = newPauseTransition(d, e -> btnSpeak.setStyle(getStyleForImage("listen1.png")));

		PauseTransition p2 = newPauseTransition(d, e -> btnSpeak.setStyle(getStyleForImage("listen2.png")));

		listenTransition = new SequentialTransition();
		listenTransition.setCycleCount(SequentialTransition.INDEFINITE);
		listenTransition.getChildren().addAll(p1, p2);
	}

	private void initRecordingTransition() {
		Duration d = Duration.millis(MILLIS_ANIMATION_FRAME);

		PauseTransition p1 = newPauseTransition(d, (e) -> btnSpeak.setStyle(getStyleForImage("recording1.png")));

		PauseTransition p2 = newPauseTransition(d, (e) -> btnSpeak.setStyle(getStyleForImage("recording2.png")));

		PauseTransition p3 = newPauseTransition(d, (e) -> btnSpeak.setStyle(getStyleForImage("recording2.png")));

		recordingTransition = new SequentialTransition();
		recordingTransition.setCycleCount(SequentialTransition.INDEFINITE);
		recordingTransition.getChildren().addAll(p1, p2, p3);
	}

	private String getStyleForImage(String image) {
		return String.format("-fx-background-image: url(\"/gfx/%s\")", image);
	}

	private void initSpeech() {
		try {
			NuanceCredentials creds = NuanceCredentials.construct();
			nuanceClient = new NuanceClient(creds);
			LOG.info("Speech recognition initialized in: " + creds.getLang());

		} catch (IOException e) {
			LOG.error(ERROR_MSG, e);
		}
	}

	@Override
	public void speechRecognitionStarted(User u) {
		if (!u.equals(user))
			this.btnSpeak.setDisable(true);

		else if (!isProcessing) {
			btnNew.setDisable(true);
			startRecording();
		}

	}

	@Override
	public void speechRecognitionFinished(User u) {
		if (!isProcessing)
			return;

		if (u.equals(user)) {
			recordingTransition.stop();
			stopRecording();
			requestRecognition(u);

		} else
			btnSpeak.setDisable(false);
	}

	private void startRecording() {
		try {
			isProcessing = true;

			recording = File.createTempFile("conceptMapRecording", ".wav");
			recorder.record(recording);

			recordingTransition.play();

		} catch (IOException e) {
			LOG.error(ERROR_MSG, e);

		}
	}

	private void requestRecognition(User u) {
		try {

			listenTransition.play();
			nuanceClient.requestAsync(new FileInputStream(recording), CODEC, (c) -> {
				Platform.runLater(() -> {
					finishRecognition(u, c);
				});
			});
		} catch (FileNotFoundException e) {
			LOG.error(ERROR_MSG, e);

		}
	}

	private void finishRecognition(User u, Result c) {
		listenTransition.stop();

		PauseTransition p = new PauseTransition(Duration.millis(SHOW_RECOGNITION_RESULT_TIME));
		p.setOnFinished((e) -> {
			btnSpeak.setStyle("");
			btnNew.setDisable(false);

			isProcessing = false;
		});
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
			LOG.error(ERROR_MSG, e);
		}
	}

	public void setBinding(CollaborativeStringTextFieldBinding binding) {
		this.binding = binding;
	}
}
