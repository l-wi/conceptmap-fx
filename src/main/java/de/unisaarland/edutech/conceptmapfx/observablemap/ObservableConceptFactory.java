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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptFactory;
import de.unisaarland.edutech.conceptmapping.User;

public class ObservableConceptFactory implements ConceptFactory, Observable,Serializable{

	private transient List<ConceptMapObserver> observers = new ArrayList<>();

	public void addListener(ConceptMapObserver c) {
		this.observers.add(c);
	}

	@Override
	public Concept create(User u) {
		ObservableCollaborativeString name = new ObservableCollaborativeString(u);
		ObservableConcept c = new ObservableConcept(name);
		observers.forEach((l) -> {
			c.addListener(l);
			name.addListener(l);
		});
		return c;
	}

	@Override
	public Concept create(User u, String s) {
		ObservableCollaborativeString name = new ObservableCollaborativeString(u, s);
		ObservableConcept c = new ObservableConcept(name);
		observers.forEach((l) -> {
			c.addListener(l);
			name.addListener(l);
		});
		return c;

	}

}
