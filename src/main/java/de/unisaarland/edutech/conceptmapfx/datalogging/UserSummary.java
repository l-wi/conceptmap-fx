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

public class UserSummary {

	private String user;
	//how many concepts did the user create
	private long conceptsCreated;
	//how many own concepts did the user edit
	private long ownConceptsEdits;
	//how many foreign concepts did the user edit
	private long foreignConceptsEdits;
	//how many concepts of the user have been deleted
	private long conceptDeletes;
	//how many links created include at least one concept of the user 
	private long linkCount;
	//how many links did the user edit
	private long linkEdits;
	//how many links associated with a concept of the user have been deleted
	private long linkDeletes;
	//what was the awareness score of the user
	private double awarenessScore;
	//how often did the user vote
	private int votingCount;

	public void setUser(String user) {
		this.user = user;
	}

	public void setConceptsCreated(long long1) {
		this.conceptsCreated = long1;
	}

	public void incConceptsCreated() {
		conceptsCreated++;
	}

	public void setOwnConceptsEdits(long ownConceptEdits) {
		this.ownConceptsEdits = ownConceptEdits;
	}

	public void incOwnConceptEdits() {
		this.ownConceptsEdits++;		
	}
	
	public void setForeignConceptsEdits(long foreignConceptEdits) {
		this.foreignConceptsEdits = foreignConceptEdits;
	}

	public void incForeignConceptEdits() {
		this.foreignConceptsEdits++;		
	}

	public void setConceptDeletes(long long1) {
		this.conceptDeletes = long1;
	}

	public void incConceptDeletes() {
		this.conceptDeletes++;		
	}

	public void setLinkCount(long long1) {
		this.linkCount = long1;
	}

	public void incLinkCount() {
		this.linkCount++;
	}

	public void setLinkEdits(int linkEdits) {
		this.linkEdits = linkEdits;
	}

	public void incLinkEdits() {
		linkEdits++;		
	}

	public void setLinkDeletes(long long1) {
		this.linkDeletes = long1;
	}

	public void incLinkDeletes() {
		linkDeletes++;	
	}

	public String getUser() {
		return user;
	}

	public long getConceptsCreated() {
		return conceptsCreated;
	}

	public long getOwnConceptsEdits() {
		return ownConceptsEdits;
	}

	public long getForeignConceptsEdits() {
		return foreignConceptsEdits;
	}

	public long getConceptDeletes() {
		return conceptDeletes;
	}

	public long getLinkCount() {
		return linkCount;
	}

	public long getLinkEdits() {
		return linkEdits;
	}

	public long getLinkDeletes() {
		return linkDeletes;
	}

	@Override
	public String toString() {
		return "UserSummary [user=" + user + ", conceptsCreated=" + conceptsCreated + ", ownConceptsEdits="
				+ ownConceptsEdits + ", foreignConceptsEdits=" + foreignConceptsEdits + ", conceptDeletes="
				+ conceptDeletes + ", linkCreates=" + linkCount + ", linkEdits=" + linkEdits + ", linkDeletes="
				+ linkDeletes + "]";
	}

	public double getAwarenessScore() {
		return awarenessScore;
	}

	public void setAwarenessScore(double awarenessScore) {
		this.awarenessScore = awarenessScore;
	}

	public void incVotingCount() {
		this.votingCount++;
	}

	public int getVotingCount() {
		return this.votingCount;
	}



}
