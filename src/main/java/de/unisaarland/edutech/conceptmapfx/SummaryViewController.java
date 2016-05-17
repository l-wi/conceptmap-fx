package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.unisaarland.edutech.conceptmapfx.datalogging.UserSummary;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class SummaryViewController {

	@FXML
	private PieChart conceptCreateChart;

	@FXML
	private PieChart conceptEditChart;

	@FXML
	private PieChart conceptDeleteChart;

	@FXML
	private PieChart linkCreateChart;

	@FXML
	private PieChart linkEditChart;

	@FXML
	private PieChart linkDeleteChart;

	@FXML
	private Button btnLeft;

	@FXML
	private Button btnRight;

	@FXML
	private Button btnTop;

	@FXML
	private Button btnBottom;

	@FXML
	private HBox leftPanel;

	@FXML
	private HBox rightPanel;
	@FXML
	private GridPane charts;

	private List<UserSummary> summary;

	private Experiment experiment;

	public void setUserSummary(Experiment e, List<UserSummary> summary) {

		this.experiment = e;

		if (e.USER_COUNT < 3)
			rightPanel.setVisible(false);
		if (e.USER_COUNT < 4)
			leftPanel.setVisible(false);

		// remove the total entry
		this.summary = summary;
		summary.remove(0);

		initCharts();

	}

	private void initCharts() {
		initChart(conceptCreateChart, (u) -> u.getConceptsCreated());
		initChart(conceptEditChart, (u) -> (u.getForeignConceptsEdits() + u.getOwnConceptsEdits()));
		initChart(conceptDeleteChart, (u) -> u.getConceptDeletes());

		initChart(linkCreateChart, (u) -> u.getLinkCount());
		initChart(linkEditChart, (u) -> (u.getLinkEdits()));
		initChart(linkDeleteChart, (u) -> u.getLinkDeletes());
	}

	private void initChart(PieChart chart, Function<UserSummary, Long> ext) {
		ArrayList<Data> data = new ArrayList<>();

		for (UserSummary s : summary) {
			data.add(new Data(s.getUser(), ext.apply(s)));
		}

		chart.setPrefWidth(50);
		chart.setPrefHeight(50);

		chart.setLabelLineLength(10);
		chart.setLegendSide(Side.BOTTOM);

		chart.setData(FXCollections.observableList(data));

	}

	@FXML
	public void onRotateAction() {
		double value = charts.getRotate() + 180 % 360;
		charts.setRotate(value);
	}

	@FXML
	public void onTopEmail() {
		boolean state = sendEmailTo(experiment.getParticipants().get(0));
		indicateSuccess(state, btnTop);
	}

	@FXML
	public void onLeftEmail() {
		boolean state = sendEmailTo(experiment.getParticipants().get(2));
		indicateSuccess(state,btnLeft);

	}

	@FXML
	public void onRightEmail() {
		boolean state = sendEmailTo(experiment.getParticipants().get(3));
		indicateSuccess(state,btnRight);

	}

	@FXML
	public void onBottomEmail() {
		boolean state = sendEmailTo(experiment.getParticipants().get(1));
		indicateSuccess(state,btnBottom);

	}

	private void indicateSuccess(boolean success, Button b) {
		b.setText("");
		if(success)
			b.getStyleClass().add("success");
		else
			b.getStyleClass().add("failure");

		b.setDisable(true);
	}

	private boolean sendEmailTo(User user) {
		ConceptMapEmail e = new ConceptMapEmail(user.getEmail());
		
		return e.sendConceptMap();
	}
}
