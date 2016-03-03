package de.unisaarland.edutech.conceptmapfx.observablemap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.LinkFactory;
import de.unisaarland.edutech.conceptmapping.User;

public class ObservableLinkFactory implements LinkFactory, Observable,Serializable {

	private transient List<ConceptMapObserver> observers  = new ArrayList<>();

	public void addListener(ConceptMapObserver c) {
		this.observers.add(c);
	}

	@Override
	public Link create(User u1, User u2) {
		ObservableCollaborativeString caption = new ObservableCollaborativeString(u1);
		observers.forEach((o) -> caption.addListener(o));
		return new Link(u1, u2, caption);
	}

}
