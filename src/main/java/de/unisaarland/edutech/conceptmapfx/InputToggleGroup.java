package de.unisaarland.edutech.conceptmapfx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;

public class InputToggleGroup {

	private static final Logger LOG = LoggerFactory.getLogger(InputToggleGroup.class);

	private ToggleButton[] toggleButtons;

	private UserToggleEnabledListener l;

	public InputToggleGroup(UserToggleEnabledListener l, ToggleButton... buttons) {
		toggleButtons = buttons;
		this.l = l;
		for (int i = 0; i < toggleButtons.length; i++) {
			ToggleButton b = toggleButtons[i];
			addToggleListener(b, i);
			addEventFilterToPreventUntoggle(b);
		}
	}

	private void addToggleListener(ToggleButton b, int participant) {
		b.selectedProperty().addListener((c, o, n) -> {
			if (n.booleanValue())
				l.userToggleEnabled(participant);

		});
	}

	private void addEventFilterToPreventUntoggle(ToggleButton b) {
		b.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (b.isSelected()) {
					LOG.info("stopping untoggle event as at least one button has to be toggled!");
					mouseEvent.consume();
				}
			}
		});
	}

}
