package com.aor.pocketgit.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.database.ProjectsDataSource;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CredentialStorage {
    private static Map<String, String> mPassphrases = new HashMap<>();
    private static Map<String, String> mPasswords = new HashMap<>();

    public interface CredentialListener {
        void credentialsSet();

        void invalidPrivateKey();
    }

    private static String generateServerKey(Project project) {
        return project.getUrl() + "|" + project.getUsername();
    }

    public static String getPassword(Project project) {
        if (project.getPassword() != null && !project.getPassword().trim().equals("")) {
            return project.getPassword();
        }
        String serverKey = generateServerKey(project);
        if (mPasswords.containsKey(serverKey)) {
            return mPasswords.get(serverKey);
        }
        return null;
    }

    public static void setPassword(Project project, String password) {
        if (password != null && !password.trim().equals("")) {
            mPasswords.put(generateServerKey(project), password);
        }
    }

    @SuppressLint({"InflateParams"})
    public static void checkCredentials(Context context, Project project, CredentialListener listener) {
        if (project.getAuthentication() == 0) {
            listener.credentialsSet();
        }
        if (project.getAuthentication() == 1) {
            String password = getPassword(project);
            if (password == null || password.trim().equals("")) {
                final View dialogPassword = LayoutInflater.from(context).inflate(R.layout.dialog_password, (ViewGroup) null);
                final Context context2 = context;
                final Project project2 = project;
                final CredentialListener credentialListener = listener;
                FontUtils.setRobotoFont(context, new MaterialDialog.Builder(context)
						.title((CharSequence) "Enter password")
						.iconRes(R.drawable.ic_key)
						.customView(dialogPassword, false)
						.positiveText(R.string.button_ok)
						.negativeText(R.string.button_cancel)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
					public void onClick(MaterialDialog dialog, DialogAction which) {
                        EditText input = (EditText) dialogPassword.findViewById(R.id.text_password);
                        if (((CheckBox) dialogPassword.findViewById(R.id.check_save_password)).isChecked()) {
                            ProjectsDataSource datasource = new ProjectsDataSource(context2);
                            datasource.open();
                            datasource.updatePassword(Integer.toString(project2.getId()), input.getText().toString());
                            datasource.close();
                        }
                        CredentialStorage.setPassword(project2, input.getText().toString());
                        credentialListener.credentialsSet();
                    }
                }).show().getWindow().getDecorView());
            } else {
                listener.credentialsSet();
            }
        }
        if (project.getAuthentication() == 2) {
            final File privateKey = new File(project.getPrivateKey());
            if (!privateKey.exists()) {
                Toast.makeText(context, "Can't find private key", 0).show();
                return;
            }
            try {
                final KeyPair keyPair = KeyPair.load(new JSch(), privateKey.getAbsolutePath());
                if (keyPair.isEncrypted()) {
                    String passphrase = getPassphrase(privateKey.getAbsolutePath());
                    if (passphrase == null || passphrase.trim().equals("")) {
                        final View dialogPassword2 = LayoutInflater.from(context).inflate(R.layout.dialog_passphrase, (ViewGroup) null);
                        final Context context3 = context;
                        final Project project3 = project;
                        final CredentialListener credentialListener2 = listener;
                        Context context4 = context;
                        FontUtils.setRobotoFont(context4, new MaterialDialog.Builder(context)
								.iconRes(R.drawable.ic_key)
								.title((CharSequence) "Enter passphrase")
								.customView(dialogPassword2, false)
								.positiveText((CharSequence) "Enter")
								.negativeText(R.string.button_cancel)
								.onPositive(new MaterialDialog.SingleButtonCallback() {
							public void onClick(MaterialDialog dialog, DialogAction which) {
                                String passphrase = ((EditText) dialogPassword2.findViewById(R.id.text_passphrase)).getText().toString();
                                keyPair.decrypt(passphrase);
                                if (keyPair.isEncrypted()) {
                                    CredentialStorage.checkCredentials(context3, project3, credentialListener2);
                                    return;
                                }
                                CredentialStorage.setPassphrase(privateKey.getAbsolutePath(), passphrase);
                                credentialListener2.credentialsSet();
                            }
                        }).show().getWindow().getDecorView());
                        return;
                    }
                    keyPair.decrypt(passphrase);
                    if (keyPair.isEncrypted()) {
                        removePassphrase(privateKey.getAbsolutePath());
                        checkCredentials(context, project, listener);
                        return;
                    }
                    setPassphrase(privateKey.getAbsolutePath(), passphrase);
                    listener.credentialsSet();
                    return;
                }
                listener.credentialsSet();
            } catch (JSchException e) {
                if (e.getMessage().contains("invalid privatekey")) {
                    listener.invalidPrivateKey();
                }
            } catch (Exception e2) {
                Log.e("Pocket Git", "", e2);
            }
        }
    }

    static String getPassphrase(String path) {
        return mPassphrases.get(path);
    }

    private static void setPassphrase(String path, String passphrase) {
        mPassphrases.put(path, passphrase);
    }

    private static void removePassphrase(String path) {
        mPassphrases.remove(path);
    }

    public static void removePassword(Project project) {
        mPasswords.remove(generateServerKey(project));
    }
}
