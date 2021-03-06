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
package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import de.unisaarland.edutech.conceptmapfx.datalogging.UserSummary;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.User;
import javafx.application.Platform;
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

		Optional<UserSummary> findFirst = summary.stream().filter((c) -> c.getUser().equals("total")).findFirst();
		summary.remove(findFirst.get());

		List<User> participants = e.getParticipants();

		// sort to prevent that the first user doing interaction get assigned
		// the color of the first user of participants
		summary.sort((UserSummary c1, UserSummary c2) -> {
			return participants.indexOf(c1.getUser()) - participants.indexOf(c2.getUser());
		});

		initCharts();

		ConceptMapEmail email = new ConceptMapEmail(e.getReseacher().getEmail());
		email.sendData();

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

		chart.setLabelsVisible(false);
		// chart.setLabelLineLength(10);
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
		runEmailAsync(btnTop, 0);
	}

	private void runEmailAsync(Button btn, int i) {
		btn.setDisable(true);

		CompletableFuture<Boolean> task = CompletableFuture
				.supplyAsync(() -> sendEmailTo(experiment.getParticipants().get(i)));

		task.thenAccept((b) -> Platform.runLater(() -> indicateSuccess(b, btn)));
	}

	@FXML
	public void onLeftEmail() {
		runEmailAsync(btnLeft, 2);

	}

	@FXML
	public void onRightEmail() {
		runEmailAsync(btnRight, 3);

	}

	@FXML
	public void onBottomEmail() {
		runEmailAsync(btnBottom, 1);

	}

	private void indicateSuccess(boolean success, Button b) {
		b.setText("");
		if (success)
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
