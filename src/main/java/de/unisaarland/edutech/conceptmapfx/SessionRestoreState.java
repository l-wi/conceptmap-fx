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

	public void handleRestoreState(Stage primaryStage, SessionSaver sessionSaver) {
		try {
			File workingDir = sessionSaver.getWorkingDir();
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
		File sessionFolder = new File("./session");

		if (!isDirectory(sessionFolder))
			sessionFolder.mkdir();

		Optional<File[]> files = getFiles(sessionFolder);

		if (!files.isPresent())
			return Optional.empty();

		for (File d : files.get()) {
			if (isDirectory(sessionFolder)) {
				Optional<File[]> directoryContentsOptinal = sortContentsNumerical(d);

				File[] directoryContents = null;

				if (directoryContentsOptinal.isPresent()
						&& (directoryContents = directoryContentsOptinal.get()).length > 0) {

					if (directoryContents[0].getName().equals(RESTORE_FILE_NAME)) {
						directoryContents[0].delete();
						File currentState = directoryContents[directoryContents.length - 1];
						try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(currentState))) {
							ObservableConceptMap cm = (ObservableConceptMap) stream.readObject();
							return Optional.of(cm);

						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		}

		return Optional.empty();
	}

	private Optional<File[]> sortContentsNumerical(File d) {
		Optional<File[]> directoryContents = getFiles(d);

		if (!directoryContents.isPresent())
			return Optional.empty();

		Comparator<File> c = (a, b) -> {

			String nameA = a.getName();
			String nameB = b.getName();

			int n1 = nameAsInt(nameA);
			int n2 = nameAsInt(nameB);

			return n1 - n2;
		};

		Arrays.sort(directoryContents.get(), c);
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

		return Integer.parseInt(nameWithoutEnding);

	}

	private Optional<File[]> getFiles(File f) {
		File[] fileList = f.listFiles();
		return Optional.ofNullable(fileList);
	}
}