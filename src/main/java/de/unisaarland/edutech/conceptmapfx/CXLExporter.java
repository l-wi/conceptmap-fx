package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.unisaarland.edutech.conceptmapping.Concept;
import de.unisaarland.edutech.conceptmapping.ConceptMap;
import de.unisaarland.edutech.conceptmapping.Link;

public class CXLExporter {

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
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	//Falsch vom link zum concept! Neu
	private void writeConnections() throws XMLStreamException {
		out.writeStartElement("connection-list");
		for (int i = 0; i < cmap.getConceptCount(); i++) {
			for (int j = 0; j < cmap.getConceptCount(); j++) {
				if (cmap.isAnyLinkExisting(cmap.getConcept(i), cmap.getConcept(j))) {

					boolean directed = cmap.isLinkedDirectedStartToEnd(i, j);

					out.writeStartElement("connection");
					out.writeAttribute("id", "con" + String.valueOf(i) + String.valueOf(j));
					out.writeAttribute("from-id", "c" + i);
					out.writeAttribute("to-id", "c" + j);
					out.writeAttribute("isBidirectional", String.valueOf(directed));
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
}
