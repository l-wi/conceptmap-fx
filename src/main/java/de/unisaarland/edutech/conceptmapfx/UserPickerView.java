package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapping.User;
import de.unisaarland.edutech.conceptmapping.exception.EmailException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class UserPickerView extends VBox {

	private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

	private String userFilePath;

	private File workingFile;

	private List<User> users;

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
	private Button btnLogin;
	@FXML
	private AnchorPane imagePanel;

	@FXML
	private ImageView image;

	private User activeUser;

	private boolean isWriteUsersOnAction = true;

	private String imageURL = "/researcher.png";

	private double imgWidth;

	private double imgHeight;

	private String prompt;

	public UserPickerView() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserPickerView.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		tryLoadingFXMLOrThrow(loader);

		initiailze();

	}

	private void tryLoadingFXMLOrThrow(FXMLLoader loader) {
		try {
			loader.load();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void initiailze() {
		btnLogin.setDisable(true);
		txtName.setDisable(true);
		txtEmail.setDisable(true);
		error.setVisible(false);

		initTxtFields();
		initComboBoxListener();

	}

	public void setUserFilePath(String userFilePath) {

		this.userFilePath = userFilePath;
		workingFile = new File(userFilePath);


	}

	public void setWriteUsersOnAction(boolean b) {
		this.isWriteUsersOnAction = b;
	}

	public boolean isWriteUsersOnAction() {
		return isWriteUsersOnAction;
	}

	public String getUserFilePath() {
		return userFilePath;
	}

	public void loadUsers() {
		
		users = new ArrayList<>();

		try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(workingFile))) {
			users = (List<User>) stream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			LOG.error("could not load existing users!", e);
		}
		
		initComboBox();
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

	public void setOnAction(Consumer<User> c) {

		btnLogin.setOnAction((e) -> {
			String name = this.getName();
			String email = this.getEMail();

			try {
				this.activeUser = new User(name, email);
				this.users.add(0,activeUser);
				if (isWriteUsersOnAction && this.isNewUser())
					appendUser();

				c.accept(activeUser);
			} catch (EmailException ex) {
				this.showError();
			}
		});
	}

	private void showError() {
		error.setVisible(true);
		lblStatus.setVisible(true);
	}

	public String getEMail() {
		return txtEmail.getText();
	}

	public String getName() {
		return txtName.getText();
	}

	public boolean isNewUser() {
		return isNewUser;
	}

	private void appendUser() {
		try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(workingFile))) {
			stream.writeObject(users);
		} catch (IOException ex) {
			LOG.error("could not save user!", ex);
		}
	}

	public double getImgHeight() {
		return imgHeight;
	}

	public double getImgWidth() {
		return imgWidth;
	}

	public void setImgHeight(double imgHeight) {
		this.imgHeight = imgHeight;
		this.image.setFitHeight(imgHeight);
	}

	public void setImgWidth(double imgWidth) {
		this.imgWidth = imgWidth;
		this.image.setFitWidth(imgWidth);
	}

	public void setPrompt(String prompt) {
		this.cmbUser.setPromptText(prompt);
	}

	public String getPrompt() {
		return this.cmbUser.getPromptText();
	}

	public void addImageCSSClass(String css) {
		this.imagePanel.getStyleClass().add(css);
	}

}
