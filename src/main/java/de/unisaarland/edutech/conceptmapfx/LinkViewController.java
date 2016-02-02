package de.unisaarland.edutech.conceptmapfx;

import java.util.ArrayList;
import java.util.List;

import de.unisaarland.edutech.conceptmapfx.event.ConceptMovedListener;
import de.unisaarland.edutech.conceptmapfx.event.InputClosedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkDirectionUpdatedListener;
import de.unisaarland.edutech.conceptmapfx.event.LinkEditRequestedListener;
import de.unisaarland.edutech.conceptmapping.User;

public class LinkViewController implements ConceptMovedListener, LinkDirectionUpdatedListener, InputClosedListener {

	private List<LinkDirectionUpdatedListener> linkDirectionListener = new ArrayList<LinkDirectionUpdatedListener>();
	private List<LinkEditRequestedListener> linkEditListners = new ArrayList<LinkEditRequestedListener>();

	public void addLinkEditRequestedListener(LinkEditRequestedListener l) {
		linkEditListners.add(l);
	}

	public void addLinkDirectionUpdatedListener(LinkDirectionUpdatedListener l) {
		linkDirectionListener.add(l);
	}

	public void conceptMoved(ConceptViewController cv, User u) {
		// TODO Auto-generated method stub

	}

	public void linkDirectionUpdated(LinkViewController lv, Direction d, User u) {
		// TODO Auto-generated method stub

	}

	public void inputClosed(User u) {
		// TODO Auto-generated method stub

	}
}
