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
package de.unisaarland.edutech.conceptmapfx.observablemap;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.User;

public class ObservableCollaborativeString extends CollaborativeString implements Cloneable, Observable, Serializable {

	private transient List<ConceptMapObserver> observers = new ArrayList<>();

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
		observers.forEach((l) -> l.beforeChange());
		super.insert(u, index, content);
		observers.forEach((l) -> l.afterChange());
		return this;
	}

	@Override
	public CollaborativeString remove(int startIndex, int count) {
		observers.forEach((l) -> l.beforeChange());
		super.remove(startIndex, count);
		observers.forEach((l) -> l.afterChange());
		return this;
	}

	@Override
	public ObservableCollaborativeString clone() {
		ObservableCollaborativeString s;

		s = (ObservableCollaborativeString) super.clone();

		s.observers = new ArrayList<>();
		if (observers != null)
			s.observers.addAll(this.observers);
		return s;

	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.observers = new ArrayList<>();
	}

}
