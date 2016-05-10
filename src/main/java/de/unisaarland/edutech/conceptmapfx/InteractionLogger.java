package de.unisaarland.edutech.conceptmapfx;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;

public class InteractionLogger {

	public enum Event {
		NEW_CONCEPT, POSITION_CONCEPT, CONTENT_CONCEPT, DELETE_CONCEPT, NEW_LINK, CONTENT_LINK, DELETE_LINK, ALIGN_DATA
	}

	private List<Row> rows = new ArrayList<InteractionLogger.Row>();

	private Instant startTime;

	private static InteractionLogger self;

	public static InteractionLogger getInstance() {
		if (self == null)
			self = new InteractionLogger();
		return self;
	}

	private InteractionLogger() {
		startTime = Instant.now();
	}

	// concept
	public void newConceptData(Concept c, User u) {
		Row r = new Row(nextTime(), Event.NEW_CONCEPT, c, null, null, u);
		rows.add(r);
	}

	public void positionConceptData(Concept c) {
		Row r = new Row(nextTime(), Event.POSITION_CONCEPT, c, null, null, null);
		rows.add(r);
	}

	public void contentConceptData(Concept c, User u) {
		Row r = new Row(nextTime(), Event.CONTENT_CONCEPT, c, null, null, u);
		rows.add(r);
	}

	public void deleteConceptData(Concept c) {
		Row r = new Row(nextTime(), Event.DELETE_CONCEPT, c, null, null, null);
		rows.add(r);
	}

	// link
	public void newLinkData(Concept c1, Concept c2, Link l) {
		Row r = new Row(nextTime(), Event.NEW_LINK, c1, c2, l, null);
		rows.add(r);
	}

	public void contentLinkData(Link l, User u) {
		Row r = new Row(nextTime(), Event.CONTENT_LINK, null, null, l, u);
		rows.add(r);
	}

	public void deleteLinkData(Link l) {
		Row r = new Row(nextTime(), Event.DELETE_LINK, null, null, l, null);
		rows.add(r);
	}

	// workspace
	public void alignData(User u) {
		Row r = new Row(nextTime(), Event.ALIGN_DATA, null, null, null, u);
		rows.add(r);
	}

	private Duration nextTime() {
		Duration d = Duration.between(startTime, Instant.now());
		return d;
	}

	private class Row {
		Duration timestamp;
		Event type;
		// the first concept under edit
		Concept c1;
		// only set when we are linking TODO source or dest
		Concept c2;
		// the link under edit
		Link l;
		// the user executing the event
		User u;

		public Row(Duration timestamp, Event type, Concept c1, Concept c2, Link l, User u) {
			this.timestamp = timestamp;
			this.type = type;
			this.c1 = c1;
			this.c2 = c2;
			this.l = l;
			this.u = u;
		}

		@Override
		public String toString() {
			return "Row [timestamp=" + timestamp + ", type=" + type + ", c1=" + c1 + ", c2=" + c2 + ", l=" + l + ", u="
					+ u + "]";
		}

	}

}
