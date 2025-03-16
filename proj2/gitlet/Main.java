package gitlet;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                if (Repository.exist()) {
                    throw new GitletException("A Gitlet version-control system already exists in the current directory.");
                }
                Repository.init();
                break;
            case "add":
                validateNumArgs("add", args, 2);
                File file = new File(args[1]);
                if (!file.exists()) {
                    throw new GitletException("File does not exist.");
                }
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                if (args.length != 2) {
                    throw new GitletException("Please enter a commit message.");
                }
                if (!Repository.changesExist()) {
                    System.out.println("No changes added to the commit.");
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;
            case "checkout":
                if (args.length == 2) {
                    Repository.checkBranch(args[1]);
                } else if (args.length == 3) {
                    if (!Repository.commitFileExist(args[2])) {
                        throw new GitletException("File does not exist in that commit.");
                    }
                    Repository.checkCommitFile(args[2]);
                } else if (args.length == 4) {
                    if (!Repository.commitExist(args[1])) {
                        System.out.println("No commit with that id exists.");
                        System.exit(0);
                    }
                    if (!Repository.commitFileExist(args[1], args[3])) {
                        throw new GitletException("File does not exist in that commit.");
                    }
                    Repository.checkCommitFile(args[1], args[3]);
                } else {
                    throw new RuntimeException(
                            String.format("Invalid number of arguments for: checkout."));
                }
                break;
            case "log":
                Repository.log();
                break;
            case "rm":
                if (!(Repository.isTracking(args[1]) || Repository.isStaging(args[1]))) {
                    throw new GitletException("No reason to remove the file.");
                }
                Repository.rm(args[1]);
                break;
            case "global-log":
                Repository.logAll();
                break;
            case "find":
                Repository.findMsg(args[1]);
                break;
            case "status":
                Repository.showStatus();
                break;
            case "branch":
                if (Repository.branchExists(args[1])) {
                    throw new GitletException("A branch with that name already exists.");
                }
                Repository.createBranch(args[1]);
                break;
            case "rm-branch":
                if (!Repository.branchExists(args[1])) {
                    throw new GitletException("A branch with that name does not exist.");
                }
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                Repository.reset(args[1]);
                break;

        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
