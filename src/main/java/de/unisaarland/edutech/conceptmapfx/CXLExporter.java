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
package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapping.CollaborativeString;
import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Experiment;
import de.unisaarland.edutech.conceptmapping.FocusQuestion;
import de.unisaarland.edutech.conceptmapping.Link;
import de.unisaarland.edutech.conceptmapping.User;

public class CXLExporter {

	private static final String ERROR_MESSAGE = "Exporting concept map to CXL failed:";

	private static final Logger LOG = LoggerFactory.getLogger(CXLExporter.class);

	private ConceptMap cmap;
	private XMLStreamWriter out;

	public CXLExporter() {

	}

	public void export(File f, ConceptMap cmap) {
		this.cmap = cmap;
		try (OutputStream outputStream = new FileOutputStream(f)) {

			out = XMLOutputFactory.newInstance().createXMLStreamWriter(new OutputStreamWriter(outputStream, "utf-8"));

			out.writeStartElement("cmap");

			out.writeStartElement("map");

			writeConceptList();
			writeLinkList();
			writeConnections();

			out.writeEndElement();

			out.writeEndElement();

			out.writeEndDocument();

			out.flush();
		} catch (Exception e) {
			LOG.error(ERROR_MESSAGE, e);
		}
	}

	private void writeConnections() throws XMLStreamException {
		out.writeStartElement("connection-list");
		for (int i = 0; i < cmap.getConceptCount(); i++) {
			for (int j = 0; j < cmap.getConceptCount(); j++) {
				if (cmap.isAnyLinkExisting(cmap.getConcept(i), cmap.getConcept(j))) {

					boolean directed = cmap.isLinkedDirectedStartToEnd(i, j);

					out.writeStartElement("connection");
					out.writeAttribute("id", "con" + String.valueOf(i) + String.valueOf(j) + "f");
					out.writeAttribute("from-id", "c" + i);
					out.writeAttribute("to-id", "l" + String.valueOf(i) + String.valueOf(j));
					if (!directed)
						out.writeAttribute("isBidirectional", "true");
					out.writeEndElement();

					out.writeStartElement("connection");
					out.writeAttribute("id", "con" + String.valueOf(i) + String.valueOf(j) + "t");
					out.writeAttribute("from-id", "l" + String.valueOf(i) + String.valueOf(j));
					out.writeAttribute("to-id", "c" + j);
					if (!directed)
						out.writeAttribute("isBidirectional", "true");

					out.writeEndElement();

				}
			}
		}
		out.writeEndElement();
	}

	private void writeLinkList() throws XMLStreamException {
		out.writeStartElement("linking-phrase-list");
		for (int i = 0; i < cmap.getConceptCount(); i++) {
			for (int j = 0; j < cmap.getConceptCount(); j++) {
				Link l = cmap.getLink(i, j);

				if (l != null) {
					out.writeStartElement("linking-phrase");
					out.writeAttribute("id", "l" + String.valueOf(i) + String.valueOf(j));
					out.writeAttribute("label", l.getCaption().getContent());
					out.writeEndElement();
				}
			}
		}
		out.writeEndElement();
	}

	private void writeConceptList() throws XMLStreamException {
		out.writeStartElement("concept-list");
		for (int i = 0; i < cmap.getConceptCount(); i++) {
			Concept c = cmap.getConcept(i);
			String id = "c" + String.valueOf(i);
			String label = c.getName().getContent();

			out.writeStartElement("concept");
			out.writeAttribute("id", id);
			out.writeAttribute("label", label);
			out.writeEndElement();
		}
		out.writeEndElement();

	}

	public static void main(String[] args) {
		User u = new User("Tim", "Tim@tim.de");
		ConceptMap map = new ConceptMap(new Experiment(u, new FocusQuestion("Test", u), 3, false, false));

		Concept dog = new Concept(new CollaborativeString(u, "Hund"));
		map.addConcept(dog);

		Concept cat = new Concept(new CollaborativeString(u, "Katze"));
		map.addConcept(cat);

		Concept mouse = new Concept(new CollaborativeString(u, "Maus"));
		map.addConcept(mouse);

		map.addDirectedLink(dog, cat).getCaption().insert(u, 0, "jagt");

		map.addDirectedLink(cat, mouse).getCaption().insert(u, 0, "eats");

		CXLExporter exporter = new CXLExporter();
		exporter.export(new File("test.cxl"), map);

	}
}
