package com.jamie.rest;

import com.google.gson.Gson;
import com.jamie.releaser.GithubAuthentication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public class Rest {
    private static Gson gson = new Gson();

    private Rest() {

    }

    public static <T> T get(String url, GithubAuthentication authentication, Class<T> clazz) {
        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");
            if(authentication != null) {
                connection.setRequestProperty("Authorization", "basic " + authentication.getBasicToken());
            }
            connection.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return gson.fromJson(response.toString(), clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T, S> S post(String url, GithubAuthentication authentication, Class<S> clazz, T body) {

        int code;
        HttpURLConnection connection;
        try {
            URL obj = new URL(url);
            connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", body instanceof Path ?
                    URLConnection.guessContentTypeFromName(((Path) body).getFileName().toString()) : "application/json");
            connection.setRequestProperty("Authorization", "basic " + authentication.getBasicToken());
            if (body instanceof Path) {
                connection.setRequestProperty("Content-Transfer-Encoding", "binary");
            }
            connection.setDoOutput(true);
            try (OutputStream output = connection.getOutputStream()) {

                if (body instanceof Path) {
                    Files.copy((Path) body, output);
                } else {
                    try (OutputStreamWriter wr = new OutputStreamWriter(output)) {
                        wr.write(gson.toJson(body));
                        wr.flush();
                    }
                }
            }
            code = connection.getResponseCode();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            if (clazz == null) {
                return null;
            }
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return gson.fromJson(response.toString(), clazz);
        } catch (Exception e) {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                throw new Exception(response.toString());
            } catch (Exception ex) {
                System.err.println(code + ex.getMessage());
                return null;
            }
        }
    }
}
