package com.aor.pocketgit.utils;

import android.util.Log;
import java.security.MessageDigest;

public class MD5Util {
    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (byte b : array) {
            sb.append(Integer.toHexString((b & 255) | 256).substring(1, 3));
        }
        return sb.toString();
    }

    public static String md5Hex(String message) {
        try {
            return hex(MessageDigest.getInstance("MD5").digest(message.getBytes("CP1252")));
        } catch (Exception e) {
            Log.e("PocketGit", "", e);
            return null;
        }
    }
}
