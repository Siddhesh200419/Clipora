package com.clipora.servlet;

import com.clipora.dao.ClipboardDAO;
import com.clipora.model.Clipboard;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/clipboards")
public class ClipboardServlet extends HttpServlet {
    private final ClipboardDAO dao = new ClipboardDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        // The frontend sends the clipboard content as plain text.
        if (request.getContentType() == null ||
                !request.getContentType().split(";")[0].trim().equalsIgnoreCase("text/plain")) {
            sendJson(response, 415, Map.of("error", "Send clipboard content as text/plain."));
            return;
        }
        StringBuilder content = new StringBuilder();
        char[] buffer = new char[1024];
        int count;
        while ((count = request.getReader().read(buffer)) != -1) {
            content.append(buffer, 0, count);
        }
        if (content.toString().isBlank()) {
            sendJson(response, 400, Map.of("error", "Please enter some text."));
            return;
        }
        try {
            sendClipboard(response, 201, dao.createClipboard(content.toString()));
        } catch (SQLException e) {
            getServletContext().log("Could not save clipboard.", e);
            sendJson(response, 503, Map.of("error", "Could not save text. Please try again later."));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        if (code == null || !code.matches("[0-9]{6}")) {
            sendJson(response, 400, Map.of("error", "Enter a six-digit code."));
            return;
        }
        try {
            Clipboard clipboard = dao.getClipboardByCode(code);
            if (clipboard == null) {
                sendJson(response, 404, Map.of("error", "Code not found or expired."));
                return;
            }
            sendClipboard(response, 200, clipboard);
        } catch (SQLException e) {
            getServletContext().log("Could not retrieve clipboard.", e);
            sendJson(response, 503, Map.of("error", "Could not retrieve text. Please try again later."));
        }
    }

    private void sendClipboard(HttpServletResponse response, int status, Clipboard clipboard) throws IOException {
        // Convert the date to a string so Gson does not need a custom date adapter.
        sendJson(response, status, Map.of(
                "code", clipboard.getCode(),
                "content", clipboard.getContent(),
                "expiresAt", clipboard.getExpiresAt().toString()));
    }

    private void sendJson(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.getWriter().write(gson.toJson(data));
    }
}
