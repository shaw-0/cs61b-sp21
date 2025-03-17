package gitlet;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author
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
                validateNumArgs(args, 1);
                if (Repository.exist()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    System.exit(0);
                }
                Repository.init();
                break;
            case "add":
                repositoryCheck();
                validateNumArgs(args, 2);
                File file = new File(args[1]);
                if (!file.exists()) {
                    System.out.println("File does not exist.");
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;
            case "commit":
                if (args.length == 1) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                validateNumArgs(args, 2);
                if (!Repository.changesExist()) {
                    System.out.println("No changes added to the commit.");
                    System.exit(0);
                }
                if (args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;
            case "checkout":
                repositoryCheck();
                if (args.length == 2) {
                    Repository.checkBranch(args[1]);
                } else if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    if (!Repository.commitFileExist(args[2])) {
                        System.out.println("File does not exist in that commit.");
                        System.exit(0);
                    }
                    Repository.checkCommitFile(args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    if (!Repository.commitExist(args[1])) {
                        System.out.println("No commit with that id exists.");
                        System.exit(0);
                    }
                    if (!Repository.commitFileExist(args[1], args[3])) {
                        System.out.println("File does not exist in that commit.");
                        System.exit(0);
                    }
                    Repository.checkCommitFile(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "log":
                repositoryCheck();
                validateNumArgs(args, 1);
                Repository.log();
                break;
            case "rm":
                repositoryCheck();
                validateNumArgs(args, 2);
                if (!(Repository.isTracking(args[1]) || Repository.isStaging(args[1]))) {
                    System.out.println("No reason to remove the file.");
                    System.exit(0);
                }
                Repository.rm(args[1]);
                break;
            case "global-log":
                repositoryCheck();
                validateNumArgs(args, 1);
                Repository.logAll();
                break;
            case "find":
                repositoryCheck();
                validateNumArgs(args, 2);
                Repository.findMsg(args[1]);
                break;
            case "status":
                repositoryCheck();
                validateNumArgs(args, 1);
                Repository.showStatus();
                break;
            case "branch":
                repositoryCheck();
                validateNumArgs(args, 2);
                if (Repository.branchExists(args[1])) {
                    System.out.println("A branch with that name already exists.");
                    System.exit(0);
                }
                Repository.createBranch(args[1]);
                break;
            case "rm-branch":
                repositoryCheck();
                validateNumArgs(args, 2);
                if (!Repository.branchExists(args[1])) {
                    System.out.println("A branch with that name does not exist.");
                    System.exit(0);
                }
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                repositoryCheck();
                validateNumArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                repositoryCheck();
                validateNumArgs(args, 2);
                if (Repository.changesExist()) {
                    System.out.println("You have uncommitted changes.");
                    System.exit(0);
                }
                if (!Repository.branchExists(args[1])) {
                    System.out.println("A branch with that name does not exist.");
                    System.exit(0);
                }
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void repositoryCheck() {
        if (!Repository.exist()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
