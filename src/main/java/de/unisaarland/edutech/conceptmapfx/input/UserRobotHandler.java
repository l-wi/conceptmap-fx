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
