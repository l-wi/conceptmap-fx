package de.unisaarland.edutech.conceptmapfx;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

public class AnchorView extends Pane {

	private Circle circle;
	private Polygon arrowPolygon;

	private Shape currentlyActive;
	private LinkViewController controller;

	private EventHandler<? super MouseEvent> onClickToggle = (l) -> {
		controller.anchorAltered(this);
	};

	AnchorView(LinkViewController controller, Color color, double width, double height) {
		this.controller = controller;
		createCircle(color, width, height);
		createArrow(width, height);

		this.getChildren().add(circle);
		circle.setOnMouseClicked(onClickToggle);
		arrowPolygon.setOnMouseClicked(onClickToggle);
		currentlyActive = circle;
		// this.setStyle("-fx-background-color: #FF0000;");

	}

	private void createCircle(Color color, double width, double height) {
		circle = new Circle(width / 2, height / 2, width / 2);
		circle.setFill(color.deriveColor(1, 1, 1, 0.5));
		circle.setStroke(color);
		circle.setStrokeWidth(2);
		circle.setStrokeType(StrokeType.OUTSIDE);
	}

	private void createArrow(double width, double height) {
		double[] arrowShape = new double[] { width / 2, height / 2, width, height, width, 0 };
		arrowPolygon = new Polygon(arrowShape);
	}

	public void toggle() {
		if (currentlyActive instanceof Circle)
			toArrow();
		else
			toCircle();
	}

	public void toArrow() {
		if (currentlyActive instanceof Polygon)
			return;
		this.getChildren().remove(circle);
		this.getChildren().add(arrowPolygon);
		this.currentlyActive = arrowPolygon;
		currentlyActive.setOpacity(100);
	}

	public void toCircle() {
		if (currentlyActive instanceof Circle)
			return;
		this.getChildren().add(circle);
		this.getChildren().remove(arrowPolygon);
		this.currentlyActive = circle;
		currentlyActive.setOpacity(100);
	}

	public boolean isDirected() {
		return currentlyActive instanceof Polygon;
	}
}