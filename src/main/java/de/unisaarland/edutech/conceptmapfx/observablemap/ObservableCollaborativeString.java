package de.unisaarland.edutech.conceptmapfx.observablemap;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.User;

public class ObservableCollaborativeString extends CollaborativeString implements Cloneable {

	private List<ConceptMapObserver> observers = new ArrayList<>();

	public ObservableCollaborativeString(User owner, String content) {
		super(owner);
		append(owner, content);
	}

	public ObservableCollaborativeString(User owner) {
		super(owner);
	}

	public void addListener(ConceptMapObserver c) {
		this.observers.add(c);
	}



	@Override
	public CollaborativeString insert(User u, int index, String content) {
		CollaborativeString old = this.clone();
		super.insert(u, index, content);
		observers.forEach((l) -> l.changedContent(old, this));
		return this;
	}

	@Override
	public CollaborativeString remove(int startIndex, int count) {
		CollaborativeString old = this.clone();
		super.remove(startIndex, count);
		observers.forEach((l) -> l.changedContent(old, this));
		return this;
	}


	@Override
	public ObservableCollaborativeString clone() {
		ObservableCollaborativeString s;

		s = (ObservableCollaborativeString) super.clone();

		s.observers = new ArrayList<>();
		if(observers != null)
			s.observers.addAll(this.observers);
		return s;

	}

}
