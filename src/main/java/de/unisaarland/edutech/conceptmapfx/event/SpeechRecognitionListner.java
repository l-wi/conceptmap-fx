package de.unisaarland.edutech.conceptmapfx.event;

import de.unisaarland.edutech.conceptmapping.User;

public interface SpeechRecognitionListner {

	void speechRecognitionStarted(User u);
	
	void speechRecognitionFinished(User u);
	
}
