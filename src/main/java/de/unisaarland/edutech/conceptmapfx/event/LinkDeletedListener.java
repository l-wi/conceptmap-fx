package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.LinkViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface LinkDeletedListener {

	void linkDeleted(LinkViewController lv, User u);
}
