package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapfx.LinkViewController;
import de.unisaarland.edutech.conceptmapping.User;

public interface LinkDirectionUpdatedListener {


	void linkDirectionUpdated(LinkViewController lv,Direction d, User u);
	
	enum Direction{
		START_TO_END,END_TO_START, NOT_DIRECTED;
		
	}
	
}
