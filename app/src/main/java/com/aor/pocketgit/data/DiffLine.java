package com.aor.pocketgit.data;

import org.slf4j.Marker;

public class DiffLine {
    private String mText;

    public DiffLine(String text) {
        this.mText = text;
    }

    public String getLineText() {
        return this.mText;
    }

    public boolean isAddition() {
        return this.mText.startsWith(Marker.ANY_NON_NULL_MARKER);
    }

    public boolean isDeletion() {
        return this.mText.startsWith("-");
    }

    public boolean isHeader() {
        return this.mText.startsWith("@@");
    }
}
