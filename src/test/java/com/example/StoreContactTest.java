package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StoreContactTest {

    @InjectMocks
    private StoreContact servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    public void testDoGetMissingParameters() throws ServletException, IOException {
        when(request.getParameter("firstName")).thenReturn(null);
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("email")).thenReturn("john.doe@example.com");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testDoGetSuccessfulAddition() throws IOException, ServletException {
        Path tempFile = Files.createTempFile(null, ".csv");
        StoreContact.setCsvFile(tempFile); // Assuming you add a setter for testing

        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("email")).thenReturn("john.doe@example.com");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assert Files.readAllLines(tempFile, StandardCharsets.UTF_8).contains("John,Doe,john.doe@example.com");
        Files.delete(tempFile); // Clean up the file
    }

    @Test
    public void testDoGetDuplicateEntry() throws IOException, ServletException {
        Path tempFile = Files.createTempFile(null, ".csv");
        Files.write(tempFile, "John,Doe,john.doe@example.com\n".getBytes(StandardCharsets.UTF_8));
        StoreContact.setCsvFile(tempFile); // Assuming you add a setter for testing

        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("email")).thenReturn("john.doe@example.com");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
        Files.delete(tempFile); // Clean up the file
    }
}
