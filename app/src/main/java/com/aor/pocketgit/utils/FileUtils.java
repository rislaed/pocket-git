package com.aor.pocketgit.utils;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FileUtils {
    public static File[] searchFiles(File folder, String search, File gitFile) {
        return searchFiles(folder, search.toLowerCase(), new HashSet<>(), gitFile);
    }

    private static File[] searchFiles(File folder, String search, Set<File> alreadySearched, File gitFile) {
        Set<File> files = new HashSet<>();
        if (alreadySearched.contains(folder)) {
            return new File[0];
        }
        alreadySearched.add(folder);
        for (File file : folder.listFiles()) {
            if (!file.equals(gitFile)) {
                if (file.getName().toLowerCase().contains(search)) {
                    files.add(file);
                }
                if (file.isDirectory()) {
                    Collections.addAll(files, searchFiles(file, search, alreadySearched, gitFile));
                }
            }
        }
        return files.toArray(new File[0]);
    }
}
