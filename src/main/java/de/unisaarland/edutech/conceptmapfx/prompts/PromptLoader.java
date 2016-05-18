package de.unisaarland.edutech.conceptmapfx.prompts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.CXLExporter;

public class PromptLoader {

	private static final Logger LOG = LoggerFactory.getLogger(CXLExporter.class);

	private List<String> prompts = new ArrayList<>();

	public PromptLoader() {
		try {
			Properties properties = new Properties();

			properties.load(new FileInputStream(new File("prompts.properties")));

			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Collection<String> c = (Collection) properties.values();
			
			prompts.addAll(c);

		}  catch (IOException e) {
			LOG.error("failed to initialize prompts" , e);
		}

	}


	public List<String> getAvailablePrompts() {
		return Collections.unmodifiableList(prompts);
	}

}
