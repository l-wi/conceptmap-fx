package de.unisaarland.edutech.conceptmapfx;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapping.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class UserPickerView extends VBox {

	private static final Logger LOG = LoggerFactory.getLogger(ExaminerLoginController.class);

	@FXML
	private String userFilePath;

	private File workingFile;

	private List<User> users;

	private User activeUser;

	private boolean isNewUser;

	@FXML
	private ComboBox<String> cmbUser;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtEmail;
	@FXML
	private ImageView error;
	@FXML
	private Label lblStatus;

	@FXML
	private String btnLoginId;

	private Button btnLogin;

	public UserPickerView() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserPickerView.fxml"));
		loader.setRoot(this);
		loader.setController(this);

	}

	@FXML
	public void initialize() {
		workingFile = new File(userFilePath);
		btnLogin = (Button) this.lookup(btnLoginId);

		btnLogin.setDisable(true);
		txtName.setDisable(true);
		txtEmail.setDisable(true);
		error.setVisible(false);

		loadUsers();
		initComboBox();
		initTxtFields();

		initComboBoxListener();
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

	
}
