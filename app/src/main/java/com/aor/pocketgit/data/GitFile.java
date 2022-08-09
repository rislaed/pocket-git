package com.aor.pocketgit.data;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class GitFile {
    public static final String ADDED = "added";
    public static final String CHANGED = "changed";
    public static final String COMMITED = "commited";
    public static final String CONFLICTING = "conflicting";
    public static final String IGNORED = "ignored";
    public static final String MISSING = "missing";
    public static final String MODIFIED = "modified";
    public static final String REMOVED = "removed";
    public static final String UNCOMMITED = "uncommited";
    public static final String UNTRACKED = "untracked";
    public static final String UNTRACKED_NOT_REMOVED = "untracked not removed";
    private File mFile;
    private Set<String> mStates;

    public GitFile(File file, Set<String> states) {
        this.mFile = file;
        this.mStates = states;
    }

    public GitFile(File file, String state) {
        this.mFile = file;
        this.mStates = new HashSet<>();
        this.mStates.add(state);
    }

    public CharSequence getName() {
        return this.mFile.getName();
    }

    public boolean isDirectory() {
        return this.mFile.isDirectory();
    }

    public File getFile() {
        return this.mFile;
    }

    public Set<String> getStates() {
        return this.mStates;
    }

    public boolean hasState(String state) {
        return this.mStates.contains(state);
    }

    public boolean isStageable() {
        return isStageable(this.mStates);
    }

    public boolean isUnstageable() {
        return isUnstageable(this.mStates);
    }

    public boolean isRevertable() {
        return isRevertable(this.mStates);
    }

    public boolean isDiffable() {
        return isDiffable(this.mStates);
    }

    public boolean isBlameable() {
        return isBlameable(this.mStates);
    }

    public static boolean isStageable(Set<String> states) {
        return states.contains(CONFLICTING) || states.contains(MODIFIED) || states.contains(UNTRACKED_NOT_REMOVED);
    }

    public static boolean isUnstageable(Set<String> states) {
        return states.contains(CHANGED) || states.contains(ADDED) || states.contains(REMOVED);
    }

    public static boolean isRemovable(Set<String> states) {
        return states.contains(COMMITED) || states.contains(ADDED) || states.contains(MODIFIED) || states.contains(CHANGED) || states.contains(CONFLICTING) || states.contains(MISSING);
    }

    public static boolean isDeletable(Set<String> states) {
        return states.contains(ADDED) || states.contains(MODIFIED) || states.contains(CHANGED) || states.contains(REMOVED) || states.contains(CONFLICTING) || states.contains(IGNORED) || states.contains(UNTRACKED) || states.contains(UNCOMMITED) || states.contains(COMMITED);
    }

    public static boolean isRevertable(Set<String> states) {
        return states.contains(MISSING) || states.contains(MODIFIED);
    }

    public static boolean isDiffable(Set<String> states) {
        return states.contains(MODIFIED) || states.contains(CHANGED) || states.contains(ADDED) || states.contains(REMOVED);
    }

    public static boolean isBlameable(Set<String> states) {
        return states.size() == 1 && states.contains(COMMITED);
    }
}
