package com.aor.pocketgit.utils;

import android.util.Log;
import com.aor.pocketgit.data.GitFile;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.services.GitService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.Session;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class GitUtils {
    public static boolean repositoryCloned(Project project) {
        if (new File(project.getLocalPath()).exists() && new File(project.getLocalPath(), ".git").exists()) {
            return true;
        }
        return false;
    }

    private static boolean hasAtLeastOneReference(Project project) throws IOException {
        for (Ref ref : getRepository(project).getAllRefs().values()) {
            if (ref.getObjectId() != null) {
                return true;
            }
        }
        return false;
    }

    private static void setPrivateKey(final String privateKey) {
        JSch.setLogger(new Logger() {
            public void log(int level, String message) {
                Log.d("Pocket Git", message);
            }

            public boolean isEnabled(int arg0) {
                return true;
            }
        });
        SshSessionFactory.setInstance(new JschConfigSessionFactory() {
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
                session.setConfig("PreferredAuthentications", "publickey,password");
            }

            protected JSch getJSch(OpenSshConfig.Host arg0, FS arg1) throws JSchException {
                JSch jsch = super.getJSch(arg0, arg1);
                if (privateKey == null || privateKey.trim().equals("") || !new File(privateKey).exists()) {
                    jsch.removeAllIdentity();
                } else {
                    String passphrase = CredentialStorage.getPassphrase(privateKey);
                    if (passphrase != null) {
                        try {
                            if (!passphrase.trim().equals("")) {
                                jsch.addIdentity(privateKey, passphrase);
                            }
                        } catch (JSchException e) {
                            Log.e("Pocket Git", "", e);
                        }
                    }
                    jsch.addIdentity(privateKey);
                }
                return jsch;
            }
        });
    }

    private static void setSSHPassword(final String password) {
        JSch.setLogger(new Logger() {
            public void log(int level, String message) {
                Log.d("Pocket Git", message);
            }

            public boolean isEnabled(int arg0) {
                return true;
            }
        });
        SshSessionFactory.setInstance(new JschConfigSessionFactory() {
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
                session.setConfig("PreferredAuthentications", "password,publickey");
                session.setPassword(password);
            }
        });
    }

    private static void setUnknownHost() {
        JSch.setLogger(new Logger() {
            public void log(int level, String message) {
                Log.d("Pocket Git", message);
            }

            public boolean isEnabled(int arg0) {
                return true;
            }
        });
        SshSessionFactory.setInstance(new JschConfigSessionFactory() {
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }
        });
    }

    public static void cloneRepository(Project project, final GitService service) throws GitAPIException, IOException {
        CloneCommand clone = Git.cloneRepository().setURI(project.getUrl()).setDirectory(new File(project.getLocalPath())).setProgressMonitor(new ProgressMonitor() {
            private int done = 0;
            private int total = 0;

            public void update(int completed) {
                this.done += completed;
                service.updateNotification(this.total, this.done, GitService.CHANNEL_START_ID);
            }

            public void start(int totalTasks) {
            }

            public boolean isCancelled() {
                return false;
            }

            public void endTask() {
            }

            public void beginTask(String title, int totalWork) {
                this.total = totalWork;
                this.done = 0;
                service.updateNotificationMessage(this.total, this.done, title, GitService.CHANNEL_START_ID);
            }
        });
        setCredentials(project, clone);
        clone.call();
    }

    private static boolean containsException(Throwable e, Class<?> needle) {
        if (needle.isInstance(e)) {
            return true;
        }
        if (e.getCause() == null) {
            return false;
        }
        return containsException(e.getCause(), needle);
    }

    public static void stageFiles(Project project, List<GitFile> files) throws IOException, GitAPIException {
        AddCommand addCommand = new Git(getRepository(project)).add();
        for (GitFile file : files) {
            addCommand.addFilepattern(getFilePattern(project, file));
        }
        addCommand.call();
    }

    public static void unstageFiles(Project project, List<GitFile> files) throws IOException, GitAPIException {
        ResetCommand resetCommand = new Git(getRepository(project)).reset();
        resetCommand.setRef("HEAD");
        for (GitFile file : files) {
            resetCommand.addPath(getFilePattern(project, file));
        }
        resetCommand.call();
    }

    public static void removeFiles(Project project, List<GitFile> files) throws IOException, GitAPIException {
        RmCommand removeCommand = new Git(getRepository(project)).rm();
        removeCommand.setCached(true);
        for (GitFile file : files) {
            removeCommand.addFilepattern(getFilePattern(project, file));
        }
        removeCommand.call();
    }

    public static void revertFiles(Project project, List<GitFile> files) throws GitAPIException, IOException, JGitInternalException {
        CheckoutCommand checkout = new Git(getRepository(project)).checkout();
        for (GitFile file : files) {
            checkout.addPath(getFilePattern(project, file));
        }
        checkout.call();
    }

    public static void revert(Project project) throws GitAPIException, IOException {
        for (int i = 0; i < 2; i++) {
            new Git(getRepository(project)).reset().setRef("HEAD").setMode(ResetCommand.ResetType.HARD).call();
        }
    }

    public static void pullRepository(Project project, final GitService service, String remote) throws GitAPIException, IOException {
        PullCommand pull = new Git(getRepository(project)).pull().setProgressMonitor(new ProgressMonitor() {
            private int done = 0;
            private int total = 0;

            public void update(int completed) {
                this.done += completed;
                service.updateNotification(this.total, this.done, GitService.CHANNEL_START_ID);
            }

            public void start(int totalTasks) {
            }

            public boolean isCancelled() {
                return false;
            }

            public void endTask() {
            }

            public void beginTask(String title, int totalWork) {
                this.total = totalWork;
                this.done = 0;
                service.updateNotificationMessage(this.total, this.done, title, GitService.CHANNEL_START_ID);
            }
        });
        pull.setRemote(remote);
        setCredentials(project, pull);
        pull.call();
    }

    public static void fetchRepository(Project project, final GitService service, String remote) throws GitAPIException, IOException {
        FetchCommand fetch = new Git(getRepository(project)).fetch().setProgressMonitor(new ProgressMonitor() {
            private int done = 0;
            private int total = 0;

            public void update(int completed) {
                this.done += completed;
                service.updateNotification(this.total, this.done, GitService.CHANNEL_START_ID);
            }

            public void start(int totalTasks) {
            }

            public boolean isCancelled() {
                return false;
            }

            public void endTask() {
            }

            public void beginTask(String title, int totalWork) {
                this.total = totalWork;
                this.done = 0;
                service.updateNotificationMessage(this.total, this.done, title, GitService.CHANNEL_START_ID);
            }
        });
        fetch.setRemote(remote);
        setCredentials(project, fetch);
        fetch.call();
    }

    public static String pushRepository(Project project, final GitService service, String remote, boolean pushTags) throws GitAPIException, IOException {
        PushCommand push = new Git(getRepository(project)).push().setProgressMonitor(new ProgressMonitor() {
            private int done = 0;
            private int total = 0;

            public void update(int completed) {
                this.done += completed;
                service.updateNotification(this.total, this.done, GitService.CHANNEL_START_ID);
            }

            public void start(int totalTasks) {
            }

            public boolean isCancelled() {
                return false;
            }

            public void endTask() {
            }

            public void beginTask(String title, int totalWork) {
                this.total = totalWork;
                this.done = 0;
                service.updateNotificationMessage(this.total, this.done, title, GitService.CHANNEL_START_ID);
            }
        });
        if (pushTags) {
            push.setPushTags();
        }
        push.setRemote(remote);
        setCredentials(project, push);
        Iterable<PushResult> results = push.call();
        StringBuilder sb = new StringBuilder();
        for (PushResult r : results) {
            for (RemoteRefUpdate remoteUpdate : r.getRemoteUpdates()) {
                if (remoteUpdate.getStatus().toString().equals("REJECTED_NODELETE")) {
                    sb.append("Remote ref update was rejected, because remote side doesn't support/allow deleting refs.\n");
                }
                if (remoteUpdate.getStatus().toString().equals("REJECTED_NONFASTFORWARD")) {
                    sb.append("Updates were rejected because the remote contains work that you do not have locally. This is usually caused by another repository pushing to the same ref. You may want to first integrate the remote changes using pull.\n");
                }
                if (remoteUpdate.getStatus().toString().equals("REJECTED_OTHER_REASON")) {
                    sb.append("Remote ref update was rejected.\n");
                }
                if (remoteUpdate.getStatus().toString().equals("REJECTED_REMOTE_CHANGED")) {
                    sb.append("Remote ref update was rejected, because old object id on remote repository wasn't the same as defined expected old object.\n");
                }
            }
            sb.append(r.getMessages());
            sb.append(10);
        }
        return sb.toString();
    }

    private static void setCredentials(Project project, TransportCommand command) {
        if (project.getAuthentication() == 1) {
            command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(project.getUsername(), CredentialStorage.getPassword(project)));
            setSSHPassword(CredentialStorage.getPassword(project));
        }
        if (project.getAuthentication() == 2) {
            setPrivateKey(project.getPrivateKey());
        }
        if (project.getAuthentication() == 0) {
            setUnknownHost();
        }
    }

    public static Repository getRepository(Project project) throws IOException {
        return new FileRepositoryBuilder().setGitDir(new File(project.getLocalPath() + "/.git")).readEnvironment().build();
    }

    private static String getFilePattern(Project project, GitFile file) throws IOException {
        return file.getFile().getCanonicalPath().substring(new File(project.getLocalPath()).getCanonicalPath().length() + 1);
    }

    public static String formatDate(int time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(((long) time) * 1000));
    }

    public static String formatDateShort(int time) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(((long) time) * 1000));
    }

    public static List<DiffEntry> getDiffList(Repository repository, RevCommit commit, RevWalk walk) throws IOException {
        List<DiffEntry> diffList = new ArrayList<>();
        for (int p = 0; p < commit.getParentCount(); p++) {
            RevCommit parent = walk.parseCommit(commit.getParent(p));
            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(repository);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            diffList.addAll(df.scan((AnyObjectId) parent, (AnyObjectId) commit));
        }
        return diffList;
    }

    public static String getDiffText(Repository repository, DiffEntry entry) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(baos);
        df.setRepository(repository);
        df.format(entry);
        return baos.toString();
    }

    /* JADX INFO: finally extract failed */
    public static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        RevWalk walk = new RevWalk(repository);
        RevTree tree = walk.parseTree(walk.parseCommit(repository.resolve("HEAD")).getTree().getId());
        CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
        ObjectReader oldReader = repository.newObjectReader();
        try {
            oldTreeParser.reset(oldReader, tree.getId());
            oldReader.close();
            walk.dispose();
            return oldTreeParser;
        } catch (Throwable th) {
            oldReader.close();
            throw th;
        }
    }

    public List<Tag> getTags(Repository repository) throws GitAPIException, IOException {
        Git git = new Git(repository);
        RevWalk walk = new RevWalk(repository);
        List<Tag> toReturnTags = new ArrayList<>();
        for (Ref tag : git.tagList().call()) {
            RevObject object = walk.parseAny(tag.getObjectId());
            if (object instanceof RevTag) {
                toReturnTags.add(new Tag(((RevTag) object).getTagName(), ((RevTag) object).getFullMessage()));
            } else if (object instanceof RevCommit) {
                toReturnTags.add(new Tag(Repository.shortenRefName(tag.getName()), ""));
            }
        }
        return toReturnTags;
    }

    public class Tag {
        private final String mMessage;
        private final String mName;

        public Tag(String name, String message) {
            this.mName = name;
            this.mMessage = message;
        }

        public String getName() {
            return this.mName;
        }

        public String getMessage() {
            return this.mMessage;
        }
    }
}
