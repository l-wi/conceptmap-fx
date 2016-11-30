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
package de.unisaarland.edutech.conceptmapfx.conceptmap;

import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.FourUserTouchEditable;
import de.unisaarland.edutech.conceptmapfx.link.AnchorView;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;

public class ConceptMapView extends Pane {

	private int linkCaptionIndex = 0;
	private int linkPathIndex = 0;
	private int conceptIndex = 0;
	private int backgroundIndex = 0;

	public ConceptMapView() {

	}

	public void add(Node added) {
		ObservableList<Node> children = getChildren();
		int index = 0;

		if (added instanceof AnchorView) {
			index = linkCaptionIndex + linkPathIndex + conceptIndex + backgroundIndex;

		} else if (added instanceof FourUserTouchEditable && added.lookup("#linkCaption") != null) {
			index = linkCaptionIndex + linkPathIndex + conceptIndex + backgroundIndex;
			linkCaptionIndex++;
		} else if (added instanceof Path) {
			index = linkPathIndex + conceptIndex + backgroundIndex;
			linkPathIndex++;
		} else if (added instanceof FourUserTouchEditable && added.lookup("#conceptCaption") != null) {
			index = backgroundIndex + conceptIndex;
			conceptIndex++;
		} else
			index = backgroundIndex++;

		children.add(index, added);

		//debugPrint();
	}

	private void debugPrint() {
		for (Node added : getChildren()) {
			if (added instanceof AnchorView)
				System.out.print("AnchorView,");
			else if (added instanceof FourUserTouchEditable && added.lookup("#linkCaption") != null)
				System.out.print("linkCaption,");
			else if (added instanceof Path)
				System.out.print("path,");
			else if (added instanceof FourUserTouchEditable && added.lookup("#conceptCaption") != null)
				System.out.print("conceptCaption,");
			else
				System.out.print("background,");

		}
		System.out.println();
	}

	public void remove(Node n) {
		if (n instanceof AnchorView)
			;
		else if (n instanceof FourUserTouchEditable && n.lookup("#linkCaption") != null)
			linkCaptionIndex--;
		else if (n instanceof Path)
			linkPathIndex--;
		else if (n instanceof FourUserTouchEditable && n.lookup("#conceptCaption") != null)
			conceptIndex--;
		else
			backgroundIndex--;

		getChildren().remove(n);
	}

	
}
