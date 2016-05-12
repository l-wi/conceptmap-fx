package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptMap;
import javafx.stage.Stage;

public class SessionRestoreState {

	private File restoreFile;

	private static final String RESTORE_FILE_NAME = "lock";

	public void handleRestoreState(Stage primaryStage) {
		try {
			File workingDir = SessionSaver.getWorkingDir();
			this.restoreFile = new File(workingDir, RESTORE_FILE_NAME);

			restoreFile.createNewFile();

			primaryStage.setOnCloseRequest((e) -> {
				restoreFile.delete();
				System.exit(0);
			});

		} catch (IOException exception) {
			throw new RuntimeException("Cannot create restore file! " + exception);
		}
	}

	public Optional<ObservableConceptMap> restoreSessionIfNeeded() {
		File rootFolder = new File("./session");

		if (!isDirectory(rootFolder))
			rootFolder.mkdir();

		File[] sessionFolders = rootFolder.listFiles();

		for (File sessionFolder : sessionFolders) {

			if (!isDirectory(sessionFolder))
				continue;

			File[] sessionContents = sortContentsNumerical(sessionFolder);

			Optional<File> lockFileOptional = Arrays.stream(sessionContents)
					.filter(f -> f.getName().equals(RESTORE_FILE_NAME)).findFirst();

			if (!lockFileOptional.isPresent())
				continue;

			lockFileOptional.get().delete();

			File currentState = getCurrentState(sessionContents);
			return loadConceptMap(currentState);

		}

		return Optional.empty();

	}

	private Optional<ObservableConceptMap> loadConceptMap(File currentState) {
		try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(currentState))) {
			ObservableConceptMap cm = (ObservableConceptMap) stream.readObject();
			return Optional.of(cm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File getCurrentState(File[] directoryContents) {
		for (int i = directoryContents.length - 1; i >= 0; i--) {
			if (directoryContents[i].getName().contains(".cmap"))
				return directoryContents[i];
		}
		throw new RuntimeException("No restore state found!");
	}

	private File[] sortContentsNumerical(File d) {
		File[] directoryContents = d.listFiles();

		Comparator<File> c = (a, b) -> {

			String nameA = a.getName();
			String nameB = b.getName();

			int n1 = nameAsInt(nameA);
			int n2 = nameAsInt(nameB);

			return n1 - n2;
		};

		Arrays.sort(directoryContents, c);
		return directoryContents;
	}

	private boolean isDirectory(File r) {
		return r.exists() && r.isDirectory();
	}

	private int nameAsInt(String nameA) {

		int index = nameA.indexOf(".");
		if (index == -1)
			return -1000;
		String nameWithoutEnding = nameA.substring(0, index);
		try {
			return Integer.parseInt(nameWithoutEnding);
		} catch (NumberFormatException e) {
			return -1000;
		}

	}

}
