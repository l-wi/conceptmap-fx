package de.unisaarland.edutech.conceptmapfx.datalogging;

public class UserSummary {

	private String user;
	private long conceptsCreated;
	private long ownConceptsEdits;
	private long foreignConceptsEdits;
	private long conceptDeletes;
	private long linkCreates;
	private long linkEdits;
	private long linkDeletes;

	public void setUser(String user) {
		this.user = user;
	}

	public void setConceptsCreated(long long1) {
		this.conceptsCreated = long1;
	}

	public void setOwnConceptsEdits(long ownConceptEdits) {
		this.ownConceptsEdits = ownConceptEdits;
	}

	public void setForeignConceptsEdits(long foreignConceptEdits) {
		this.foreignConceptsEdits = foreignConceptEdits;
	}

	public void setConceptDeletes(long long1) {
		this.conceptDeletes = long1;
	}

	public void setLinkCreates(long long1) {
		this.linkCreates = long1;
	}

	public void setLinkEdits(int linkEdits) {
		this.linkEdits = linkEdits;
	}

	public void setLinkDeletes(long long1) {
		this.linkDeletes = long1;
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

	public long getLinkCreates() {
		return linkCreates;
	}

	public long getOwnLinkEdits() {
		return linkEdits;
	}

	public long getLinkDeletes() {
		return linkDeletes;
	}

	@Override
	public String toString() {
		return "UserSummary [user=" + user + ", conceptsCreated=" + conceptsCreated + ", ownConceptsEdits="
				+ ownConceptsEdits + ", foreignConceptsEdits=" + foreignConceptsEdits + ", conceptDeletes="
				+ conceptDeletes + ", linkCreates=" + linkCreates + ", linkEdits=" + linkEdits + ", linkDeletes="
				+ linkDeletes + "]";
	}

}
