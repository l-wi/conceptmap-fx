package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import de.unisaarland.edutech.conceptmapfx.conceptmap.ConceptMapViewController;
import de.unisaarland.edutech.conceptmapfx.observablemap.ConceptMapObserver;
import de.unisaarland.edutech.conceptmapfx.observablemap.Observable;
import de.unisaarland.edutech.conceptmapfx.observablemap.ObservableConceptMap;
import javafx.scene.control.Button;

public class UndoHistory implements ConceptMapObserver, Observable {

	private ObservableConceptMap conceptMap;
	private Stack<ObservableConceptMap> states = new Stack<ObservableConceptMap>();
	private boolean isRestoringState;
	private ConceptMapViewController controller;
	private List<Button> btnUndos = new ArrayList<>();
	private boolean isSetup;
	private List<ConceptMapObserver> listeners = new ArrayList<ConceptMapObserver>();

	public UndoHistory(ObservableConceptMap conceptMap, ConceptMapViewController controller) {
		this.conceptMap = conceptMap;
		this.controller = controller;
		this.isSetup = true;
	}

	public void activate() {
		this.isSetup = false;
	}

	public void addUndoButton(Button btn) {
		btnUndos.add(btn);
		updateButtonStates();
	}

	private void updateButtonStates() {
		btnUndos.forEach((b) -> {
			b.setDisable(states.isEmpty());
		});
	}

	@Override
	public void beforeChange() {
		if (!isRestoringState && !isSetup) {
			states.push(conceptMap.clone());
			updateButtonStates();
		}
	}

	@Override
	public void afterChange() {

	}

	public void undo() {
		isRestoringState = true;
		if (states.isEmpty())
			return;

		listeners.forEach((l) -> l.beforeChange());

		conceptMap = states.pop();
		controller.setConceptMap(conceptMap);
		controller.layout();

		updateButtonStates();

		listeners.forEach((l) -> l.afterChange());

		isRestoringState = false;
	}

	public boolean isEmpty() {
		return states.isEmpty();
	}

	@Override
	public void addListener(ConceptMapObserver c) {
		this.listeners.add(c);
	}
}
