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
import java.util.function.Consumer;

import org.comtel2000.keyboard.control.KeyboardPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.prompts.PromptLoader;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class LoginController {

	private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

	@FXML
	private VBox usersBox;

	@FXML
	private Parent root;

	@FXML
	private KeyboardPane keyboard;

	private Consumer<User> nextFunction;

	private ComboBox<String> cmbInstruction;

	@FXML
	private UserPickerView picker;

	@FXML
	public void initialize() {
		initKeyboard();
		initLoginClick();
	}

	public void usePrompts(PromptLoader promptLoader) {
		cmbInstruction = new ComboBox<String>(FXCollections.observableList(promptLoader.getAvailablePrompts()));
		cmbInstruction.setVisible(true);
		cmbInstruction.setPromptText("Optional: Select a user prompt");
		usersBox.getChildren().add(cmbInstruction);

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

	private void initLoginClick() {
		picker.setOnAction((u) -> {

			String prompt = (cmbInstruction != null) ? this.cmbInstruction.getValue() : null;
			u.setPrompt(prompt);
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
		picker.loadUsers();
		return root;
	}

	public void setPrompt(String prompt) {
		picker.setPrompt(prompt);
	}

	public void addImageCSSClass(String css) {
		picker.addImageCSSClass(css);
	}

}
