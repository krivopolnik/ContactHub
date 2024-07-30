package com.example;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreContact extends HttpServlet {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final Charset CHARSET_WINDOWS_1250 = Charset.forName("windows-1250");
	private static Path csvFile = Paths.get(System.getProperty("java.io.tmpdir"), "contacts.csv");
	private static final Logger logger = LoggerFactory.getLogger(StoreContact.class);

	private synchronized boolean addUniqueContact(Path filePath, String firstName, String lastName, String email) throws IOException {
		final List<String> existingLines = Files.exists(filePath) ? Files.readAllLines(filePath, CHARSET_WINDOWS_1250) : Collections.emptyList();
		final String newContact = String.format("%s,%s,%s", firstName, lastName, email);

		if (!existingLines.contains(newContact)) {
			try (BufferedWriter writer = Files.newBufferedWriter(filePath, CHARSET_WINDOWS_1250, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
				writer.write(newContact);
				writer.newLine();
				return true;
			}
		}
		return false;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String firstName = request.getParameter("firstName");
		final String lastName = request.getParameter("lastName");
		final String email = request.getParameter("email");

		if (isNullOrEmpty(firstName) || isNullOrEmpty(lastName) || isNullOrEmpty(email)) {
			logger.error("Required parameters are missing or empty - firstName, lastName, or email");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (!isValidEmail(email)) {
			logger.error("Invalid email format: {}", email);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (addUniqueContact(csvFile, firstName, lastName, email)) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
	}

	private boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	private boolean isValidEmail(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		Pattern pattern = Pattern.compile(emailRegex);
		return pattern.matcher(email).matches();
	}

	public static void setCsvFile(Path newPath) {
		csvFile = newPath;
	}
}
