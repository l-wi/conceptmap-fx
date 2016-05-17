package de.unisaarland.edutech.conceptmapfx.prompts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class PromptLoader {

	private List<String> prompts = new ArrayList<>();

	public PromptLoader() {
		try {
			Properties properties = new Properties();

			properties.load(new FileInputStream(new File("prompts.properties")));

			Collection<String> c = (Collection) properties.values();
			prompts.addAll(c);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public List<String> getAvailablePrompts() {
		return Collections.unmodifiableList(prompts);
	}

}
