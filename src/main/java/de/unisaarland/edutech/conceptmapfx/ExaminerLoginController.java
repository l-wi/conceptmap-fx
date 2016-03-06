package de.unisaarland.edutech.conceptmapfx;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.comtel2000.keyboard.control.KeyboardPane;
import org.comtel2000.keyboard.xml.layout.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapping.User;
import de.unisaarland.edutech.conceptmapping.exception.EmailException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class ExaminerLoginController {

	private static final Logger LOG = LoggerFactory.getLogger(ExaminerLoginController.class);

	@FXML
	private ComboBox<String> cmbUser;

	@FXML
	private ImageView error;

	@FXML
	private TextField txtName;
	@FXML
	private TextField txtEmail;
	@FXML
	private String nothingSelected;
	@FXML
	private Button btnLogin;
	@FXML
	private Label lblStatus;
	@FXML
	private KeyboardPane keyboard;

	private List<User> users;

	private File workingFile = new File("./profiles/examiners.ostream");

	private User activeUser;

	private boolean isNewUser;

	private Consumer<User> nextFunction;

	@FXML
	public void initialize() {
		btnLogin.setDisable(true);
		txtName.setDisable(true);
		txtEmail.setDisable(true);
		error.setVisible(false);
		loadUsers();
		initComboBox();
		initTxtFields();
		initKeyboard();
		initComboBoxListener();
		initLoginClick();

	}

	private void initKeyboard() {
		keyboard.setKeyBoardStyle(getClass().getResource("/input.css").toString());
		keyboard.setSpaceKeyMove(false);
		keyboard.setLayerPath(new File("./keyboardLayout").toPath());
		try {
			keyboard.load();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initLoginClick() {
		btnLogin.setOnAction((e) -> {
			String name = txtName.getText();
			String email = txtEmail.getText();

			try {
				this.activeUser = new User(name, email);

				if (isNewUser)
					appendUser();

				fadeOut();

			} catch (EmailException ex) {
				error.setVisible(true);
				lblStatus.setVisible(true);
			}
		});
	}

	private void fadeOut() {
		nextFunction.accept(activeUser);
	}

	private void appendUser() {
		try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(workingFile, true))) {
			stream.writeObject(activeUser);
		} catch (IOException ex) {
			LOG.error("could not save user!", ex);
		}
	}

	private void initTxtFields() {
		txtName.textProperty().addListener((c, o, n) -> {
			btnLogin.setDisable(n.trim().isEmpty() || txtEmail.getText().trim().isEmpty());
			error.setVisible(false);
			lblStatus.setVisible(false);
		});
		txtEmail.textProperty().addListener((c, o, n) -> {
			btnLogin.setDisable(n.trim().isEmpty() || txtName.getText().trim().isEmpty());
			error.setVisible(false);
			lblStatus.setVisible(false);
		});
	}

	private void loadUsers() {
		users = new ArrayList<>();

		try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(workingFile))) {

			User u = null;

			while ((u = (User) stream.readObject()) != null)
				users.add(u);

		} catch (EOFException ex) {
			LOG.info("finished reading user input!");
		} catch (IOException | ClassNotFoundException e) {
			LOG.error("could not load existing users!", e);
		}

	}

	private void initComboBox() {
		List<String> usersAsString = users.stream().map((u) -> u.toString()).collect(Collectors.toList());
		cmbUser.getItems().addAll(usersAsString);
	}

	private void initComboBoxListener() {
		cmbUser.getSelectionModel().selectedIndexProperty().addListener((s, o, n) -> {
			if (n.intValue() == 0) {
				toNewState();
			} else
				toExistingState(n.intValue());
		});
	}

	private void toExistingState(int n) {
		txtName.setDisable(true);
		txtEmail.setDisable(true);

		txtName.setText(users.get(n - 1).getName());
		txtEmail.setText(users.get(n - 1).getEmail());
		isNewUser = false;
	}

	private void toNewState() {
		txtName.setDisable(false);
		txtEmail.setDisable(false);

		txtName.setText("");
		txtEmail.setText("");

		isNewUser = true;
	}

	public User getActiveUser() {
		return activeUser;
	}

	public void setNext(Consumer<User> func) {
		this.nextFunction = func;
	}

}
