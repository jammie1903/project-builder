package com.jamie.releaser;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Release {
    private static Gson gson = new Gson();

    private static <T> T get(String url, GithubAuthentication authentication, Class<T> clazz) {
        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "basic " + authentication.getBasicToken());
            connection.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println(response.toString());
            return gson.fromJson(response.toString(), clazz);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T, S> S post(String url, GithubAuthentication authentication, Class<S> clazz, T body) {

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

    public static void main(String... args) throws Exception {
        System.out.println("args: " + Arrays.toString(args));
        var releaseType = args.length > 0 ? Version.VersionSegment.valueOf(args[0].toUpperCase()) : Version.VersionSegment.MINOR;
        System.out.println("Release type: " + releaseType);
        JsonReader reader = new JsonReader(new FileReader("github-authentication.json"));
        GithubAuthentication authentication = gson.fromJson(reader, GithubAuthentication.class);
        System.out.println("performing release as " + authentication.getUsername());

        var lastRelease = get("https://api.github.com/repos/jammie1903/project-builder/releases/latest", authentication, GithubRelease.class);
        Version lastVersion;
        String loadFrom = null;
        if (lastRelease != null) {
            lastVersion = new Version(lastRelease.tag_name);
            Calendar c = Calendar.getInstance(TimeZone.getDefault());
            DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            c.setTime(f.parse(lastRelease.created_at));
            c.add(Calendar.MINUTE, 1);
            loadFrom = f.format(c.getTime());
            System.out.println("last version release details:" + lastRelease.name + " created at " + lastRelease.created_at);
        } else {
            lastVersion = new Version(0, 0, 0);
            System.out.println("no previous release found");
        }
        lastVersion.increment(releaseType);
        System.out.println("new Version: " + lastVersion);

        GithubCommit[] commits = get("https://api.github.com/repos/jammie1903/project-builder/commits" + (loadFrom != null ? "?since=" + loadFrom : ""), authentication, GithubCommit[].class);
        var releaseDetails = GithubRelease.newRelease(lastVersion.toString(), commits);
        System.out.println("Release message: \n ----------------------------------------\n\n" + releaseDetails.body);

        System.out.println("\n\n---------------------------------------\n\nZipping release folder...");
        File releaseZip = Zip.pack(new File("out/artifacts/project_builder"));
        System.out.println("submitting release...");
        var newRelease = post("https://api.github.com/repos/jammie1903/project-builder/releases", authentication, GithubRelease.class, releaseDetails);
        System.out.println("new release: " + newRelease.html_url);

        var releaseId = newRelease.id;
        System.out.println("uploading release asset...");
        post("https://uploads.github.com/repos/jammie1903/project-builder/releases/" + releaseId + "/assets?name=release.zip", authentication, null, releaseZip.toPath());
        System.out.println("done");
    }
}
