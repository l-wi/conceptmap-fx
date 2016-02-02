package de.unisaarland.edutech.conceptmapfx;

import org.comtel2000.keyboard.robot.IRobot;

import de.unisaarland.edutech.conceptmapping.User;

public class UserRobotHandler implements IRobot {

	private ConceptViewController cv;
	private User u;

	public UserRobotHandler(ConceptViewController cv, User u) {
		this.cv = cv;
		this.u = u;
	}

	@Override
	public void sendToComponent(Object source, char ch, boolean ctrl) {
		//TODO just a dummy implementation
		cv.append(ch,u);
		
	}

}
