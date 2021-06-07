package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     *  java gitlet.Main add hello.txt
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command");
            System.exit(0);
        }
        String firstArg = args[0];
        if (!firstArg.equals("init")) {
            if (!Repository.GITLET_DIR.exists() || !Repository.GITLET_DIR.isDirectory()) {
                System.out.println("Not in an initialized Gitlet directory");
                System.exit(0);
            }
        }
        Command cmd = new Command();
        switch (firstArg) {
            case "init":
                cmd.init(args);
                break;
            case "add":
                cmd.add(args);
                break;
            case "commit":
                cmd.commit(args);
                break;
            case "log":
                cmd.log(args);
                break;
            case "global-log":
                cmd.globalLog(args);
                break;
            case "checkout":
                cmd.checkout(args);
                break;
            case "rm":
                cmd.rm(args);
                break;
            case "find":
                cmd.find(args);
                break;
            case "status":
                cmd.status(args);
                break;
            case "branch":
                cmd.branch(args);
                break;
            case "rm-branch":
                cmd.rmbranch(args);
                break;
            case "reset":
                cmd.reset(args);
                break;
            case "merge":
                cmd.merge(args);
                break;
            default:
                System.out.println("No command with that name exists");
                System.exit(0);
        }
    }
}
