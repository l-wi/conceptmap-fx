package de.unisaarland.edutech.conceptmapfx;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class ConceptMapEmail {

	private String to;

	private String from;
	private String subject;
	private String userText;

	private Properties properties;

	public ConceptMapEmail(String address) {
		this.to = address;

		try {
			Properties p = new Properties();

			p.load(new FileReader("mail.properties"));

			this.properties = p;

			from = p.getProperty("from");
			subject = p.getProperty("subject");
			userText = p.getProperty("userMessage");

		} catch (IOException e) {
			// TODO error handling
			e.printStackTrace();
		}

	}

	public boolean sendConceptMap() {
		String username = properties.getProperty("username");
		String password = properties.getProperty("password");

		Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			initMessageText(message, this.userText);

			// Send message
			Transport.send(message);
			return true;

		} catch (MessagingException mex) {
			// TODO exception handling
			mex.printStackTrace();
			return false;
		}

	}

	private void initMessageText(MimeMessage message, String text) throws MessagingException {
		// Set Subject: header field
		message.setSubject(subject);

		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(text);

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		String conceptMapFileName = getConceptMapFileName();

		if (conceptMapFileName != null)
			addAttachment(multipart, conceptMapFileName);

		message.setContent(multipart);
	}

	private String getConceptMapFileName() {
		File[] list = SessionSaver.getCXLDir().listFiles();

		return (list.length == 0) ? null : list[list.length - 1].getAbsolutePath();
	}

	private static void addAttachment(Multipart multipart, String filename) throws MessagingException {
		DataSource source = new FileDataSource(filename);
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName("conceptMap.cxl");
		
		multipart.addBodyPart(messageBodyPart);
	}
}
