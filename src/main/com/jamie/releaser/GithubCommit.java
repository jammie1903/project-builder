package com.jamie.releaser;

public class GithubCommit {
    public String html_url;
    public CommitBody commit;

    public static class CommitBody {
        public String message;
    }

    public String toMarkdown() {
        return "[" + commit.message + "](" + html_url + ")";
    }
}
