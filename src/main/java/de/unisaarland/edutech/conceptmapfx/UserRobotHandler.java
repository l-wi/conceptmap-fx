package de.unisaarland.edutech.conceptmapfx;

import org.comtel2000.keyboard.robot.IRobot;

import de.unisaarland.edutech.conceptmapping.User;

public class UserRobotHandler implements IRobot {

	private ConceptViewController cv;
	private User u;

	public UserRobotHandler(ConceptViewController cv, User u) {
		this.cv = cv;
		this.u = u;
		cv.adjustCaret();
	}

	@Override
	public void sendToComponent(Object source, char ch, boolean ctrl) {
		cv.requestTextFieldFocus();

		if (ch == java.awt.event.KeyEvent.VK_BACK_SPACE) {
			cv.remove(cv.getCaretPosition() - 1);
		} else {
			int index = cv.getCaretPosition();
			cv.insert(u, index, ch);
		}

	}

}
