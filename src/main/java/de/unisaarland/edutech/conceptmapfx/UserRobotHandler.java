package de.unisaarland.edutech.conceptmapfx;

import org.comtel2000.keyboard.robot.IRobot;

import de.unisaarland.edutech.conceptmapping.User;

public class UserRobotHandler implements IRobot {

	private Editable editable;
	private User u;

	public UserRobotHandler(Editable editable, User u) {
		this.editable = editable;
		this.u = u;
	}

	@Override
	public void sendToComponent(Object source, char ch, boolean ctrl) {

		if (ch == java.awt.event.KeyEvent.VK_BACK_SPACE) {
			editable.removeLast();
		} else {
			editable.append(u,ch);
		}

	}

}
