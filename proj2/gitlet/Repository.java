package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File BRANCH_DIR = join(GITLET_DIR, "branch");
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    public static final File ADD_DIR = join(STAGING_DIR, "add");
    public static final File ADD_LIST = join(STAGING_DIR, "add_list");
    public static final File RM_LIST = join(STAGING_DIR, "rm_list");
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");
    public static final File BLOB_DIR = join(GITLET_DIR, "blob");
    public static final File HEAD_FILE = join(GITLET_DIR, "head");

    /* TODO: fill in the rest of this class. */
    /** init
     * set up persistence, create directories
     * start with an initial commit
     */
    public static void init() {
        GITLET_DIR.mkdir();
        BRANCH_DIR.mkdir();
        STAGING_DIR.mkdir();
        ADD_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        if (!HEAD_FILE.exists()) {
            try {
                HEAD_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!ADD_LIST.exists()) {
            try {
                ADD_LIST.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!RM_LIST.exists()) {
            try {
                RM_LIST.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String[] sl = {};
        Utils.writeObject(ADD_LIST, sl);
        Utils.writeObject(RM_LIST, sl);
        Commit firstCommit = new Commit("initial commit", new Date(0));
        makeCommit(firstCommit, "master");
    }

    /** whether a gitlet system exist
     *
     */
    public static boolean exist() {
        List<String> sl = Utils.plainFilenamesIn(COMMIT_DIR);
        if (sl == null) {
            return false;
        } else {
            return !sl.isEmpty();
        }
    }

    private static Commit getFatherCommit() {
        String branch = Utils.readContentsAsString(HEAD_FILE);
        String fatherRef = Utils.readContentsAsString(join(BRANCH_DIR, branch));
        return readObject(join(COMMIT_DIR, fatherRef), Commit.class);
    }

    private static Commit createCommit(Commit father, HashMap<String, Boolean> map, String msg) {
        Commit child = new Commit(msg, new Date());
    }

    /** make commit.
     *  all the process.
     */
    public static void commit(String msg) {
        // get father commit
        Commit father = getFatherCommit();
        // get add list and rm list
        List<String> addList = Arrays.asList(readObject(ADD_LIST, String[].class));
        List<String> rmList = Arrays.asList(readObject(RM_LIST, String[].class));
        // get filename list that would be tracked
        HashMap<String, Boolean> trakingFiles = new HashMap<>();
        for (String filename : father.getRefs().keySet()) {
            trakingFiles.put(filename, false);
        }
        for (String rm : rmList) {
            trakingFiles.remove(rm);
        }
        for (String add : addList) {
            trakingFiles.put(add, true);
        }
        // create new commit
        Commit child = createCommit(father, trakingFiles, msg);
        // make commit
    }

    /** already build a commit class var.
     *  do the commit(last half).
     */
    private static void makeCommit(Commit commit, String branch) {
        String ref = commit.saveCommit();
        Utils.writeContents(HEAD_FILE, branch);
        File branchFile = Utils.join(BRANCH_DIR, branch);
        if (!branchFile.exists()) {
            try {
                branchFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Utils.writeContents(branchFile, ref);
    }

    /** add.
     *
     */
    public static void add(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        List<String> addList = Arrays.asList(Utils.readObject(ADD_LIST, String[].class));
        List<String> rmList = Arrays.asList(Utils.readObject(RM_LIST, String[].class));
        if (rmList.contains(filename)) {
            List<String> tmp = new ArrayList<>(rmList);
            tmp.remove(filename);
            Utils.writeObject(RM_LIST, tmp.toArray(new String[0]));
            return;
        }
        if (addList.contains(filename)) {
            File old = Utils.join(ADD_DIR, filename);
            if (!old.equals(file)) {
                byte[] content = Utils.readContents(file);
                Utils.writeContents(old, content);
            }
        } else {
            File copy = Utils.join(ADD_DIR, filename);
            byte[] content = Utils.readContents(file);
            Utils.writeContents(copy, content);
            List<String> tmp = new ArrayList<>(addList);
            tmp.add(filename);
            Utils.writeObject(ADD_LIST, tmp.toArray(new String[0]));
        }
    }

}
