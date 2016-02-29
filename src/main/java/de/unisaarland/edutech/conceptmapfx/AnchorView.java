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

	private boolean isActive = false;

	private EventHandler<? super MouseEvent> onClickToggle = (l) -> {
		if (isActive)
			controller.anchorAltered(this);
	};

	AnchorView(LinkViewController controller, Color color, double width, double height) {
		this.controller = controller;
		createCircle(color, width, height);
		createArrow(color, width, 1.5 * height);

		this.getChildren().add(circle);
		circle.setOnMouseClicked(onClickToggle);
		arrowPolygon.setOnMouseClicked(onClickToggle);
		currentlyActive = circle;
		// this.setStyle("-fx-background-color: #FF0000;");

	}

	private void createCircle(Color color, double width, double height) {
		circle = new Circle(width / 2, height / 2, width / 2);
		circle.setFill(color.deriveColor(0, 0, 0, 0.8));
		circle.setStroke(color);
		circle.setStrokeWidth(4);
		circle.setStrokeType(StrokeType.OUTSIDE);
		circle.setCache(true);
	}

	private void createArrow(Color color, double width, double height) {
		double[] arrowShape = new double[] { 0, height / 2, width, height, width, 0 };
		arrowPolygon = new Polygon(arrowShape);
		arrowPolygon.setFill(color);
		arrowPolygon.setStroke(Color.BLACK);
		arrowPolygon.setCache(true);
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

	public void setActive(boolean isSelected) {
		this.isActive = isSelected;
		if (isActive) {
			this.arrowPolygon.setFill(Color.GREEN);
			this.circle.setStroke(Color.GREEN);
		} else {
			this.arrowPolygon.setFill(Color.WHITE);
			this.circle.setStroke(Color.WHITE);
		}
	}
}