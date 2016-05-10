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
			String currentQuestion = cmbFocusQuestion.getSelectionModel().getSelectedItem();

			if(currentQuestion == null)
				currentQuestion = cmbFocusQuestion.getEditor().getText();
			
			currentQuestion.trim();
			
			FocusQuestion q = updateQuestionList(currentQuestion);

			rewriteQuestionList();
		
			Integer userCount = Integer.valueOf(userPicker.getValue());
			
			next.accept(new Experiment(researcher, q,userCount));

		});
	}

	private FocusQuestion updateQuestionList(String currentQuestion) {

		Optional<FocusQuestion> question = this.available.stream()
				.filter((f) -> f.getQuestion().equals(currentQuestion)).findFirst();

		if (question.isPresent()) {
			int index = this.available.indexOf(question.get());
			Collections.rotate(available.subList(0, index+1), 1);
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
		cmbFocusQuestion.getEditor().textProperty().addListener((c, o, n) -> btnRun.setDisable(n.trim().isEmpty()));

		cmbFocusQuestion.getSelectionModel().clearSelection();
		cmbFocusQuestion.setFocusTraversable(false);
	}

	private void initKeyboard() {
		keyboard.setKeyBoardStyle(getClass().getResource("/css/input.css").toString());
		keyboard.setSpaceKeyMove(false);
		keyboard.setLayerPath(new File("./keyboardLayout").toPath());

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
