package com.aor.pocketgit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.aor.pocketgit.data.Project;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.lib.ConfigConstants;

public class ProjectsDataSource {
    private String[] allColumns = { "id", ConfigConstants.CONFIG_KEY_NAME, "url", "local_path", "authentication", "username", "password", "privatekey", "state" };
    private SQLiteDatabase database;
    private PocketDbHelper dbHelper;

    public ProjectsDataSource(Context context) {
        this.dbHelper = new PocketDbHelper(context);
    }

    public void open() throws SQLException {
        this.database = this.dbHelper.getWritableDatabase();
    }

    public void close() {
        this.dbHelper.close();
    }

    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        Cursor cursor = this.database.query("projects", this.allColumns, (String) null, (String[]) null, (String) null, (String) null, "name COLLATE NOCASE");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            projects.add(cursorToProject(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return projects;
    }

    private Project cursorToProject(Cursor cursor) {
        return new Project(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8));
    }

    public void createProject(String name, String url, String localPath, int authentication, String username, String password, String privateKey) {
        ContentValues values = new ContentValues();
        values.put(ConfigConstants.CONFIG_KEY_NAME, name);
        values.put("url", url);
        values.put("local_path", localPath);
        values.put("authentication", Integer.valueOf(authentication));
        values.put("username", username);
        values.put("password", password);
        values.put("privatekey", privateKey);
        values.put("state", Project.STATE_UNKNOWN);
        this.database.insert("projects", (String) null, values);
    }

    public void updateProject(String id, String name, String url, String localPath, int authentication, String username, String password, String privateKey) {
        ContentValues values = new ContentValues();
        values.put(ConfigConstants.CONFIG_KEY_NAME, name);
        values.put("url", url);
        values.put("local_path", localPath);
        values.put("authentication", Integer.valueOf(authentication));
        values.put("username", username);
        values.put("password", password);
        values.put("privatekey", privateKey);
        this.database.update("projects", values, "id = ?", new String[] { id });
    }

    public void deleteProject(Integer id) {
        this.database.delete("projects", "id = ?", new String[] { Integer.toString(id.intValue()) });
    }

    public Project getProject(String id) {
        Project project = null;
        Cursor cursor = this.database.query("projects", this.allColumns, "id = ?", new String[] { id }, (String) null, (String) null, (String) null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            project = cursorToProject(cursor);
        }
        cursor.close();
        return project;
    }

    public Project getProjectFromURL(String url) {
        Project project = null;
        Cursor cursor = this.database.query("projects", this.allColumns, "url = ?", new String[] { url }, (String) null, (String) null, (String) null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            project = cursorToProject(cursor);
        }
        cursor.close();
        return project;
    }

    public void updatePassword(String id, String password) {
        ContentValues values = new ContentValues();
        values.put("password", password);
        this.database.update("projects", values, "id = ?", new String[]{id});
    }

    public void updateState(String id, String state) {
        ContentValues values = new ContentValues();
        values.put("state", state);
        this.database.update("projects", values, "id = ?", new String[]{id});
    }
}
