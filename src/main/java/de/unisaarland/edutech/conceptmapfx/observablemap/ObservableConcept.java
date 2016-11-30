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
import de.unisaarland.edutech.conceptmapping.Concept;

public class ObservableConcept extends Concept implements Cloneable, Observable, Serializable {

	private transient List<ConceptMapObserver> observers = new ArrayList<>();

	public ObservableConcept(CollaborativeString name) {
		super(name);
	}

	public void addListener(ConceptMapObserver c) {
		this.observers.add(c);
	}

	@Override
	public void setPosition(double x, double y, double r) {
		observers.forEach((l) -> l.beforeChange());
		super.setPosition(x, y, r);
		observers.forEach((l) -> l.afterChange());
	}

	@Override
	public ObservableConcept clone() {
		ObservableConcept s = (ObservableConcept) super.clone();
		s.observers = new ArrayList<>();
		s.observers.addAll(this.observers);
		return s;
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.observers = new ArrayList<>();
	}
}
