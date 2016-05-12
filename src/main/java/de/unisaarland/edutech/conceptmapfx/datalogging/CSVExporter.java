package de.unisaarland.edutech.conceptmapfx.datalogging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import de.unisaarland.edutech.conceptmapfx.SessionSaver;
import de.unisaarland.edutech.conceptmapping.Experiment;

public class CSVExporter {

	private CSVPrinter processPrinter;
	private CSVPrinter summaryPrinter;
	private CSVPrinter corePrinter;
	private File wd;

	public CSVExporter() {
		wd = new File(SessionSaver.getWorkingDir(), "data");

		wd.mkdir();

		initProcessPrinter();
	}

	private void initUserSummaryPrinter() {
		try {
			summaryPrinter = new CSVPrinter(new FileWriter(new File(wd, "summaryData.csv")), CSVFormat.EXCEL);
		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}
	}

	private void initProcessPrinter() {
		try {
			processPrinter = new CSVPrinter(new FileWriter(new File(wd, "processData.csv")), CSVFormat.EXCEL);
		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}
	}

	private void initCorePrinter() {
		try {
			corePrinter = new CSVPrinter(new FileWriter(new File(wd, "coreData.csv")), CSVFormat.EXCEL);
		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}
	}

	public void printUserSummary(List<UserSummary> l) {
		initUserSummaryPrinter();
		printUserSummaryHeader();
		l.forEach(e -> printUserSummaryEntry(e));
		try {
			summaryPrinter.close();
		} catch (IOException e1) {
			// TODO error handling
			e1.printStackTrace();
		}
	}

	public void printCoreData(Experiment e) {
		initCorePrinter();
		printCoreHeader();
		printCore(e);
		try {
			corePrinter.close();
		} catch (IOException e1) {
			// TODO error handling
			e1.printStackTrace();
		}
	}

	private void printCore(Experiment e) {
		try {
			corePrinter.print(e.USER_COUNT);
			corePrinter.print(e.getFocusQuestion().getQuestion());
			corePrinter.print(e.getReseacher().getEmail());
			corePrinter.print(e.getRunDate().toGMTString());
			corePrinter.println();
			corePrinter.flush();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void printCoreHeader() {
		try {
			corePrinter.print("Participants");
			corePrinter.print("FocusQuestion");
			corePrinter.print("Experimenter");
			corePrinter.print("Date");
			corePrinter.println();
			corePrinter.flush();
		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}
	}

	public void printProcessHeader() {
		try {
			processPrinter.print("Timestamp");

			processPrinter.print("Event");

			printProcessConceptHeader("Source");
			printProcessConceptHeader("Linked");

			processPrinter.print("Link_ID");
			processPrinter.print("Link_Caption");
			processPrinter.print("Interacting_User");
			processPrinter.println();
			processPrinter.flush();
		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}
	}

	public void printUserSummaryHeader() {
		try {
			summaryPrinter.print("User");
			summaryPrinter.print("Concepts_Created");
			summaryPrinter.print("Own_Concepts_Edits");
			summaryPrinter.print("Foreign_Concepts_Edits");
			summaryPrinter.print("Concept_Deletes");
			summaryPrinter.print("Link_Creates");
			summaryPrinter.print("Link_Edits");
			summaryPrinter.print("Link_Deletes");

			summaryPrinter.println();
			summaryPrinter.flush();
		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}
	}

	public void printExperimentHeader() {

	}

	private void printProcessConceptHeader(String prefix) throws IOException {
		processPrinter.print(prefix + "_Concept_ID");
		processPrinter.print(prefix + "_Concept_Caption");
		processPrinter.print(prefix + "_Concept_X");
		processPrinter.print(prefix + "_Concept_Y");
		processPrinter.print(prefix + "_Concept_R");
		processPrinter.print(prefix + "_Concept_Owner");
		processPrinter.print(prefix + "_Concept_inlinks");
		processPrinter.print(prefix + "_Concept_outlinks");
		processPrinter.print(prefix + "_Concept_totallinks");
	}

	public void printUserSummaryEntry(UserSummary s) {
		try {
			summaryPrinter.print(s.getUser());

			summaryPrinter.print(s.getConceptsCreated());
			summaryPrinter.print(s.getOwnConceptsEdits());
			summaryPrinter.print(s.getForeignConceptsEdits());
			summaryPrinter.print(s.getConceptDeletes());

			summaryPrinter.print(s.getLinkCreates());
			summaryPrinter.print(s.getOwnLinkEdits());
			summaryPrinter.print(s.getLinkDeletes());

			summaryPrinter.println();
			summaryPrinter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printProcessEntry(Row r) {

		try {
			processPrinter.print(r.getTimestamp());

			processPrinter.print(r.getType());

			processPrinter.print(r.getSrcConceptId());
			processPrinter.print(r.getSrcConceptCaption());
			processPrinter.print(r.getSrcConceptX());
			processPrinter.print(r.getSrcConceptY());
			processPrinter.print(r.getSrcConceptR());
			processPrinter.print(r.getSrcConceptOwner());
			processPrinter.print(r.getSrcConceptIngoingLinks());
			processPrinter.print(r.getSrcConceptOutgoingLinks());
			processPrinter.print(r.getSrcConceptTotalLinks());

			processPrinter.print(r.getDestConceptId());
			processPrinter.print(r.getDestConceptCaption());
			processPrinter.print(r.getDestConceptX());
			processPrinter.print(r.getDestConceptY());
			processPrinter.print(r.getDestConceptR());
			processPrinter.print(r.getDestConceptOwner());
			processPrinter.print(r.getDestConceptIngoingLinks());
			processPrinter.print(r.getDestConceptOutgoingLinks());
			processPrinter.print(r.getDestConceptTotalLinks());

			processPrinter.print(r.getLinkId());
			processPrinter.print(r.getLinkLabel());

			processPrinter.print(r.getEditingUser());
			processPrinter.println();
			processPrinter.flush();

		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}
	}

}
