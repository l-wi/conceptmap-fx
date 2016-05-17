package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.unisaarland.edutech.conceptmapfx.observablemap.ConceptMapObserver;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptMap;

public class SessionSaver implements ConceptMapObserver {

	private static final int SNAPSHOT_TIME = 1000 * 30;

	private static final String FOLDER = "./session";

	private ObservableConceptMap conceptMap;

	private static File workingDir;

	private static File binaryDir;

	private static File cxlDir;

	private int counter = 1;

	private boolean isSetup;

	private boolean hasUnsavedChanges;

	private CXLExporter cxlExporter;

	public SessionSaver(ObservableConceptMap conceptMap) {
		this.conceptMap = conceptMap;

		this.cxlExporter = new CXLExporter();
		isSetup = true;

		getWorkingDir();
		getCXLDir();
		getBinaryDir();

		try {
			serialize();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static File getWorkingDir() {
		if (workingDir == null) {
			Date d = new Date();
			String dateSuffix = new SimpleDateFormat("yyyyMMddHHmmss").format(d);
			workingDir = new File(FOLDER + "/" + dateSuffix);
			if (!workingDir.mkdirs())
				throw new RuntimeException("could not create session dir  " + workingDir);
		}
		return workingDir;
	}

	public static File getBinaryDir() {
		if (binaryDir == null) {
			binaryDir = new File(getWorkingDir(), "binary");
			binaryDir.mkdir();
		}
		return binaryDir;
	}

	public static File getCXLDir() {
		if (cxlDir == null) {
			cxlDir = new File(getWorkingDir(), "cxl");
			cxlDir.mkdir();
		}
		return cxlDir;
	}

	private void startTimer() {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				if (hasUnsavedChanges)
					try {
						serialize();
						export();
						hasUnsavedChanges = false;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
			}

		}, 0, 5000);
	}

	public void activate() {
		this.isSetup = false;
		startTimer();
	}

	@Override
	public void beforeChange() {

	}

	private void export() {
		File f = nextCXLFile();
		cxlExporter.export(f, conceptMap);
	}

	private void serialize() throws IOException {
		ObjectOutputStream outputter = new ObjectOutputStream(new FileOutputStream(nextSerializedFile()));
		outputter.writeObject(conceptMap);
		outputter.close();
	}

	private File nextSerializedFile() {

		return new File(binaryDir, String.valueOf(counter++) + ".cmap");
	}

	private File nextCXLFile() {
		return new File(cxlDir, String.valueOf(counter) + ".cxl");

	}

	@Override
	public void afterChange() {
		if (!isSetup) {
			hasUnsavedChanges = true;
		}
	}

}
