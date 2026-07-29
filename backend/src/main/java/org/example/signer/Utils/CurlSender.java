package org.example.signer.Utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PrivateKey;
import java.time.Duration;

public final class CurlSender {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private CurlSender() {
        // Private constructor to prevent instantiation of this utility class.
    }

    public static class CurlResult {
        private final int statusCode;
        private final String body;

        public CurlResult(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }
    }

    public static CurlResult send(String xmlContent, String url, String forwardedIp) throws IOException, InterruptedException {
        return send(xmlContent, url, null, forwardedIp);
    }

    public static CurlResult send(String xmlContent, String url, PrivateKey privateKey, String forwardedIp) throws IOException, InterruptedException {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/xml")
                    .header("X-Forwarded-For", forwardedIp)
                    .POST(HttpRequest.BodyPublishers.ofString(xmlContent))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (privateKey != null && responseBody != null && !responseBody.trim().isEmpty()) {
                try {
                    String decryptedResponse = Decrypter.decrypt(responseBody, privateKey);
                    return new CurlResult(response.statusCode(), decryptedResponse);
                } catch (Exception e) {
                    System.out.println("Could not decrypt response (might not be encrypted):\n" + responseBody);
                }
            }
            return new CurlResult(response.statusCode(), responseBody);

        } catch (ConnectException e) {
            // Provide a more specific and helpful error message for connection issues
            throw new IOException("Failed to connect to the server at " + url + ". Please ensure the server is running and accessible.", e);
        }
    }
}

