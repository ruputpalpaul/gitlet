package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Repository.GITLET_DIR;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */

    public static final SimpleDateFormat DATE = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");


    private String message;
    String parent;

    public void setParent2(String parent2) {
        this.parent2 = parent2;
    }

    String parent2;
    public HashMap<String, Blobs> getBlobmap() {
        return blobmap;
    }

    public void setBlobmap(HashMap<String, Blobs> blobmap) {
        this.blobmap = blobmap;
    }

    HashMap<String, Blobs> blobmap;
    String timestamp;
    private String ID;

    public Commit(String msg, String par, String par2, HashMap<String, Blobs> blo, Boolean val) {
        this.message = msg;
        this.parent = par;
        this.parent2 = par2;
        this.blobmap = blo;
        this.ID = Utils.sha1(Utils.serialize(this), message);
        if (val) {
            timestamp = DATE.format(new Date(0)) + " -0800";
        } else {
            timestamp = DATE.format(new Date()) + " -0800";
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getID() {
        return ID;
    }

    public HashMap<String, Blobs> getBlobs() {
        return blobmap;
    }

    public Commit getParent() {
        if (parent == null) {
            return null;
        }
        File f = Utils.join(GITLET_DIR, parent);
        return Utils.readObject(f, Commit.class);
    }

    public Commit getParent2() {
        if (parent2 == null) {
            return null;
        }
        File f = Utils.join(GITLET_DIR, parent2);
        return Utils.readObject(f, Commit.class);
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
