package com.aor.pocketgit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.aor.pocketgit.data.GitFile;
import java.util.Comparator;

public class FileComparator implements Comparator<GitFile> {
    public static int SORT_DATE = 1;
    public static int SORT_DOWN = 1;
    public static int SORT_NAME = 0;
    public static int SORT_SIZE = 2;
    public static int SORT_TYPE = 3;
    public static int SORT_UP = 0;
    private final int mSortDirection;
    private final int mSortType;

    public FileComparator(int sortType, int sortDirection) {
        this.mSortType = sortType;
        this.mSortDirection = sortDirection;
    }

    public static Comparator<GitFile> getPreferedComparator(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new FileComparator(prefs.getInt("pref_sort_type", SORT_NAME), prefs.getInt("pref_sort_dir", SORT_DOWN));
    }

    public int compare(GitFile lhs, GitFile rhs) {
        int i = 1;
        if (lhs.isDirectory() && !rhs.isDirectory()) {
            return -1;
        }
        if (!lhs.isDirectory() && rhs.isDirectory()) {
            return 1;
        }
        if (this.mSortType == SORT_NAME) {
            int compareName = compareName(lhs, rhs);
            if (this.mSortDirection != SORT_DOWN) {
                i = -1;
            }
            return compareName * i;
        } else if (this.mSortType == SORT_DATE) {
            int compareDate = compareDate(lhs, rhs);
            if (this.mSortDirection != SORT_DOWN) {
                i = -1;
            }
            return compareDate * i;
        } else if (this.mSortType == SORT_SIZE) {
            int compareSize = compareSize(lhs, rhs);
            if (this.mSortDirection != SORT_DOWN) {
                i = -1;
            }
            return compareSize * i;
        } else if (this.mSortType != SORT_TYPE) {
            return 0;
        } else {
            int compareType = compareType(lhs, rhs);
            if (this.mSortDirection != SORT_DOWN) {
                i = -1;
            }
            return compareType * i;
        }
    }

    private int compareName(GitFile lhs, GitFile rhs) {
        return lhs.getFile().compareTo(rhs.getFile());
    }

    private int compareDate(GitFile lhs, GitFile rhs) {
        if (lhs.getFile().lastModified() == rhs.getFile().lastModified()) {
            return compareName(lhs, rhs);
        }
        return lhs.getFile().lastModified() < rhs.getFile().lastModified() ? 1 : -1;
    }

    private int compareSize(GitFile lhs, GitFile rhs) {
        if (lhs.getFile().length() == rhs.getFile().length()) {
            return compareName(lhs, rhs);
        }
        return lhs.getFile().length() < rhs.getFile().length() ? 1 : -1;
    }

    private int compareType(GitFile lhs, GitFile rhs) {
        String left = getExtension(lhs);
        String right = getExtension(rhs);
        if (left.equals(right)) {
            return compareName(lhs, rhs);
        }
        return left.compareTo(right);
    }

    private String getExtension(GitFile file) {
        String name = file.getName().toString();
        int index = name.lastIndexOf(46);
        if (index == -1) {
            return "";
        }
        return name.substring(index);
    }
}
