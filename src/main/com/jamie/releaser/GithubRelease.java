package com.jamie.releaser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class GithubRelease {
    public String tag_name;
    public Integer id;
    public String upload_url;
    public String html_url;

    public String target_commitish;
    public String name;
    public String body;
    public String created_at;

    public static GithubRelease newRelease(String version, GithubCommit[] changes) {
        var release = new GithubRelease();
        release.tag_name = version;
        release.name = "Release " + version;

        var changesMessage = changes == null || changes.length == 0 ? "No Changes" :
                Arrays.stream(changes).map(GithubCommit::toMarkdown).collect(Collectors.joining("\n\n"));

        release.body = "Released on " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "\n\n### Changes\n\n" + changesMessage;
        release.target_commitish = "master";
        return release;
    }
}
