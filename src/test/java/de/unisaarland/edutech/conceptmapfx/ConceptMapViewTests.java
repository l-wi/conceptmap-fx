package de.unisaarland.edutech.conceptmapfx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapView;
import de.unisaarland.edutech.conceptmapfx.event.AnchorAlteredListener;
import de.unisaarland.edutech.conceptmapfx.link.AnchorView;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

public class ConceptMapViewTests extends ApplicationTest {

	private Stage stage;
	private ConceptMapView view;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		this.view = new ConceptMapView();

	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testAdd() throws IOException {
		// given
		Node c1 = loadConcept();
		Node c2 = loadConcept();

		AnchorAlteredListener l = (e) -> System.out.println("altered");

		AnchorView anchor1 = new AnchorView(l, Color.RED, 10, 10);
		AnchorView anchor2 = new AnchorView(l, Color.RED, 10, 10);

		Path linkLine = new Path(new MoveTo(10, 10), new LineTo(20, 20));

		Node linkCaption = loadLinkCaption();
		Node bg = new AnchorPane();

		Node[] expected = { bg, c1, c2, linkLine, linkCaption, anchor2, anchor1 };

		// when

		view.add(c1);
		view.add(linkLine);
		view.add(c2);
		view.add(linkCaption);
		view.add(anchor1);
		view.add(bg);
		view.add(anchor2);

		// then

		assertEqualsList(expected);

	}

	private void assertEqualsList(Node[] expected) {
		ObservableList<Node> children = view.getChildren();
		
		for (int i = 0; i < children.size(); i++) {
			assertEquals(children.get(i), expected[i]);
		}
	}

	private Node loadLinkCaption() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/LinkView.fxml"));
		return loader.load();
	}

	private Node loadConcept() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConceptView.fxml"));
		return loader.load();

	}

	@Test
	public void testRemove() throws IOException {
		// given
		Node c1 = loadConcept();
		Node c2 = loadConcept();

		AnchorAlteredListener l = (e) -> System.out.println("altered");

		AnchorView anchor1 = new AnchorView(l, Color.RED, 10, 10);
		AnchorView anchor2 = new AnchorView(l, Color.RED, 10, 10);

		Path linkLine = new Path(new MoveTo(10, 10), new LineTo(20, 20));

		Node linkCaption = loadLinkCaption();
		Node bg = new AnchorPane();

		Node[] expected1 = { bg, c1,  linkCaption, anchor2, anchor1 };
		Node[] expected2 = { bg, c1,  linkCaption, anchor1 };

		view.add(c1);
		view.add(linkLine);
		view.add(c2);
		view.add(linkCaption);
		view.add(anchor1);
		view.add(bg);
		view.add(anchor2);
		
		// when

	
		view.remove(c2);
		view.remove(linkLine);
		
		assertEqualsList(expected1);
	
		view.remove(anchor2);
		
		assertEqualsList(expected2);
		
		for(Node n: expected2)
			view.remove(n);
		
		assertSame(0, view.getChildren().size());
	}

}
