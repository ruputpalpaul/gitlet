package gitlet;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @Arnav Singhvi
 */
public class Repository implements Serializable {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = new File(".gitlet");

    public static final File COMMIT_DIR = Utils.join(GITLET_DIR, "tree");

    private String curbranch;
    private HashMap<String, Blobs> stagedFiles;
    private Commit curHead;
    private HashMap<String, Commit> branchmap = new HashMap<>();
    private final HashMap<String, String> masterbranches = new HashMap<>();
    private HashMap<String, Blobs> stagedFilesRm = new HashMap<>();

    public void init() {
        File f = Utils.join(CWD, ".gitlet");
        if (!f.exists()) {
            GITLET_DIR.mkdir();
            stagedFiles = new HashMap<>();
            Commit iniCmt = new Commit("initial commit", null, null, new HashMap<>(), true);
            curHead = iniCmt;
            curbranch = "master";
            masterbranches.put(curbranch, iniCmt.getID());
            branchmap.put(iniCmt.getID(), iniCmt);
            File x = Utils.join(GITLET_DIR, iniCmt.getID());
            Utils.writeObject(x, iniCmt);
            Utils.writeObject(COMMIT_DIR, this);
        } else {
            System.out.println("A Gitlet version-control system already"
                    + " exists in the current directory");
        }
    }

