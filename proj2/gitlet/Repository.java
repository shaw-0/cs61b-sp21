package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author
 */
public class Repository {
    /**
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
        createFile(HEAD_FILE);
        createFile(ADD_LIST);
        createFile(RM_LIST);
        removeStaging();
        Commit firstCommit = new Commit("initial commit", new Date(0), 0);
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

    private static void removeStaging() {
        String[] sl = {};
        Utils.writeObject(ADD_LIST, sl);
        Utils.writeObject(RM_LIST, sl);
    }

    /** make commit.
     *  all the process.
     *  get father commit, create new commit file, save it, clear the staging area.
     * @param msg user message of this commit
     */
    public static void commit(String msg) {
        commit(msg, null);
    }

    public static void commit(String msg, String mergeFather) {
        // get father commit
        String fatherRef = getHeadCommitRef();
        Commit father = getCommitFromRef(fatherRef);
        // create new commit
        Commit child = new Commit(msg, new Date(), father.getDepth() + 1);
        child.setMergeFather(mergeFather);
        child.trackFiles(father.getRefs());
        child.setFather(fatherRef);
        // make commit
        makeCommit(child);
        for (String filename : plainFilenamesIn(ADD_DIR)) {
            File file = join(ADD_DIR, filename);
            file.delete();
        }
        removeStaging();
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
        createFile(branchFile);
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
                removeItemsFromListFile(ADD_LIST, filename);
                File copy = join(ADD_DIR, filename);
                if (copy.exists()) {
                    copy.delete();
                }
                return;
            }
        }
        pureAdd(filename);
    }

    private static void pureAdd(String filename) {
        File file = join(CWD, filename);
        File copy = join(ADD_DIR, filename);
        createFile(copy);
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
        createFile(file);
        writeContents(file, blob);
    }

    private static String shortRefToLongRef(String shortRef) {
        List<String> fileList = plainFilenamesIn(COMMIT_DIR);
        String longRef = shortRef;
        if (shortRef.length() < 40) {
            int len = shortRef.length();
            for (String s : fileList) {
                if (s.equals("tmp")) {
                    continue;
                }
                if (shortRef.equals(s.substring(0, len))) {
                    longRef = s;
                    break;
                }
            }
        }
        return longRef;
    }

    /** whether a commit exist.
     *  judge by its hash value.
     * @param ref
     * @return
     */
    public static boolean commitExist(String ref) {
        String fullRef = shortRefToLongRef(ref);
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        return commitList.contains(fullRef);
    }

    /** branch name known, get the commit of that brach.
     *
     * @param commitRef
     * @return commit
     */
    private static Commit getCommitFromRef(String commitRef) {
        String fullRef = shortRefToLongRef(commitRef);
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

    public static boolean branchExists(String branchName) {
//        List<String> commitList = plainFilenamesIn(BRANCH_DIR);
//        return commitList.contains(branchName);
        File branch = join(BRANCH_DIR, branchName);
        return branch.exists();
    }

    public static void checkBranch(String branchName) {
        if (!branchExists(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchName.equals(readContentsAsString(HEAD_FILE))) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File branchFile = join(BRANCH_DIR, branchName);
        checkCommit(readContentsAsString(branchFile));
        writeContents(HEAD_FILE, branchName);
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

    private static String getHeadCommitRef() {
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
        if (c.getMergeFather() != null) {
            System.out.println("Merge: " + c.getFather().substring(0, 7)
                    + " " + c.getMergeFather().substring(0, 7));
        }
        Formatter fmt = new Formatter();
        fmt.format("%ta %tb %te %tT %tY %tz", c.getDate(), c.getDate(),
                c.getDate(), c.getDate(), c.getDate(), c.getDate());
        System.out.println("Date: " + fmt);
        System.out.println(c.getMessage());
        System.out.println();
    }

    public static void log() {
        String ref = getHeadCommitRef();
        while (ref != null) {
            commitLog(ref);
            ref = getFatherRef(ref);
        }
    }

    public static boolean isStaging(String filename) {
        List<String> addList = Arrays.asList(Utils.readObject(ADD_LIST, String[].class));
        List<String> rmList = Arrays.asList(Utils.readObject(RM_LIST, String[].class));
        return addList.contains(filename) || rmList.contains(filename);
    }

    public static boolean isTracking(String filename) {
        Commit now = getCommitFromRef(getHeadCommitRef());
        return isTracking(filename, now);
    }

    public static boolean isTracking(String filename, Commit commit) {
        return commit.getRefs().containsKey(filename);
    }

    public static void rm(String filename) {
        Commit now = getCommitFromRef(getHeadCommitRef());
        removeItemsFromListFile(ADD_LIST, filename);
        if (now.getRefs().containsKey(filename)) {
            addItemsToListFile(RM_LIST, filename);
            restrictedDelete(filename);
        }
    }

    public static void logAll() {
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        for (String commitRef : commitList) {
            if (commitRef.equals("tmp")) {
                continue;
            }
            commitLog(commitRef);
        }
    }

    private static String find(String msg) {
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        for (String commitRef : commitList) {
            if (commitRef.equals("tmp")) {
                continue;
            }
            Commit commit = getCommitFromRef(commitRef);
            if (msg.equals(commit.getMessage())) {
                return commitRef;
            }
        }
        return null;
    }

    public static void findMsg(String msg) {
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        int count = 0;
        for (String commitRef : commitList) {
            if (commitRef.equals("tmp")) {
                continue;
            }
            Commit commit = getCommitFromRef(commitRef);
            if (msg.equals(commit.getMessage())) {
                System.out.println(commitRef);
                count = count + 1;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void showStatus() {
        System.out.println("=== Branches ===");
        String head = readContentsAsString(HEAD_FILE);
        System.out.println("*" + head);
        List<String> branches = plainFilenamesIn(BRANCH_DIR);
        if (branches != null) {
            for (String branch : branches) {
                if (branch.equals(head)) {
                    continue;
                }
                System.out.println(branch);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        List<String> addList = Arrays.asList(Utils.readObject(ADD_LIST, String[].class));
        for (String add : addList) {
            System.out.println(add);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        List<String> rmList = Arrays.asList(Utils.readObject(RM_LIST, String[].class));
        for (String rm : rmList) {
            System.out.println(rm);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void createBranch(String branchName) {
        String headRef = getHeadCommitRef();
        File newBranch = join(BRANCH_DIR, branchName);
        createFile(newBranch);
        writeContents(newBranch, headRef);
    }

    private static void createFile(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void removeBranch(String branchName) {
        if (branchName.equals(readContentsAsString(HEAD_FILE))) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File branch = join(BRANCH_DIR, branchName);
        branch.delete();
    }

    private static void untrackedFileCheck(Commit commit) {
        HashMap<String, String> branchTrackingFiles = commit.getRefs();
        for (String file : branchTrackingFiles.keySet()) {
            File overwrite = join(CWD, file);
            if ((!isTracking(file)) && overwrite.exists()) {
                System.out.println("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    private static void checkCommit(String commitRef) {
        Commit branch = getCommitFromRef(commitRef);
        untrackedFileCheck(branch);
        Commit head = getCommitFromRef(getHeadCommitRef());
        for (String file : head.getRefs().keySet()) {
            if (!isTracking(file, branch)) {
                restrictedDelete(file);
            }
        }
        HashMap<String, String> branchTrackingFiles = branch.getRefs();
        for (String file : branchTrackingFiles.keySet()) {
            File overwrite = join(CWD, file);
            byte[] content = readContents(join(BLOB_DIR, branchTrackingFiles.get(file)));
            createFile(overwrite);
            writeContents(overwrite, content);
        }
        removeStaging();
    }

    public static void reset(String ref) {
        String fullRef = shortRefToLongRef(ref);
        if (!commitExist(fullRef)) {
            System.out.println("No commit with that id exists.");
        }
        checkCommit(fullRef);
        String branch = readContentsAsString(HEAD_FILE);
        File branchFile = join(BRANCH_DIR, branch);
        writeContents(branchFile, fullRef);
    }

    private static String findSpiltPoint(String masterRef, String branchRef) {
        Commit deeper = getCommitFromRef(branchRef);
        Commit lessDeeper = getCommitFromRef(masterRef);
        String deeperRef = branchRef;
        String lessDeeperRef = masterRef;
        if (deeper.getDepth() < lessDeeper.getDepth()) {
            Commit tmp = deeper;
            deeper = lessDeeper;
            lessDeeper = tmp;
            deeperRef = masterRef;
            lessDeeperRef = branchRef;
        }
        int deeperDepth = deeper.getDepth();
        int lessDepth = lessDeeper.getDepth();
        for (int i = deeperDepth; i > lessDepth; i--) {
            deeperRef = deeper.getFather();
            deeper = getCommitFromRef(deeperRef);
        }
        for (int i = lessDepth; i > 0; i--) {
            if (deeperRef.equals(lessDeeperRef)) {
                return deeperRef;
            }
            deeperRef = deeper.getFather();
            deeper = getCommitFromRef(deeperRef);
            lessDeeperRef = lessDeeper.getFather();
            lessDeeper = getCommitFromRef(lessDeeperRef);
        }
        return find("initial commit");
    }

    /** how file changed.
     *  0: same with head/other branch
     *  1: changed
     *  -1: removed
     * @param filename
     * @param fileRef
     * @return
     */
    private static int howFileChanged(String filename, String fileRef) {
        return howFileChanged(filename, fileRef, getHeadCommitRef());
    }

    private static int howFileChanged(String filename, String fileRef, String commitRef) {
        Commit branch = getCommitFromRef(commitRef);
        HashMap<String, String> branchMap = branch.getRefs();
        if (!branchMap.containsKey(filename)) {
            return -1;
        }
        String newFileRef = branchMap.get(filename);
        if (fileRef.equals(newFileRef)) {
            return 0;
        }
        return 1;
    }

    private static boolean handleConflict(String filename, String branchRef) {
        String headRef = getHeadCommitRef();
        Commit head = getCommitFromRef(headRef);
        Commit branch = getCommitFromRef(branchRef);

        String headFileRef = head.getRefs().get(filename);
        String branchFileRef = branch.getRefs().get(filename);

        if (headFileRef.equals(branchFileRef)) {
            return false;
        }

        String headContent = "";
        String branchContent = "";

        if (headFileRef != null) {
            File headFile = join(BLOB_DIR, headFileRef);
            headContent = readContentsAsString(headFile);
        }
        if (branchFileRef != null) {
            File branchFile = join(BLOB_DIR, branchFileRef);
            branchContent = readContentsAsString(branchFile);
        }
        File cwdFile = join(CWD, filename);
        writeContents(cwdFile, "<<<<<<< HEAD\r\n" + headContent
                + "=======\r\n" + branchContent + ">>>>>>>");
        return true;
    }

    public static void merge(String branchName) {
        String headName = readContentsAsString(HEAD_FILE);
        if (headName.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String branchRef = readContentsAsString(join(BRANCH_DIR, branchName));
        Commit branch = getCommitFromRef(branchRef);
        untrackedFileCheck(branch);

        String headRef = getHeadCommitRef();
        String spiltPointRef = findSpiltPoint(headRef, branchRef);
        if (spiltPointRef.equals(branchRef)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (spiltPointRef.equals(headRef)) {
            checkCommit(branchRef);
            writeContents(HEAD_FILE, branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        Commit spiltPoint = getCommitFromRef(spiltPointRef);
        HashMap<String, String> spiltMap = spiltPoint.getRefs();
        boolean hasConflict = false;
        for (String filename : spiltMap.keySet()) {
            String fileRef = spiltMap.get(filename);
            int changeInHead = Repository.howFileChanged(filename, fileRef);
            int changeInBranch = howFileChanged(filename, fileRef, branchRef);
            if (changeInBranch == 1 && changeInHead == 0) {
                checkCommitFile(branchRef, filename);
                pureAdd(filename);
            } else if (changeInBranch == -1 && changeInHead == -1) {
                continue;
            } else if (changeInBranch == -1 && changeInHead == 0) {
                rm(filename);
            } else if (changeInBranch != 0 && changeInHead != 0) {
                hasConflict = handleConflict(filename, branchRef);
            }
        }
        HashMap<String, String> branchMap = branch.getRefs();
        HashMap<String, String> headMap = getCommitFromRef(headRef).getRefs();
        for (String filename : branchMap.keySet()) {
            if (!spiltMap.containsKey(filename)) {
                if (!headMap.containsKey(filename)) {
                    checkCommitFile(branchRef, filename);
                    pureAdd(filename);
                } else {
                    boolean tmp = handleConflict(filename, branchRef);
                    hasConflict = hasConflict || tmp;
                }
            }
        }
        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        commit("Merged " + branchName + " into " + headName + ".", branchRef);
    }

}
