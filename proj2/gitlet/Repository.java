package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
        Utils.writeContents(HEAD_FILE, "master");
        makeCommit(firstCommit);
    }

    /** whether a gitlet system exist
     *  check whether there are commits
     */
    public static boolean exist() {
        List<String> sl = Utils.plainFilenamesIn(COMMIT_DIR);
        if (sl == null) {
            return false;
        } else {
            return !sl.isEmpty();
        }
    }

    /** make commit.
     *  all the process.
     *  get father commit, create new commit file, save it, clear the staging area.
     * @param msg user message of this commit
     */
    public static void commit(String msg) {
        // get father commit
        String fatherRef = getHeadCommit();
        Commit father = getCommitFromRef(fatherRef);
        // create new commit
        Commit child = new Commit(msg, new Date());
        child.trackFiles(father.getRefs());
        child.setFather(fatherRef);
        // make commit
        makeCommit(child);
        for (String filename : plainFilenamesIn(ADD_DIR)) {
            File file = join(ADD_DIR, filename);
            file.delete();
        }
        String[] sl = {};
        Utils.writeObject(ADD_LIST, sl);
        Utils.writeObject(RM_LIST, sl);
    }

    /** already build a commit class var.
     *  do the commit(last half).
     *  files are all tracked correctly, now save the commit,
     *  update the branch file. overwrite the head file.(maybe redundant)
     *  @param commit commit class var
     */
    private static void makeCommit(Commit commit) {
        String ref = commit.saveCommit();
        String branch = Utils.readContentsAsString(HEAD_FILE);
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

    /** add_list and rm_list are stored as Sting[].
     *  convenient methods to change their contents.
     * @param listFile ADD_LIST or RM_LIST
     * @param add filename that need to be added to the list
     */
    private static void addItemsToListFile(File listFile, String add) {
        List<String> addList = Arrays.asList(Utils.readObject(listFile, String[].class));
        List<String> tmp = new ArrayList<>(addList);
        if (!tmp.contains(add)) {
            tmp.add(add);
            Utils.writeObject(listFile, tmp.toArray(new String[0]));
        }
    }

    /** convenient method to remove things from Sting[] files.
     *  almost same as addItemsToListFile.
     * @param listFile ADD_LIST or RM_LST.
     * @param rm filename that need to be removed from the list.
     */
    private static void removeItemsFromListFile(File listFile, String rm) {
        List<String> rmList = Arrays.asList(Utils.readObject(listFile, String[].class));
        if (rmList.contains(rm)) {
            List<String> tmp = new ArrayList<>(rmList);
            tmp.remove(rm);
            Utils.writeObject(listFile, tmp.toArray(new String[0]));
        }
    }

//    private static String getHashOfFile(File dir, String filename) {
//        File file = join(dir, filename);
//        return getHashOfFile(file);
//    }

    /** get hash value of the file.
     *
     * @param file file need to be handled
     * @return hash value
     */
    private static String getHashOfFile(File file) {
        byte[] content = Utils.readContents(file);
        return Utils.sha1(content);
    }

    /** add.
     *  if already removed, cancel the remove and add it to the staging area.
     *  if already added, overwrite it. (if same, then nothing changes.)
     *  if same with commit version, then do not add it.
     * @param filename must be in CWD
     */
    public static void add(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        removeItemsFromListFile(RM_LIST, filename);
        String branch = readContentsAsString(HEAD_FILE);
        File branchFile = join(BRANCH_DIR, branch);
        String branchRef = readContentsAsString(branchFile);
        Commit now = getCommitFromRef(branchRef);
        if (now.getRefs().containsKey(filename)) {
            String oldRef = now.getRefs().get(filename);
            String newRef = getHashOfFile(file);
            if (oldRef.equals(newRef)) {
                return;
            }
        }
        File copy = join(ADD_DIR, filename);
        if (!copy.exists()) {
            try {
                copy.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        byte[] content = readContents(file);
        Utils.writeContents(copy, content);
        addItemsToListFile(ADD_LIST, filename);
    }

    /** check staging area to see if any changes are recorded.
     *
     * @return T or F
     */
    public static boolean changesExist() {
        List<String> addList = Arrays.asList(Utils.readObject(ADD_LIST, String[].class));
        List<String> rmList = Arrays.asList(Utils.readObject(RM_LIST, String[].class));
        return !(addList.isEmpty() && rmList.isEmpty());
    }

    /** check head file
     *  check out file from last commit
     * @param fileName
     */
    public static void checkCommitFile(String fileName) {
        String branch = readContentsAsString(HEAD_FILE);
        File branchFile = join(BRANCH_DIR, branch);
        String branchRef = readContentsAsString(branchFile);
        checkCommitFile(branchRef, fileName);
    }

    /** check commit file
     * check file from ref String
     * @param commitRef
     * @param fileName
     */
    public static void checkCommitFile(String commitRef, String fileName) {
        Commit branch = getCommitFromRef(commitRef);
        byte[] blob = getFileContentFromCommit(branch, fileName);
        File file = join(CWD, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(file, blob);
    }

    /** whether a commit exist.
     *  judge by its hash value.
     * @param ref
     * @return
     */
    public static boolean commitExist(String ref) {
        String fullRef = ref;
        if (ref.length() == 6) {
            List<String> fileList = plainFilenamesIn(COMMIT_DIR);
            for (String s : fileList) {
                if (s.equals("tmp")) {
                    continue;
                }
                if (ref.equals(s.substring(0, 6))) {
                    fullRef = s;
                }
            }
        }
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        return commitList.contains(fullRef);
    }

    /** branch name known, get the commit of that brach.
     *
     * @param commitRef
     * @return commit
     */
    private static Commit getCommitFromRef(String commitRef) {
        String fullRef = commitRef;
        if (commitRef.length() == 6) {
            List<String> fileList = plainFilenamesIn(COMMIT_DIR);
            for (String s : fileList) {
                if (s.equals("tmp")) {
                    continue;
                }
                if (commitRef.equals(s.substring(0, 6))) {
                    fullRef = s;
                }
            }
        }
        File commitBlob = join(COMMIT_DIR, fullRef);
        return readObject(commitBlob, Commit.class);
    }

    /** commit known, get the tracking file content.
     *  if no such file, return null.
     * @param commit
     * @param fileName
     * @return
     */
    private static byte[] getFileContentFromCommit(Commit commit, String fileName) {
        HashMap<String, String> map = commit.getRefs();
        if (!map.containsKey(fileName)) {
            return null;
        }
        String ref = map.get(fileName);
        File blob = join(BLOB_DIR, ref);
        return readContents(blob);
    }

    public static void checkBranch(String branchName) {

    }

    /** whether the file exists in head branch.
     *
     * @param fileName
     * @return
     */
    public static boolean commitFileExist(String fileName) {
        String branch = readContentsAsString(HEAD_FILE);
        File branchFile = join(BRANCH_DIR, branch);
        String branchRef = readContentsAsString(branchFile);
        return commitFileExist(branchRef, fileName);
    }

    /** whether the file exists in certain branch.
     *
     * @param commitRef
     * @param fileName
     * @return
     */
    public static boolean commitFileExist(String commitRef, String fileName) {
        Commit branch = getCommitFromRef(commitRef);
        HashMap<String, String> map = branch.getRefs();
        return map.containsKey(fileName);
    }

    private static String getHeadCommit() {
        String branch = Utils.readContentsAsString(HEAD_FILE);
        return Utils.readContentsAsString(join(BRANCH_DIR, branch));
    }

    private static String getFatherRef(String commitRef) {
        Commit c = getCommitFromRef(commitRef);
        return c.getFather();
    }

    private static void commitLog(String ref) {
        Commit c = getCommitFromRef(ref);
        System.out.println("===");
        System.out.println("commit " + ref);
        Formatter fmt = new Formatter();
        fmt.format("%ta %tb %te %tT %tY %tz", c.getDate(), c.getDate(),
                c.getDate(), c.getDate(), c.getDate(), c.getDate());
        System.out.println("Date: " + fmt);
        System.out.println(c.getMessage());
        System.out.println();
    }

    public static void log() {
        String ref = getHeadCommit();
        while (ref != null) {
            commitLog(ref);
            ref = getFatherRef(ref);
        }
    }

}
