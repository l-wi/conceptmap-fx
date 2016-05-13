package de.unisaarland.edutech.conceptmapfx.datalogging;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisaarland.edutech.conceptmapfx.awt.AWTFormula;
import de.unisaarland.edutech.conceptmapfx.awt.AwarenessBars;
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

	private List<AwarenessBars> awts = new ArrayList<>();

	private AWTFormula awtFormula;

	private Map<String, UserSummary> stats = new HashMap<>();

	private UserSummary totalSummary = new UserSummary();

	public static InteractionLogger getInstance() {
		if (self == null)
			self = new InteractionLogger();
		return self;
	}

	private InteractionLogger() {
		startTime = Instant.now();
		this.exporter = new CSVExporter();
		this.exporter.printProcessHeader();
		this.stats.put("total", totalSummary);
	}

	public void bindAWT(AwarenessBars bars) {
		this.awts.add(bars);
		this.awtFormula = new AWTFormula();
	}

	public void setConceptMap(ConceptMap map) {
		this.map = map;
		this.exporter.printCoreData(map.getExperiment());
	}

	// concept
	public void newConceptData(Concept c, User u) {

		UserSummary userSummary = getUserSummaryForUser(u);

		userSummary.incConceptsCreated();
		totalSummary.incConceptsCreated();

		addRow(Event.NEW_CONCEPT, c, null, null, u);

	}

	public void positionConceptData(Concept c) {
		addRow(Event.POSITION_CONCEPT, c, null, null, null);
	}

	public void contentConceptData(Concept c, User u) {

		UserSummary userSummary = getUserSummaryForUser(u);

		if (c.getOwner().equals(u)) {
			userSummary.incOwnConceptEdits();
			totalSummary.incOwnConceptEdits();
		} else {
			userSummary.incForeignConceptEdits();
			totalSummary.incForeignConceptEdits();
		}

		addRow(Event.CONTENT_CONCEPT, c, null, null, u);

	}

	public void deleteConceptData(Concept c) {

		UserSummary userSummary = getUserSummaryForUser(c.getOwner());
		userSummary.incConceptDeletes();
		totalSummary.incConceptDeletes();

		addRow(Event.DELETE_CONCEPT, c, null, null, null);

	}

	// link
	public void newLinkData(Concept c1, Concept c2, Link l) {

		UserSummary userSummary = getUserSummaryForUser(c1.getOwner());
		userSummary.incLinkCount();
		totalSummary.incLinkCount();

		if (!c1.getOwner().equals(c2.getOwner())) {
			userSummary = getUserSummaryForUser(c2.getOwner());
			userSummary.incLinkCount();
		}

		addRow(Event.NEW_LINK, c1, c2, l, null);

	}

	public void contentLinkData(Link l, User u) {
		UserSummary userSummary = getUserSummaryForUser(u);

		userSummary.incLinkEdits();
		totalSummary.incLinkEdits();

		addRow(Event.CONTENT_LINK, null, null, l, u);

	}

	public void deleteLinkData(Concept c1, Concept c2, Link l) {

		UserSummary userSummary = getUserSummaryForUser(c1.getOwner());
		userSummary.incLinkDeletes();
		totalSummary.incLinkDeletes();

		if (!c1.getOwner().equals(c2.getOwner())) {
			userSummary = getUserSummaryForUser(c2.getOwner());
			userSummary.incLinkDeletes();
		}

		addRow(Event.DELETE_LINK, c1, c2, l, null);

	}

	public void directionUpdateLinkData(Concept c1, Concept c2, Link l) {
		addRow(Event.DIRECTION_LINK, c1, c2, l, null);
	}

	private UserSummary getUserSummaryForUser(User u) {
		addUserToSummaryIfNotExisting(u);
		return stats.get(u.getEmail());

	}

	private void addUserToSummaryIfNotExisting(User u) {
		stats.putIfAbsent(u.getEmail(), new UserSummary());
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
		exporter.printUserSummary(stats.values());

		if (awtFormula == null)
			return;

		long vT = computeAWTValue(totalSummary);

		for (AwarenessBars bars : awts) {
			List<User> participants = map.getExperiment().getParticipants();

			for (int i = 0; i < participants.size(); i++) {
				String user = participants.get(i).getEmail();
				UserSummary userSummary = stats.get(user);

				long vU = 0;
				if (userSummary != null)
					vU = computeAWTValue(userSummary);

				double v = vU * 1.0 / vT;
				bars.setValue(i, v);
			}

		}

	}

	private long computeAWTValue(UserSummary userSummary) {
		return awtFormula.compute(userSummary.getConceptsCreated(), userSummary.getOwnConceptsEdits(),
				userSummary.getForeignConceptsEdits(), userSummary.getLinkEdits(), userSummary.getLinkCount());
	}

}
