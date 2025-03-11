package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date date;
    private HashMap<String, String> refs;
    private String father;
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File COMMIT_DIR = join(CWD, ".gitlet", "commit");
    public static final File ADD_LIST = join(CWD, ".gitlet", "staging", "add_list");
    public static final File ADD_DIR = join(CWD, ".gitlet", "staging", "add");
    public static final File RM_LIST = join(CWD, ".gitlet", "staging", "rm_list");
    public static final File TMP = Utils.join(COMMIT_DIR, "tmp");
    public static final File BLOB_DIR = join(CWD, ".gitlet", "blob");

    /* TODO: fill in the rest of this class. */
    /** make first commit
     *
     */
    public Commit(String msg, Date now) {
        message = msg;
        date = now;
        refs = new HashMap<>();
        father = null;
    }

    /** according to father-commit's tracking list(represented as String set)
     *  and staging files, update hashmap.
     */
    public void trackFiles(HashMap<String, String> fatherTracking) {
        String[] addList = Utils.readObject(ADD_LIST, String[].class);
        String[] rmList = Utils.readObject(RM_LIST, String[].class);
        for (String s : addList) {
            File add = Utils.join(ADD_DIR, s);
            byte[] contents = Utils.readContents(add);
            String ref = Utils.sha1(contents);
            fatherTracking.put(s, ref);
            File blob = join(BLOB_DIR, ref);
            try {
                blob.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writeContents(blob, contents);
        }
        for (String s : rmList) {
            fatherTracking.remove(s);
        }
        refs = fatherTracking;
    }

    /** save me...
     *  just save this commit to correct directory, set filename as hash value.
     */
    public String saveCommit() {
        if (!TMP.exists()) {
            try {
                TMP.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Utils.writeObject(TMP, this);
        byte[] content = Utils.readContents(TMP);
        String ref = Utils.sha1(content);
        File path = Utils.join(COMMIT_DIR, ref);
        try {
            path.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Utils.writeObject(path, this);
        return ref;
    }

    public Date getDate() {
        return date;
    }

    public String getFather() {
        return father;
    }

    public String getMessage() {
        return message;
    }

    public HashMap<String, String> getRefs() {
        return refs;
    }

    public void setFather(String father) {
        this.father = father;
    }
}
