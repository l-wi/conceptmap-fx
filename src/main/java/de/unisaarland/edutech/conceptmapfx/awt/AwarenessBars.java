package de.unisaarland.edutech.conceptmapfx.awt;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class AwarenessBars extends AnchorPane {

	private static final int BORDER_SIZE = 9;
	private static final int ZPD_STROKE_WIDTH = 3;
	private ProgressBar[] bars;
	private String[] styles;

	private int barWidth;
	private int barHeight;

	private double zpdLower;
	private double zpdUpper;

	private int spacing = 10;
	private int count;

	public AwarenessBars(int count, int barHeight, int barWidth, double zpdLower, double zpdUpper) {
		throwIfInvalidZPD(zpdLower, zpdUpper);
		this.count = count;
		this.barHeight = barHeight;
		this.barWidth = barWidth;
		this.zpdLower = zpdLower;
		this.zpdUpper = zpdUpper;

		this.setMinHeight(barHeight);

		initBars(count);
		initZPD(zpdLower, zpdUpper);
	}

	private void throwIfInvalidZPD(double zpdLower, double zpdUpper) {
		throwForRange(zpdLower);
		throwForRange(zpdUpper);

		if (zpdLower > zpdUpper)
			throw new RuntimeException("lower zpd bound was larger than upper bound");

	}

	private void throwForRange(double zpdLower) {
		if (zpdLower < 0 || zpdLower >= 1)
			throw new RuntimeException("zpd bound needs to be between 0..1 but was:" + zpdLower);
	}

	private void initZPD(double zpdLower, double zpdUpper) {

		double zpdLowerY = barHeight - (zpdLower * barHeight + 4);

		initZPDLine(zpdLowerY);

		double zpdUpperY = barHeight - (zpdUpper * barHeight);

		initZPDLine(zpdUpperY);

	}

	private void initZPDLine(double y) {

		double lineWidth = barWidth - 2 * BORDER_SIZE;

		for (int i = 0; i < count; i++) {
			Path p = new Path();

			double barStart = (i + 1) * spacing + i * barWidth + BORDER_SIZE;
			double barEnd = barStart + lineWidth;

			MoveTo m = new MoveTo(barStart, y);

			LineTo l = new LineTo(barEnd, y);

			p.setStrokeWidth(ZPD_STROKE_WIDTH);
			p.getElements().addAll(m, l);

			this.getChildren().add(p);
		}
	}

	private void initBars(int count) {
		bars = new ProgressBar[4];
		styles = new String[4];
		for (int i = 0; i < count; i++) {
			ProgressBar bar = initBar(i);
			bars[i] = bar;
			this.getChildren().add(bars[i]);
		}
	}

	private ProgressBar initBar(int i) {
		ProgressBar bar = new ProgressBar(0);

		initStyle(i, bar);

		bar.setPrefWidth(barHeight);
		bar.minWidth(USE_PREF_SIZE);
		bar.maxWidth(USE_PREF_SIZE);
		bar.setPrefHeight(barWidth);
		bar.minHeight(USE_PREF_SIZE);
		bar.maxHeight(USE_PREF_SIZE);

		bar.setRotate(-90);
		bar.setTranslateY(barHeight / 2.0 - barWidth / 2);
		bar.setTranslateX(-barHeight / 2.0 + barWidth / 2);
		bar.setLayoutX(spacing + i * (barWidth + spacing));

		bar.progressProperty().addListener((c, o, n) -> {
			if (n.doubleValue() >= zpdLower && n.doubleValue() <= zpdUpper) {
				bar.setStyle("-fx-accent: green;");
			} else {
				bar.setStyle("");
			}
		});

		return bar;
	}

	private void initStyle(int i, ProgressBar bar) {
		bar.getStylesheets().add("/css/awt.css");

		if (i == 0)
			bar.getStyleClass().add("userTop");
		else if (i == 1)
			bar.getStyleClass().add("userBottom");
		else if (i == 2)
			bar.getStyleClass().add("userLeft");
		else if (i == 3)
			bar.getStyleClass().add("userRight");
	}

	public void setValue(int i, double val) {
		throwIfBarNotExisting(i);
		bars[i].setProgress(val);

	}

	public DoubleProperty valueProperty(int i) {
		throwIfBarNotExisting(i);
		return bars[i].progressProperty();
	}

	private void throwIfBarNotExisting(int i) {
		if (i >= bars.length)
			throw new RuntimeException("there is no Bar " + i);
	}

	public void setOwnersBar(int i) {
	}
}
