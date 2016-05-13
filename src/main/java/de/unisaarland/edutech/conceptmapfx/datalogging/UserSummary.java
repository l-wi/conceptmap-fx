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



}
