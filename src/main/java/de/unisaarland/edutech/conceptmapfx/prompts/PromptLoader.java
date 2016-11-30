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
