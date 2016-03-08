package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.util.function.Consumer;

import org.comtel2000.keyboard.control.KeyboardPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXML;
import javafx.scene.Parent;

public class LoginController {

	private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

	@FXML
	private Parent root;
	
	@FXML
	private KeyboardPane keyboard;

	private Consumer<User> nextFunction;

	@FXML
	private UserPickerView picker;

	@FXML
	public void initialize() {
		initKeyboard();
		initLoginClick();

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

	private void initLoginClick() {
		picker.setOnAction((u) -> {

			fadeOut(u);
		});
	}

	private void fadeOut(User u) {
		nextFunction.accept(u);
	}

	public void setNext(Consumer<User> func) {
		this.nextFunction = func;
	}

	public Parent getView() {
		//TODO call this not when getting view but on a better place, e.g. when controller is added to view
		picker.loadUsers();
		return root;
	}
	
	public void setPrompt(String prompt){
		picker.setPrompt(prompt);
	}
	
	public void addImageCSSClass(String css){
		picker.addImageCSSClass(css);
	}
}
