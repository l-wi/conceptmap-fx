package de.unisaarland.edutech.conceptmapfx.input;

import org.comtel2000.keyboard.robot.IRobot;

import de.unisaarland.edutech.conceptmapfx.fourusertoucheditable.CollaborativeStringTextFieldBinding;
import de.unisaarland.edutech.conceptmapping.User;

public class UserRobotHandler implements IRobot {

	private CollaborativeStringTextFieldBinding editable;
	private User u;

	public UserRobotHandler(CollaborativeStringTextFieldBinding editable, User u) {
		this.editable = editable;
		this.u = u;
	}

	@Override
	public void sendToComponent(Object source, char ch, boolean ctrl) {

		if (ch == java.awt.event.KeyEvent.VK_BACK_SPACE) {
			editable.removeLast(u);
		} else {
			editable.append(u,ch);
		}

	}

}
