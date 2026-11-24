package com.clipora.dao;

import com.clipora.model.Clipboard;
import com.clipora.utils.CodeGenerator;
import com.clipora.utils.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class ClipboardDAO {
    public Clipboard createClipboard(String content) throws SQLException {
        String sql = """
                INSERT INTO clipboards (code, content, expires_at)
                VALUES (?, ?, CURRENT_TIMESTAMP + INTERVAL '15 minutes')
                ON CONFLICT (code) DO NOTHING
                RETURNING *
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Remove expired text whenever someone saves a new clipboard.
            try (PreparedStatement cleanup = connection.prepareStatement(
                    "DELETE FROM clipboards WHERE expires_at <= CURRENT_TIMESTAMP")) {
                cleanup.executeUpdate();
            }

            // A random code might already exist. Try another one if it does.
            for (int attempt = 0; attempt < 10; attempt++) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, CodeGenerator.generateCode());
                    statement.setString(2, content);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            return readClipboard(result);
                        }
                    }
                }
            }
        }
        throw new SQLException("Could not create a unique clipboard code.");
    }

    public Clipboard getClipboardByCode(String code) throws SQLException {
        String sql = """
                SELECT * FROM clipboards
                WHERE code = ? AND expires_at > CURRENT_TIMESTAMP
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return readClipboard(result);
                }
            }
        }
        return null;
    }

    private Clipboard readClipboard(ResultSet result) throws SQLException {
        return new Clipboard(
                result.getLong("id"),
                result.getString("code"),
                result.getString("content"),
                result.getObject("created_at", OffsetDateTime.class),
                result.getObject("expires_at", OffsetDateTime.class));
    }
}
