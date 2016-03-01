package de.unisaarland.edutech.conceptmapfx;

import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;

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
