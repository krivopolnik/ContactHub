package com.example;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreContact extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Charset CHARSET_WINDOWS_1250 = Charset.forName("windows-1250");
	private static Path csvFile = Paths.get(System.getProperty("java.io.tmpdir"), "contacts.csv");
	private static final Logger logger = LoggerFactory.getLogger(StoreContact.class);

	public static void setCsvFile(Path newPath) {
		csvFile = newPath;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String firstName = request.getParameter("firstName");
		final String lastName = request.getParameter("lastName");
		final String email = request.getParameter("email");

		if (firstName == null || lastName == null || email == null) {
			logger.error("Required parameters are missing - firstName, lastName, or email");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (addUniqueContact(csvFile, firstName, lastName, email)) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
	}

	private boolean addUniqueContact(Path filePath, String firstName, String lastName, String email) throws IOException {
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
}
