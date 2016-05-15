package de.unisaarland.edutech.conceptmapfx.prompts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.unisaarland.edutech.conceptmapping.User;

public class PromptLoader {

	private List<String> prompts = new ArrayList<>();
	private Map<User,String> userPrompts = new HashMap<>();

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
	
	public void setPromptForUser(User u, String s){
		this.userPrompts.put(u,s);
	}

	public String getPromptForUser(User u){
		return this.userPrompts.get(u);
	}
	public List<String> getAvailablePrompts() {
		return Collections.unmodifiableList(prompts);
	}

}
