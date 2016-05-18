package de.unisaarland.edutech.conceptmapfx.datalogging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.SessionSaver;
import de.unisaarland.edutech.conceptmapfx.preparation.LoginController;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.User;

public class CSVExporter {

	private static final Logger LOG = LoggerFactory.getLogger(CSVExporter.class);

	public static final String CORE_FILE_NAME = "coreData.csv";
	public static final String PROCESS_FILE_NAME = "processData.csv";
	public static final String SUMMARY_FILE_NAME = "summaryData.csv";
	public static final String DATA_DIR_NAME = "data";
	private CSVPrinter processPrinter;
	private CSVPrinter summaryPrinter;
	private CSVPrinter corePrinter;
	private File wd;

	public CSVExporter() {
		wd = new File(SessionSaver.getWorkingDir(), DATA_DIR_NAME);

		wd.mkdir();

		processPrinter = initPrinter(PROCESS_FILE_NAME);
	}

	private CSVPrinter initPrinter(String filename) {
		try {
			return new CSVPrinter(new FileWriter(new File(wd, filename)), CSVFormat.EXCEL);
		} catch (IOException e) {
			throw new RuntimeException("Cannot log data! ", e);
		}
	}

	public void printUserSummary(Collection<UserSummary> collection) {
		summaryPrinter = initPrinter(SUMMARY_FILE_NAME);
		printUserSummaryHeader();
		collection.forEach(e -> printUserSummaryEntry(e));
		closePrinter(summaryPrinter);
	}

	private void closePrinter(CSVPrinter printer) {
		try {
			printer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void printCoreData(Experiment e) {
		corePrinter = initPrinter(CORE_FILE_NAME);
		printCoreHeader();
		printCore(e);
		closePrinter(corePrinter);
	}

	private void printCore(Experiment e) {
		try {
			corePrinter.print(e.USER_COUNT);
			corePrinter.print(e.getFocusQuestion().getQuestion());
			corePrinter.print(e.getReseacher().getEmail());
			corePrinter.print(e.getRunDate().toGMTString());
			corePrinter.print(e.USE_AWT);
			corePrinter.print(e.USE_VOTING);

			List<User> participants = e.getParticipants();

			corePrinter.print(participants.get(0).getEmail());
			corePrinter.print(participants.get(1).getEmail());
			corePrinter.print(participants.size() > 2 ? participants.get(2).getEmail() : "null");
			corePrinter.print(participants.size() > 3 ? participants.get(3).getEmail() : "null");

			corePrinter.print(getPrompt(participants, 0));
			corePrinter.print(getPrompt(participants, 1));
			corePrinter.print(getPrompt(participants, 2));
			corePrinter.print(getPrompt(participants, 3));

		} catch (IOException ex) {
			LOG.error("Cannot write data", ex);
		}
	}

	private String getPrompt(List<User> participants, int i) {
		if (participants.size() <= i)
			return "null";

		String prompt = participants.get(i).getPrompt();
		return (prompt == null) ? "null" : prompt;
	}

	private void printCoreHeader() {
		try {
			corePrinter.print("Participants");
			corePrinter.print("FocusQuestion");
			corePrinter.print("Experimenter");
			corePrinter.print("Date");
			corePrinter.print("AWT_used");
			corePrinter.print("Votes_used");
			corePrinter.print("user_top");
			corePrinter.print("user_bottom");
			corePrinter.print("user_left");
			corePrinter.print("user_right");
			corePrinter.print("prompts_top");
			corePrinter.print("prompts_bottom");
			corePrinter.print("prompts_left");
			corePrinter.print("prompts_right");
			corePrinter.println();
			corePrinter.flush();
		} catch (IOException e) {
			LOG.error("Cannot write data", e);
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
			processPrinter.print("Interacting_User_Awareness_Score");

			processPrinter.println();
			processPrinter.flush();
		} catch (IOException e) {
			LOG.error("Cannot write data", e);
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
			summaryPrinter.print("Awareness_score");
			summaryPrinter.print("Votes");

			summaryPrinter.println();
			summaryPrinter.flush();
		} catch (IOException e) {
			LOG.error("Cannot write data", e);
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

			summaryPrinter.print(s.getLinkCount());
			summaryPrinter.print(s.getLinkEdits());
			summaryPrinter.print(s.getLinkDeletes());
			summaryPrinter.print(s.getAwarenessScore());
			summaryPrinter.print(s.getVotingCount());

			summaryPrinter.println();
			summaryPrinter.flush();
		} catch (IOException e) {
			LOG.error("Cannot write data", e);
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
			processPrinter.print(r.getAwarenessScore());
			processPrinter.println();
			processPrinter.flush();

		} catch (IOException e) {
			LOG.error("Cannot write data", e);
		}
	}

}
