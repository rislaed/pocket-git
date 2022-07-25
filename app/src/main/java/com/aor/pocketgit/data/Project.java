package com.aor.pocketgit.data;

public class Project {
    public static final int NONE = 0;
    public static final int PASSWORD = 1;
    public static final int PRIVATE_KEY = 2;
    public static final String STATE_CLONED = "CLONED";
    public static final String STATE_CLONING = "CLONING";
    public static final String STATE_FAILED = "FAILED";
    public static final String STATE_UNKNOWN = "UNKNOWN";
    private int mAuthentication;
    private int mId;
    private String mLocalPath;
    private String mName;
    private String mPassword;
    private String mPrivateKey;
    private String mState;
    private String mUrl;
    private String mUsername;

    public Project(int id, String name, String url, String localPath, int authentication, String username, String password, String privateKey, String state) {
        this.mId = id;
        this.mName = name;
        this.mLocalPath = localPath;
        this.mUrl = url;
        this.mAuthentication = authentication;
        this.mUsername = username;
        this.mPassword = password;
        this.mPrivateKey = privateKey;
        this.mState = state;
        if (this.mUrl != null && this.mUrl.startsWith("http") && this.mUrl.contains("//") && this.mUrl.contains("@") && this.mUrl.indexOf("//") < this.mUrl.indexOf(64)) {
            if (this.mUsername == null || this.mUsername.trim().equals("")) {
                this.mUsername = this.mUrl.substring(this.mUrl.indexOf("//") + 2, this.mUrl.indexOf(64));
            }
        }
    }

    public String getName() {
        return this.mName;
    }

    public int getId() {
        return this.mId;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public String getLocalPath() {
        return this.mLocalPath;
    }

    public int getAuthentication() {
        return this.mAuthentication;
    }

    public String getUsername() {
        return this.mUsername;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public String getPrivateKey() {
        return this.mPrivateKey;
    }

    public String getState() {
        return this.mState;
    }
}
