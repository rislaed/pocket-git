package com.aor.pocketgit.data;

public class BlameLine {
    private String mAuthor;
    private String mCommit;
    private String mText;

    public BlameLine(String text, String commit, String author) {
        this.mText = text;
        this.mCommit = commit;
        this.mAuthor = author;
    }

    public String getLineText() {
        return this.mText;
    }

    public String getCommit() {
        return this.mCommit;
    }

    public String getAuthor() {
        return this.mAuthor;
    }

    public void setCommit(String commit) {
        this.mCommit = commit;
    }

    public void setAuthor(String author) {
        this.mAuthor = author;
    }
}
