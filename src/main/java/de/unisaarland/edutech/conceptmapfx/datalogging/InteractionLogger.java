package de.unisaarland.edutech.conceptmapfx.datalogging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

	private List<Row> rows = new ArrayList<Row>();

	private Instant startTime;

	private ConceptMap map;

	private static InteractionLogger self;

	private CSVExporter exporter;

	public static InteractionLogger getInstance() {
		if (self == null)
			self = new InteractionLogger();
		return self;
	}

	private InteractionLogger() {
		startTime = Instant.now();
		this.exporter = new CSVExporter();
		this.exporter.printProcessHeader();
	}

	public void setConceptMap(ConceptMap map) {
		this.map = map;
		this.exporter.printCoreData(map.getExperiment());
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

	public List<UserSummary> computeUserSummary() {

		List<UserSummary> res = new ArrayList<>();

		Map<String, Map<Event, List<Row>>> group = rows.stream()
				.collect(Collectors.groupingBy(Row::getEditingUser, Collectors.groupingBy(Row::getType)));

		group.entrySet().forEach(userEvents -> {
			UserSummary summaryRow = new UserSummary();
			summaryRow.setUser(userEvents.getKey());

			summaryRow.setConceptsCreated(getEventGroup(userEvents, Event.NEW_CONCEPT).size());
			summaryRow.setConceptDeletes(getEventGroup(userEvents, Event.DELETE_CONCEPT).size());
			summaryRow.setLinkCreates(getEventGroup(userEvents, Event.NEW_LINK).size());
			summaryRow.setLinkDeletes(getEventGroup(userEvents, Event.DELETE_LINK).size());
			summaryRow.setLinkEdits(getEventGroup(userEvents, Event.CONTENT_LINK).size());

			long ownConceptEdits = getEventGroup(userEvents, Event.CONTENT_CONCEPT).stream()
					.filter((r) -> r.getEditingUser().equals(r.getSrcConceptOwner())).count();

			long foreignConceptEdits = getEventGroup(userEvents, Event.CONTENT_CONCEPT).stream()
					.filter((r) -> !r.getEditingUser().equals(r.getSrcConceptOwner())).count();

			summaryRow.setForeignConceptsEdits(foreignConceptEdits);
			summaryRow.setOwnConceptsEdits(ownConceptEdits);

			res.add(summaryRow);

		});
		//

		return res;
	}

	private List<Row> getEventGroup(Entry<String, Map<Event, List<Row>>> userEvents, Event e) {
		return userEvents.getValue().getOrDefault(e, Collections.emptyList());
	}

	private Duration nextTime() {
		Duration d = Duration.between(startTime, Instant.now());
		return d;
	}

	private void addRow(Event e, Concept c, Concept c2, Link l, User u) {
		int ingoingC1 = (c == null) ? 0 : map.getIngoingLinkCount(c);
		int ingoingC2 = (c2 == null) ? 0 : map.getIngoingLinkCount(c2);

		int outgoingC1 = (c == null) ? 0 : map.getOutgoingLinkCount(c);
		int outgoingC2 = (c2 == null) ? 0 : map.getOutgoingLinkCount(c2);

		Row r = new Row(nextTime(), e, c, ingoingC1, outgoingC1, c2, ingoingC2, outgoingC2, l, u);
		rows.add(r);
		
		exporter.printProcessEntry(r);
		exporter.printUserSummary(this.computeUserSummary());

	}

}
