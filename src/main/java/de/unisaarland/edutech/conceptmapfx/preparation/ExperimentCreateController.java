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
package de.unisaarland.edutech.conceptmapfx.preparation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.comtel2000.keyboard.control.KeyboardPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.FocusQuestion;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

public class ExperimentCreateController {

	private static final Logger LOG = LoggerFactory.getLogger(ExperimentCreateController.class);

	private Consumer<Experiment> next;
	private User researcher;

	@FXML
	private Parent root;

	@FXML
	private KeyboardPane keyboard;

	@FXML
	private ComboBox<String> cmbFocusQuestion;

	@FXML
	private ComboBox<String> userPicker;

	@FXML
	private Button btnRun;

	@FXML
	private CheckBox useAwt;

	@FXML
	private CheckBox useVoting;
	
	private List<FocusQuestion> available = new ArrayList<>();

	private File workingFile = new File("profiles/focusQuesions.ostream");

	@FXML
	public void initialize() {
		btnRun.setDisable(true);
		initKeyboard();
		loadFocusQuestions();
		initCmbFocusQuestion();
		initBtnRun();
	}

	private void initBtnRun() {
		btnRun.setOnAction((e) -> {
			String currentQuestion = cmbFocusQuestion.getValue().trim();
			FocusQuestion q = updateQuestionList(currentQuestion);

			rewriteQuestionList();

			Integer userCount = Integer.valueOf(userPicker.getValue());
			boolean awt = this.useAwt.isSelected();
			boolean useVoting = this.useVoting.isSelected();

			next.accept(new Experiment(researcher, q, userCount, awt, useVoting));

		});
	}

	private FocusQuestion updateQuestionList(String currentQuestion) {

		Optional<FocusQuestion> question = this.available.stream()
				.filter((f) -> f.getQuestion().equals(currentQuestion)).findFirst();

		if (question.isPresent()) {
			int index = this.available.indexOf(question.get());
			Collections.rotate(available.subList(0, index + 1), 1);
		} else {
			question = Optional.of(new FocusQuestion(currentQuestion, researcher));
			available.add(0, question.get());
		}
		return question.get();

	}

	private void rewriteQuestionList() {
		try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(workingFile))) {
			stream.writeObject(this.available);
			stream.flush();
		} catch (IOException e) {
			LOG.error("could not save focus questions for reuse!", e);

		}

	}

	private void loadFocusQuestions() {
		try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(workingFile))) {
			this.available = (List) stream.readObject();

		} catch (IOException | ClassNotFoundException e) {
			LOG.error("could not load old focus questions!", e);
		}

	}

	private void initCmbFocusQuestion() {

		List<String> questionStrings = available.stream().map((f) -> f.getQuestion()).collect(Collectors.toList());
		cmbFocusQuestion.getItems().addAll(questionStrings);
		cmbFocusQuestion.getSelectionModel().select(0);
		cmbFocusQuestion.autosize();
		cmbFocusQuestion.getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {

		});
		cmbFocusQuestion.getEditor().textProperty().addListener((c, o, n) -> {
			btnRun.setDisable(n.trim().isEmpty());
			cmbFocusQuestion.setValue(n);
		});

		cmbFocusQuestion.getSelectionModel().clearSelection();
		cmbFocusQuestion.setFocusTraversable(false);
	}

	private void initKeyboard() {
		keyboard.setKeyBoardStyle(getClass().getResource("/css/input.css").toString());
		keyboard.setSpaceKeyMove(false);
		keyboard.setLayerPath(new File("./keyboardLayout").toPath());
		keyboard.setScale(1.2);

		try {
			keyboard.load();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setNext(Consumer<Experiment> next) {
		this.next = next;
	}

	public void setResearcher(User researcher) {
		this.researcher = researcher;
	}

	public Parent getView() {
		return root;
	}

}
