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
package de.unisaarland.edutech.conceptmapfx.link;

import de.unisaarland.edutech.conceptmapfx.concept.ConceptViewController;
import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapView;
import de.unisaarland.edutech.conceptmapfx.event.LinkDeletedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkEditRequestedListener;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Link;

public class LinkViewBuilder {

	private ConceptMap map;

	private ConceptMapView pane;

	private LinkViewController controller;

	private ConceptViewController cv1;

	private ConceptViewController cv2;

	public LinkViewBuilder(ConceptMap m, ConceptMapView conceptMapPane, ConceptViewController cv1, ConceptViewController cv2) {
		this.map = m;
		this.pane = conceptMapPane;
		this.cv1 = cv1;
		this.cv2 = cv2;
		controller = new LinkViewController(this.map.getExperiment().getParticipants(), pane, cv1, cv2);
	}

	public LinkViewBuilder forNewLink() {
		Link link = map.addUndirectedLink(cv1.getConcept(), cv2.getConcept());
		controller.setLink(link);
		cv1.addConceptMovingListener(controller);
		cv2.addConceptMovingListener(controller);
		return this;
	}

	public LinkViewBuilder withLink(Link link) {
		controller.setLink(link);
		cv1.addConceptMovingListener(controller);
		cv2.addConceptMovingListener(controller);
		return this;
	}

	public LinkViewBuilder withDeletedListener(LinkDeletedListener l){
		controller.addLinkDeletionListener(l);
		return this;
	}
	
	public LinkViewBuilder withEditListener(LinkEditRequestedListener l) {
		controller.addLinkEditRequestedListener(l);
		return this;
	}

	public LinkViewBuilder withDirectionListener(LinkDirectionUpdatedListener l) {
		controller.addLinkDirectionUpdatedListener(l);
		return this;
	}

	public LinkViewController buildUndirectedAndAdd() {
		controller.initialize();
		return controller;

	}

	public LinkViewController buildWithDirectionAndAdd(LinkDirectionUpdatedListener.Direction d) {
		controller.initialize();
		controller.setDirected(d);
		return controller;
	}

	public void newLink(ConceptViewController cv1, ConceptViewController cv2) {

	}
}
