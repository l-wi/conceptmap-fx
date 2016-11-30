/*******************************************************************************
 * conceptmap-fx a concept mapping prototype for research.
 * Copyright (C) Tim Steuer (master's thesis 2016)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, US
 *******************************************************************************/
package de.unisaarland.edutech.conceptmapfx.datalogging;

import java.time.Duration;

import de.unisaarland.edutech.conceptmapfx.datalogging.InteractionLogger.Event;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;

public class Row {

	private Duration timestamp;
	private Event type;

	// the first concept under edit
	private ConceptData conceptData1;

	// only set when we are linking 
	private ConceptData conceptData2;

	// the link under edit
	private int idL;
	private String labelL;

	// the user executing the event
	private String user;
	
	private double awarenessScore;

	public Row(Duration timestamp, Event type, Concept c1, int ingoingLinks1, int outgoingLinks1, Concept c2,
			int ingoingLinks2, int outgoingLinks2, Link l, User u) {
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
			conceptData1.ingoingLinks = ingoingLinks1;
			conceptData1.outgoingLinks = outgoingLinks1;
			conceptData1.totalLinks = ingoingLinks1 + outgoingLinks1;
		}
		if (c2 != null) {
			conceptData2 = new ConceptData();
			extractConceptData(c2, this.conceptData2);
			conceptData2.ingoingLinks = ingoingLinks2;
			conceptData2.outgoingLinks = outgoingLinks2;
			conceptData2.totalLinks = ingoingLinks2 + outgoingLinks2;
		}

	}

	private void extractConceptData(Concept c, ConceptData cd) {
		cd.id = c.hashCode();
		cd.caption = c.getName().getContent();
		cd.x = c.getX();
		cd.y = c.getY();
		cd.r = c.getRotate();
		cd.owner = c.getOwner().getEmail();
	}

	public Duration getTimestamp() {
		return timestamp;
	}

	public Event getType() {
		return type;
	}

	public int getSrcConceptId() {
		return (conceptData1 == null) ? 0 : conceptData1.id;
	}

	public String getSrcConceptCaption() {
		return (conceptData1 == null) ? "null" :conceptData1.caption;
	}

	public double getSrcConceptX() {
		return (conceptData1 == null) ? 0 :conceptData1.x;
	}

	public double getSrcConceptY() {
		return (conceptData1 == null) ? 0 :conceptData1.y;
	}

	public double getSrcConceptR() {
		return (conceptData1 == null) ? 0 :conceptData1.r;
	}

	public String getSrcConceptOwner() {
		return (conceptData1 == null) ? "null" :conceptData1.owner;
	}

	public int getSrcConceptIngoingLinks() {
		return (conceptData1 == null) ? 0 :conceptData1.ingoingLinks;
	}

	public int getSrcConceptOutgoingLinks() {
		return (conceptData1 == null) ? 0 :conceptData1.outgoingLinks;
	}

	public int getSrcConceptTotalLinks() {
		return (conceptData1 == null) ? 0 :conceptData1.totalLinks;
	}

	public int getDestConceptId() {
		return (conceptData2 == null) ? 0 :conceptData2.id;
	}

	public String getDestConceptCaption() {
		return (conceptData2 == null) ? "null" :conceptData2.caption;
	}

	public double getDestConceptX() {
		return (conceptData2 == null) ? 0 :conceptData2.x;
	}

	public double getDestConceptY() {
		return (conceptData2 == null) ? 0 :conceptData2.y;
	}

	public double getDestConceptR() {
		return (conceptData2 == null) ? 0 :conceptData2.r;
	}

	public String getDestConceptOwner() {
		return (conceptData2 == null) ? "null" :conceptData2.owner;
	}

	public int getDestConceptIngoingLinks() {
		return (conceptData2 == null) ? 0 :conceptData2.ingoingLinks;
	}

	public int getDestConceptOutgoingLinks() {
		return (conceptData2 == null) ? 0 :conceptData2.outgoingLinks;
	}

	public int getDestConceptTotalLinks() {
		return (conceptData2 == null) ? 0 :conceptData2.totalLinks;
	}

	public int getLinkId() {
		return idL;
	}

	public String getLinkLabel() {
		return labelL;
	}

	public String getEditingUser() {
		return (user == null) ? "anonymous" :user;
	}

	@Override
	public String toString() {
		return "Row [timestamp=" + timestamp + ", type=" + type + ", conceptData1=" + conceptData1 + ", conceptData2="
				+ conceptData2 + ", idL=" + idL + ", labelL=" + labelL + ", user=" + user + "]";
	}

	public double getAwarenessScore() {
		return awarenessScore;
	}

	public void setAwarenessScore(double awarenessScore) {
		this.awarenessScore = awarenessScore;
	}

	private class ConceptData {
		public int id;
		public String caption;
		public double x;
		public double y;
		public double r;
		public String owner;
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

}
