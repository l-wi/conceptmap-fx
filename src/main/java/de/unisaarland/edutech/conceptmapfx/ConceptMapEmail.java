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
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisaarland.edutech.conceptmapfx.datalogging.CSVExporter;

public class ConceptMapEmail {

	private static final String ERROR_MSG = "Failed to send email";

	private static final Logger LOG = LoggerFactory.getLogger(ConceptMapEmail.class);

	private String to;

	private String from;
	private String subject;
	private String userText;

	private Properties properties;

	private String examinerText;

	private String examinerSubject;

	private boolean isSendingDataEmail;

	public ConceptMapEmail(String address) {
		this.to = address;

		try {
			Properties p = new Properties();

			p.load(new FileReader("mail.properties"));

			this.properties = p;

			from = p.getProperty("from");
			subject = p.getProperty("subject");
			userText = p.getProperty("userMessage");
			examinerText = p.getProperty("examinerMessage");
			examinerSubject = p.getProperty("examinerSubject");
			isSendingDataEmail = Boolean.parseBoolean(p.getProperty("sendDataViaEmail"));

		} catch (IOException e) {
			LOG.error(ERROR_MSG, e);
		}

	}

	public boolean sendData() {
		try {
			if (!isSendingDataEmail)
				return false;

			MimeMessage message = initEmail();

			Multipart multipart = initMessageText(message, examinerSubject, examinerText);

			addFileAttachment(multipart, getSummaryDataFile(), "summary.csv");
			addFileAttachment(multipart, getProcessDataFile(), "process.csv");
			addFileAttachment(multipart, getCoreDataFile(), "core.csv");
			addFileAttachment(multipart, getScreenshot(), "conceptMap.png");

			sendEmail(message, multipart);

			return true;
		} catch (MessagingException e) {
			LOG.error(ERROR_MSG, e);
			return false;
		}

	}

	private File getCoreDataFile() {
		Path path = Paths.get(SessionSaver.getWorkingDir().getAbsolutePath(), CSVExporter.DATA_DIR_NAME,
				CSVExporter.CORE_FILE_NAME);
		return path.toFile();
	}

	private File getProcessDataFile() {
		Path path = Paths.get(SessionSaver.getWorkingDir().getAbsolutePath(), CSVExporter.DATA_DIR_NAME,
				CSVExporter.PROCESS_FILE_NAME);
		return path.toFile();
	}

	private File getSummaryDataFile() {
		Path path = Paths.get(SessionSaver.getWorkingDir().getAbsolutePath(), CSVExporter.DATA_DIR_NAME,
				CSVExporter.SUMMARY_FILE_NAME);
		return path.toFile();
	}

	public boolean sendConceptMap() {
		try {
			MimeMessage message = initEmail();

			Multipart multipart = initMessageText(message, this.subject, this.userText);

			addFileAttachment(multipart, getConceptMapFileName(), "conceptMap.cxl");
			addFileAttachment(multipart, getScreenshot(), "conceptMap.png");
			sendEmail(message, multipart);
			return true;

		} catch (MessagingException e) {
			LOG.error(ERROR_MSG, e);
			return false;
		}

	}

	private File getScreenshot() {
		return new File(SessionSaver.getWorkingDir(), Main.FINAL_MAP_SCREENSHOT);
	}

	private void sendEmail(MimeMessage message, Multipart multipart) throws MessagingException {
		message.setContent(multipart);
		Transport.send(message);
	}

	private void addFileAttachment(Multipart multipart, File f, String name) throws MessagingException {
		if (f != null)
			addAttachment(multipart, f.getAbsolutePath(), name);
	}

	private MimeMessage initEmail() throws MessagingException, AddressException {
		String username = properties.getProperty("username");
		String password = properties.getProperty("password");

		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		MimeMessage message = new MimeMessage(session);

		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		message.setFrom(new InternetAddress(from));
		return message;
	}

	private Multipart initMessageText(MimeMessage message, String subject, String text) throws MessagingException {
		message.setSubject(subject);

		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(text);

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		return multipart;
	}

	private File getConceptMapFileName() {
		File[] list = SessionSaver.getCXLDir().listFiles();

		return (list.length == 0) ? null : list[list.length - 1];
	}

	private static void addAttachment(Multipart multipart, String path, String title) throws MessagingException {
		DataSource source = new FileDataSource(path);
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(title);

		multipart.addBodyPart(messageBodyPart);
	}
}
