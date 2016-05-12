package de.unisaarland.edutech.conceptmapfx.datalogging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import de.unisaarland.edutech.conceptmapfx.SessionSaver;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;

public class InteractionLogger {

	public enum Event {
		NEW_CONCEPT, POSITION_CONCEPT, CONTENT_CONCEPT, DELETE_CONCEPT, NEW_LINK, CONTENT_LINK, DELETE_LINK, DIRECTION_LINK
	}

	private List<Row> rows = new ArrayList<InteractionLogger.Row>();

	private Instant startTime;

	private ConceptMap map;

	private CSVPrinter CSVprinter;

	private static InteractionLogger self;

	public static InteractionLogger getInstance() {
		if (self == null)
			self = new InteractionLogger();
		return self;
	}

	private InteractionLogger() {
		startTime = Instant.now();
		initCSVPrinter();

	}

	private void initCSVPrinter() {
		try {
			// TODO get filename from somewhere
			File wd = SessionSaver.getWorkingDir();

			CSVprinter = new CSVPrinter(new FileWriter(new File(wd, "data.csv")), CSVFormat.EXCEL);
			CSVprinter.print("Timestamp");
			CSVprinter.print("Event");

			printConceptHeader("Source");
			printConceptHeader("Linked");

			CSVprinter.print("Link_ID");
			CSVprinter.print("Link_Caption");
			CSVprinter.print("Interacting_User");
			CSVprinter.println();
		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}
	}

	private void printConceptHeader(String prefix) throws IOException {
		CSVprinter.print(prefix + "_Concept_ID");
		CSVprinter.print(prefix + "_Concept_Caption");
		CSVprinter.print(prefix + "_Concept_X");
		CSVprinter.print(prefix + "_Concept_Y");
		CSVprinter.print(prefix + "_Concept_R");
		CSVprinter.print(prefix + "_Concept_inlinks");
		CSVprinter.print(prefix + "_Concept_outlinks");
		CSVprinter.print(prefix + "_Concept_totallinks");
	}

	public void setConceptMap(ConceptMap map) {
		this.map = map;
	}

	// concept
	public void newConceptData(Concept c, User u) {
		addRow(Event.NEW_CONCEPT, c, null, null, u);
	}

	public void positionConceptData(Concept c) {
		addRow(Event.POSITION_CONCEPT, c, null, null, null);
	}

	public void contentConceptData(Concept c, User u) {
		addRow(Event.CONTENT_CONCEPT, c, null, null, u);
	}

	public void deleteConceptData(Concept c) {
		addRow(Event.DELETE_CONCEPT, c, null, null, null);
	}

	// link
	public void newLinkData(Concept c1, Concept c2, Link l) {
		addRow(Event.NEW_LINK, c1, c2, l, null);

	}

	public void contentLinkData(Link l, User u) {
		addRow(Event.CONTENT_LINK, null, null, l, u);
	}

	public void deleteLinkData(Concept c1, Concept c2, Link l) {
		addRow(Event.DELETE_LINK, c1, c2, l, null);
	}

	public void directionUpdateLinkData(Concept c1, Concept c2, Link l) {
		addRow(Event.DIRECTION_LINK, c1, c2, l, null);
	}

	private Duration nextTime() {
		Duration d = Duration.between(startTime, Instant.now());
		return d;
	}

	private void addRow(Event e, Concept c, Concept c2, Link l, User u) {
		Row r = new Row(nextTime(), e, c, c2, l, u);
		rows.add(r);

		try {
			r.printToCSV(CSVprinter);
		} catch (IOException e1) {
			// TODO error handling
			e1.printStackTrace();
		}
	}

	private class Row {

		private class ConceptData {
			public int id;
			public String caption;
			public double x;
			public double y;
			public double r;
			public int ingoingLinks;
			public int outgoingLinks;
			public int totalLinks;

			@Override
			public String toString() {
				return "ConceptData [id=" + id + ", caption=" + caption + ", x=" + x + ", y=" + y + ", r=" + r
						+ ", ingoingLinks=" + ingoingLinks + ", outgoingLinks=" + outgoingLinks + ", totalLinks="
						+ totalLinks + "]";
			}

		}

		public void printToCSV(CSVPrinter CSVprinter) throws IOException {
			CSVprinter.print(timestamp);
			CSVprinter.print(type);
			printConceptData(conceptData1);
			printConceptData(conceptData2);
			printLink(CSVprinter);
			if (user == null || user.trim().isEmpty())
				CSVprinter.print("null");
			else
				CSVprinter.print(user);

			CSVprinter.println();
			CSVprinter.flush();
		}

		private void printLink(CSVPrinter CSVprinter) throws IOException {
			CSVprinter.print(idL);
			if (labelL == null || labelL.trim().isEmpty())
				CSVprinter.print("null");
			else
				CSVprinter.print(labelL);

		}

		private void printConceptData(de.unisaarland.edutech.conceptmapfx.datalogging.InteractionLogger.Row.ConceptData c)
				throws IOException {
			if (c == null) {
				for (int i = 0; i < 8; i++)
					CSVprinter.print("null");
			} else {
				CSVprinter.print(c.id);
				CSVprinter.print(c.caption);
				CSVprinter.print(c.x);
				CSVprinter.print(c.y);
				CSVprinter.print(c.r);
				CSVprinter.print(c.ingoingLinks);
				CSVprinter.print(c.outgoingLinks);
				CSVprinter.print(c.totalLinks);
			}
		}

		private Duration timestamp;
		private Event type;

		// the first concept under edit
		private ConceptData conceptData1;

		// only set when we are linking TODO source or dest
		private ConceptData conceptData2;

		// the link under edit
		private int idL;
		private String labelL;

		// the user executing the event
		private String user;

		public Row(Duration timestamp, Event type, Concept c1, Concept c2, Link l, User u) {
			this.timestamp = timestamp;
			this.type = type;
			if (u != null)
				this.user = u.getEmail();
			if (l != null) {
				this.idL = l.hashCode();
				this.labelL = l.getCaption().getContent();
			}
			if (c1 != null) {
				conceptData1 = new ConceptData();
				extractConceptData(c1, this.conceptData1);
			}
			if (c2 != null) {
				conceptData2 = new ConceptData();
				extractConceptData(c2, this.conceptData2);
			}

		}

		private void extractConceptData(Concept c, ConceptData cd) {
			cd.id = c.hashCode();
			cd.caption = c.getName().getContent();
			cd.x = c.getX();
			cd.y = c.getY();
			cd.r = c.getRotate();
			cd.ingoingLinks = InteractionLogger.this.map.getIngoingLinkCount(c);
			cd.outgoingLinks = InteractionLogger.this.map.getOutgoingLinkCount(c);
			cd.totalLinks = cd.ingoingLinks + cd.outgoingLinks;

		}

		@Override
		public String toString() {
			return "Row [timestamp=" + timestamp + ", type=" + type + ", conceptData1=" + conceptData1
					+ ", conceptData2=" + conceptData2 + ", idL=" + idL + ", labelL=" + labelL + ", user=" + user + "]";
		}

	}

}