    public void add(String name) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        File branch = Utils.join(GITLET_DIR, save.masterbranches.get(save.curbranch));
        HashMap<String, Blobs> head = Utils.readObject(branch, Commit.class).getBlobmap();
        File f = Utils.join(CWD, name);
        if (f.exists()) {
            if ((head.containsKey(name))) {
                if (!(Utils.readContentsAsString(f).equals(head.get(name).getContents()))) {
                    save.stagedFiles.put(name, new Blobs(f));
                }
            } else {
                save.stagedFiles.put(name, new Blobs(f));
            }
            if (save.stagedFilesRm.containsKey(name)) {
                save.stagedFilesRm.remove(name);
            }
        } else {
            System.out.println("File does not exists");
            System.exit(0);
        }
        Utils.writeObject(COMMIT_DIR, save);
    }

    public void rm(String name) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        File branch = Utils.join(GITLET_DIR, save.masterbranches.get(save.curbranch));
        HashMap<String, Blobs> head = Utils.readObject(branch, Commit.class).getBlobmap();
        if (head.keySet().contains(name)) {
            save.stagedFilesRm.put(name, save.stagedFiles.get(name));
            save.stagedFiles.remove(name);
            save.curHead.getBlobmap().remove(name);
            File f = Utils.join(CWD, name);
            if ((f.exists())) {
                f.delete();
            }
        } else if (save.stagedFiles.containsKey(name)) {
            save.stagedFiles.remove(name);
        } else {
            System.out.println("No reason to remove the file.");
        }
        Utils.writeObject(COMMIT_DIR, save);
    }

    public void commit(String msg) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        if (msg.equals("")) {
            System.out.println("Please enter a commit message");
        }
        if (save.getStageFiles().size() > 0 || save.getStagedFilesRm().size() > 0) {
            HashMap<String, Blobs> saverr = new HashMap<>();
            if (save.curHead.getBlobmap() != null) {
                saverr = save.curHead.getBlobmap();
                for (String x : save.stagedFiles.keySet()) {
                    if (saverr.containsKey(x)) {
                        saverr.replace(x, save.stagedFiles.get(x));
                    } else {
                        saverr.put(x, save.stagedFiles.get(x));
                    }
                }
                for (String x: save.stagedFilesRm.keySet()) {
                    saverr.remove(x);
                }
            }
            Commit newCmt = new Commit(msg, save.curHead.getID(), null, saverr, false);
            save.curHead = newCmt;
            save.masterbranches.replace(save.curbranch, newCmt.getID());
            save.branchmap.put(newCmt.getID(), newCmt);
            Utils.writeObject(Utils.join(GITLET_DIR, newCmt.getID()), newCmt);
            save.stagedFiles.clear();
            save.stagedFilesRm.clear();
            Utils.writeObject(COMMIT_DIR, save);
        } else {
            System.out.println("No changes added to the commit");
            save.stagedFiles.clear();
            save.stagedFilesRm.clear();
            Utils.writeObject(COMMIT_DIR, save);
            System.exit(0);
        }
    }

    public void log() {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        while (save.getCurHead() != null) {
            System.out.println("===");
            System.out.println("commit " + save.getCurHead().getID());
            System.out.println("Date: " + save.getCurHead().getTimestamp());
            System.out.println(save.getCurHead().getMessage() + "\n");
            save.curHead = save.getCurHead().getParent();
        }
    }
    public void globalLog() {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        for (String name : Utils.plainFilenamesIn(GITLET_DIR)) {
            if (save.getBranchmap().get(name) == null) {
                break;
            }
            System.out.println("===");
            System.out.println("commit " + save.getBranchmap().get(name).getID());
            System.out.println("Date: " + save.getBranchmap().get(name).getTimestamp());
            System.out.println(save.getBranchmap().get(name).getMessage() + "\n");
        }
    }
    public void find(String msg) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        int counter = 0;
        for (String x: Utils.plainFilenamesIn(GITLET_DIR)) {
            if (!x.equals("tree")) {
                if (save.getBranchmap().get(x).getMessage().equals(msg)) {
                    System.out.println(x);
                    counter += 1;
                }
            }
        }
        if (counter == 0) {
            System.out.println("Found no commit with that message");
            System.exit(0);
        }
    }

    public void checkoutFirst(String file) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        File branch = Utils.join(GITLET_DIR, save.masterbranches.get(save.curbranch));
        HashMap<String, Blobs> head = Utils.readObject(branch, Commit.class).getBlobmap();
        if (head.containsKey(file)) {
            File f = Utils.join(CWD, file);
            Utils.writeContents(f, head.get(file).getContents());
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Utils.writeObject(COMMIT_DIR, save);
    }

    public void checkoutSecond(String file, String cmtId) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        if (cmtId.length() < 30) {
            cmtId = abbreviate(cmtId);
        }
        if (save.getBranchmap().containsKey(cmtId)) {
            File fileWithId = Utils.join(GITLET_DIR, cmtId);
            HashMap<String, Blobs> blobs = Utils.readObject(fileWithId, Commit.class).getBlobs();
            if (blobs.containsKey(file)) {
                File f = Utils.join(CWD, file);
                Utils.writeContents(f, blobs.get(file).getContents());
            } else {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
        } else {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
        Utils.writeObject(COMMIT_DIR, save);
    }

    public String abbreviate(String id) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        int length = id.length();
        for (String x: save.getBranchmap().keySet()) {
            if (x.substring(0, length).equals(id)) {
                return x;
            }
        }
        return null;
    }

    public void checkoutThird(String name) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        if (save.masterbranches.containsKey(name)) {
            File branch = Utils.join(GITLET_DIR, save.masterbranches.get(save.curbranch));
            File branchForCheckout = Utils.join(GITLET_DIR, save.masterbranches.get(name));
            Commit cmtBranchCheckout = Utils.readObject(branchForCheckout, Commit.class);
            Commit cmtBranch = Utils.readObject(branch, Commit.class);
            HashMap<String, Blobs> head = cmtBranchCheckout.getBlobmap();
            HashMap<String, Blobs> head2 = cmtBranch.getBlobmap();
            if (save.getCurbranch().equals(name)) {
                System.out.println("No need to checkout the current branch");
                System.exit(0);
            } else {
                for (String x : Utils.plainFilenamesIn(CWD)) {
                    if (head.containsKey(x)) {
                        if (!head2.containsKey(x)) {
                            System.out.println("There is an untracked file in the way;"
                                    +   " delete it, or add and commit it first");
                            System.exit(0);
                        }
                    }
                }
                for (String x : Utils.plainFilenamesIn(CWD)) {
                    if ((head.containsKey(x)) && (head2.containsKey(x))) {
                        String fileContents = Utils.readContentsAsString(Utils.join(CWD, x));
                        if (!(head.get(x).getContents().equals(fileContents))) {
                            Utils.restrictedDelete(x);
                        }
                    } else if (!(head.containsKey(x)) && ((head2.containsKey(x)))) {
                        Utils.restrictedDelete(x);
                    } else if (!(head.containsKey(x)) && (!(head2.containsKey(x)))) {
                        Utils.restrictedDelete(x);
                    } else {
                        continue;
                    }
                }
                for (String x : head.keySet()) {
                    Utils.writeContents(Utils.join(CWD, x), head.get(x).getContents());
                }
                save.setCurbranch(name);
                File brf = Utils.join(GITLET_DIR, save.masterbranches.get(name));
                save.setCurHead(Utils.readObject(brf, Commit.class));
                if (Utils.readObject(brf, Commit.class).parent != null) {
                    save.curHead.parent = Utils.readObject(brf, Commit.class).parent;
                }
                save.curbranch = name;
                save.stagedFiles.clear();
                save.stagedFilesRm.clear();
            }
        } else {
            System.out.println("No such branch exists");
            System.exit(0);
        }
        Utils.writeObject(COMMIT_DIR, save);
    }

    public void status() {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        File bf = Utils.join(GITLET_DIR, save.masterbranches.get(save.curbranch));
        HashMap<String, Blobs> head = Utils.readObject(bf, Commit.class).getBlobmap();
        System.out.println("=== " + "Branches " + "===");
        ArrayList<String> branch = new ArrayList<String>(save.getMasterbranches().keySet().size());
        branch.addAll(save.getMasterbranches().keySet());
        Collections.sort(branch);
        for (String x: branch) {
            if (x.equals(save.getCurbranch())) {
                System.out.println("*" + x);
            } else {
                System.out.println(x);
            }
        }
        System.out.println();
        System.out.println("=== " + "Staged Files " + "===");
        for (String x: save.getStageFiles().keySet()) {
            System.out.println(x);
        }
        System.out.println();
        System.out.println("=== " + "Removed Files " + "===");
        for (String x: save.getStagedFilesRm().keySet()) {
            File f = Utils.join(CWD, x);
            if (f.exists()) {
                if (!(Utils.readContentsAsString(f).equals(head.get(x).getContents()))) {
                    System.out.println(x);
                }
            } else {
                System.out.println(x);
            }
        }
        System.out.println();
        System.out.println("=== " + "Modifications Not Staged For Commit " + "===");
        HashMap<String, Blobs> y = save.curHead.getBlobmap();
        for (String x: Utils.plainFilenamesIn(CWD)) {
            File f = Utils.join(CWD, x);
            List<String> l = Utils.plainFilenamesIn(CWD);
            if (y.containsKey(x) && !(save.getStageFiles().containsKey(x)) && !(l.contains(x)))  {
                System.out.println(x + " (deleted)");
            } else if (y.get(x) != null) {
                if (!(Utils.readContentsAsString(f).equals(y.get(x).getContents()))) {
                    if (save.getStageFiles().containsKey(x)) {
                        String fileContents = Utils.readContentsAsString(f);
                        if ((!(save.getStageFiles().get(x).getContents().equals(fileContents)))
                                && !(save.getStagedFilesRm().containsKey(x))) {
                            System.out.println(x + " (modified)");
                        }
                    }
                }
            }
        }

        System.out.println();
        System.out.println("=== " + "Untracked Files " + "===");
        HashSet<String> hs = new HashSet<>();
        for (String x: save.getBranchmap().keySet()) {
            File f = Utils.join(GITLET_DIR, x);
            Commit cmt = Utils.readObject(f, Commit.class);
            for (String s: cmt.getBlobmap().keySet()) {
                hs.add(s);
            }
        }
        for (String x: Utils.plainFilenamesIn(CWD)) {
            if (!(hs.contains(x)) && !(save.getStageFiles().containsKey(x))) {
                System.out.println(x);
            }
        }
        System.out.println();
    }

    public void branch(String name) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        if (save.masterbranches.containsKey(name)) {
            System.out.println("A branch with that name already exists");
            System.exit(0);
        }
        save.masterbranches.put(name, save.curHead.getID());
        Utils.writeObject(COMMIT_DIR, save);
    }

    public void rmbranch(String name) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        if (!(save.masterbranches.containsKey(name))) {
            System.out.println("A branch with that name does not exists");
            System.exit(0);
        }
        if (name.equals(save.curbranch)) {
            System.out.println("Cannot remove the current branch");
            System.exit(0);
        }
        save.masterbranches.remove(name);
        Utils.writeObject(COMMIT_DIR, save);
    }

    public void reset(String id) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        if (!(save.getBranchmap().containsKey(id))) {
            System.out.println("No commit with that id exists");
            System.exit(0);
        }
        if (id.length() < 30) {
            id = abbreviate(id);
        }
        HashMap<String, Blobs> head = save.curHead.getBlobmap();
        HashMap<String, Blobs> head2 = save.branchmap.get(id).getBlobmap();
        for (String x: Utils.plainFilenamesIn(CWD)) {
            if (head2.containsKey(x) && !(head.containsKey(x))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String x: Utils.plainFilenamesIn(CWD)) {
            if (head2.containsKey(x) && head.containsKey(x)) {
                String fileContents = Utils.readContentsAsString(Utils.join(CWD, x));
                if (!(head2.containsKey(x))) {
                    Utils.restrictedDelete(x);
                } else if (!(head2.get(x).getContents().equals(fileContents))) {
                    Utils.restrictedDelete(x);
                } else {
                    continue;
                }
            }
        }
        save.curHead = save.getBranchmap().get(id);
        save.stagedFiles.clear();
        save.stagedFilesRm.clear();
        save.masterbranches.replace(save.curbranch, save.curHead.getID());
        Utils.writeObject(COMMIT_DIR, save);
    }

    public void merge(String name) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        if (!(save.masterbranches.containsKey(name))) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        HashSet<String> branch1 = new HashSet<>();
        boolean conflict = false;
        Commit cmt = save.branchmap.get(save.getMasterbranches().get(save.getCurbranch()));
        String mergeID = save.getMasterbranches().get(name);
        File mergefile = Utils.join(GITLET_DIR, mergeID);
        Commit merge = Utils.readObject(mergefile, Commit.class);
        Commit split = findSplit(cmt, merge);
        branch1.addAll(cmt.getBlobmap().keySet());
        if ((save.stagedFiles.size() > 0 || save.getStagedFilesRm().size() > 0)) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (name.equals(save.getCurbranch())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        for (String x : Utils.plainFilenamesIn(CWD)) {
            if (!(cmt.getBlobmap().containsKey(x)) && (merge.getBlobmap().containsKey(x))) {
                System.out.println("There is an untracked file in the way;"
                        +   " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        branch1.addAll(merge.getBlobmap().keySet());
        if (split == null) {
            commit("Merged " + name + " into " + save.curbranch + ".");
            System.exit(0);
        }
        branch1.addAll(split.getBlobmap().keySet());
        if (split.getID().equals(merge.getID())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (split.getID().equals(cmt.getID())) {
            checkoutThird(name);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        for (String x: branch1) {
            if (split.getBlobmap().containsKey(x)) {
                if (cmt.getBlobmap().containsKey(x)) {
                    if (merge.getBlobmap().containsKey(x)) {
                        if (!(cmt.getBlobmap().get(x).getContents().equals(split.getBlobmap().get(x).getContents())) && !(merge.getBlobmap().get(x).getContents().equals(split.getBlobmap().get(x).getContents()))) {
                            File f = Utils.join(CWD, x);
                            Utils.writeContents(f, "<<<<<<< HEAD\n" + cmt.getBlobmap().get(x).getContents()
                                    + "=======\n" + merge.getBlobmap().get(x).getContents() + ">>>>>>>\n");
                            conflict = true;
                            save.stagedFiles.put(x, new Blobs(f));
                        } else if (!(split.getBlobmap().get(x).getContents().equals(merge.getBlobmap().get(x).getContents()))) {
                            if ((split.getBlobmap().get(x).getContents().equals(cmt.getBlobmap().get(x).getContents()))) {
                                File f = Utils.join(CWD, x);
                                Utils.writeContents(f, merge.getBlobmap().get(x).getContents());
                                save.stagedFiles.put(x, new Blobs(f));
                            }
                        } else if (!(split.getBlobmap().get(x).getContents().equals(cmt.getBlobmap().get(x).getContents()))) {
                            if (!(split.getBlobmap().get(x).getContents().equals(merge.getBlobmap().get(x).getContents()))) {
                                if((cmt.getBlobmap().get(x).getContents().equals(merge.getBlobmap().get(x).getContents()))) {
                                    continue;
                                }
                            }
                        }
                    }
                }
            }
            if (!split.getBlobmap().containsKey(x)) {
                if ((cmt.getBlobmap().containsKey(x))) {
                    if ((merge.getBlobmap().containsKey(x))) {
                        if (!(cmt.getBlobmap().get(x).getContents().equals(split.getBlobmap().get(x).getContents())) && !(merge.getBlobmap().get(x).getContents().equals(split.getBlobmap().get(x).getContents()))) {
                            File f = Utils.join(CWD, x);
                            Utils.writeContents(f, "<<<<<<< HEAD\n" + cmt.getBlobmap().get(x).getContents()
                                    + "=======\n" + merge.getBlobmap().get(x).getContents() + ">>>>>>>\n");
                            conflict = true;
                        }
                    }
                }
            }

            if (split.getBlobmap().containsKey(x)) {
                if (cmt.getBlobmap().containsKey(x)) {
                    if (!(merge.getBlobmap().containsKey(x))) {
                        if (!(cmt.getBlobmap().get(x).getContents().equals(split.getBlobmap().get(x).getContents())) && !(merge.getBlobmap().get(x).getContents().equals(split.getBlobmap().get(x).getContents()))) {
                            File f = Utils.join(CWD, x);
                            Utils.writeContents(f, "<<<<<<< HEAD\n" + cmt.getBlobmap().get(x).getContents()
                                    + "=======\n" + ">>>>>>>\n");
                            conflict = true;
                            save.stagedFiles.put(x, new Blobs(f));
                        } else {
                            File f = Utils.join(CWD, x);
                            save.stagedFilesRm.put(x, new Blobs(f));
                            Utils.restrictedDelete(f);
                        }
                    }
                }
            }
            if (split.getBlobmap().containsKey(x)) {
                if (!(cmt.getBlobmap().containsKey(x))) {
                    if (merge.getBlobmap().containsKey(x)) {
                        if (!(merge.getBlobmap().get(x).getContents().equals(split.getBlobmap().get(x).getContents()))) {
                            File f = Utils.join(CWD, x);
                            Utils.writeContents(f, "<<<<<<< HEAD\n" + cmt.getBlobmap().get(x).getContents()
                                    + "=======\n" + merge.getBlobmap().get(x).getContents() + ">>>>>>>\n");
                            conflict = true;
                        }
                    }
                }
            }
            if (!(split.getBlobmap().containsKey(x))) {
                if (!(cmt.getBlobmap().containsKey(x))) {
                    if (merge.getBlobmap().containsKey(x)) {
                        File f = Utils.join(CWD, x);
                        Utils.writeContents(f, merge.getBlobmap().get(x).getContents());
                        save.stagedFiles.put(x, new Blobs(f));
                    }
                }
            }
            if (!(split.getBlobmap().containsKey(x))) {
                if (cmt.getBlobmap().containsKey(x)) {
                    if (!(merge.getBlobmap().containsKey(x))) {
                        File f = Utils.join(CWD, x);
                        Utils.writeContents(f, cmt.getBlobmap().get(x).getContents());
                    }
                }
            }
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        if (save.stagedFiles.size() > 0 || save.stagedFilesRm.size() > 0) {
            Commit newCmt = new Commit("Merged " + name + " into " + save.curbranch + ".", save.curHead.getID(), merge.getID(), save.curHead.getBlobs(), false);
            save.curHead = newCmt;
            save.curHead.blobmap.putAll(save.stagedFiles);
            save.masterbranches.put(save.curbranch, newCmt.getID());
            save.branchmap.put(newCmt.getID(), newCmt);
            save.stagedFiles.clear();
            save.stagedFilesRm.clear();
            Utils.writeObject(Utils.join(GITLET_DIR, newCmt.getID()), newCmt);
        } else {
            System.out.println("No changes added to the commit.");
            save.stagedFiles.clear();
            save.stagedFilesRm.clear();
            Utils.writeObject(COMMIT_DIR, save);
            System.exit(0);
        }
        Utils.writeObject(COMMIT_DIR, save);
    }

    public Commit findSplit(Commit cur, Commit merge) {
        Repository save = Utils.readObject(COMMIT_DIR, Repository.class);
        ArrayList<String> lst1 = new ArrayList<>();
        while (merge != null) {
            lst1.add(merge.getID());
            merge = merge.getParent();
        }
        while (cur != null) {
            if (cur.parent != null && cur.parent2 != null) {
                ArrayList<Commit> parents = new ArrayList<>();
                Commit cur1 = cur.getParent();
                Commit cur2 = cur.getParent2();
                while (cur1 != null) {
                    if (lst1.contains(cur1.getID())) {
                        parents.add(cur1);
                        break;
                    }
                    cur1 = cur1.getParent();
                }
                while (cur2 != null) {
                    if (lst1.contains(cur2.getID())) {
                        parents.add(cur2);
                        break;
                    }
                    cur2 = cur2.getParent2();
                }
                // Compared timestamps using these sources: 1) use simpledateformat:
                // https://stackoverflow.com/questions/23225687/how-can-i-compare-two-
                // simpledateformat-types-in-java-with-the-following-spec-dd
                //
                // 2) compare timestamps: https://stackoverflow.com/questions/7913264/compare-two-timestamp-in-java
                // 3) how to try and catch parse exception:
                // https://examples.javacodegeeks.com/core-java/text/parseexception/java-text-
                // parseexception-how-to-solve-parseexception/
                Date min;
                try {
                    min = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy").parse(parents.get(0).timestamp);
                } catch (ParseException e) {
                    continue;
                }
                Commit c = parents.get(0);
                for (int i = 1; i < parents.size(); i++) {
                    Date d;
                    try {
                        d = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy").parse(parents.get(i).timestamp);
                    } catch (ParseException e) {
                        continue;
                    }
                    if (d.after(min)) {
                        min = d;
                        c = parents.get(i);
                    }
                }
                return c;
            } else if (lst1.contains(cur.getID())) {
                return cur;
            }
            cur = cur.getParent();
        }
        return null;
    }

    public Commit getCurHead() {
        return curHead;
    }

    public void setCurHead(Commit curHead) {
        this.curHead = curHead;
    }

    public HashMap<String, Commit> getBranchmap() {
        return branchmap;
    }

    public HashMap<String, String> getMasterbranches() {
        return masterbranches;
    }

    public HashMap<String, Blobs> getStageFiles() {
        return stagedFiles;
    }

    public HashMap<String, Blobs> getStagedFilesRm() {
        return stagedFilesRm;
    }

    public String getCurbranch() {
        return curbranch;
    }

    public void setCurbranch(String curbranch) {
        this.curbranch = curbranch;
    }
}