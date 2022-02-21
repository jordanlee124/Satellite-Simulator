package unsw.entities;

import java.util.List;

/** Represents a File
 * @author z5312190
 */

public class File {
    private String filename;
    private String data;
    private int fileSize;
    private boolean hasTransferCompleted;
    private int bytesTransfered = 0;
    private Entity from;

    
    /**
     * Constructor for File
     * @param filename
     * @param data
     * @param fileSize
     * @param hasTransferCompleted
     */
    public File(String filename, String data, int fileSize, boolean hasTransferCompleted, Entity from) {
        this.filename = filename;
        this.data = data;
        this.fileSize = fileSize;
        this.hasTransferCompleted = hasTransferCompleted;
        this.from = from;
    }

    /**
     * Check if file with provided file name exists anywhere
     * @param fileName
     * @return true when it exists, false otherwise.
     */
    public static boolean fileExists (String fileName, List<File> files) {
        for (File file : files) {
            if (file.getFilename().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve file object with provided filename
     * @param fileName
     * @return file object upon success, else return null
     */
    public static File retrieveFile (String fileName, List<File> files) {
        for (File file : files) {
            if (file.getFilename().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    public Entity getFromEntity() {
        return this.from;
    }

    public int getBytesTransfered() {
        return this.bytesTransfered;
    }

    public void setBytesTransfered(int bytes) {
        this.bytesTransfered = bytes;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String content) {
        this.data = content;
    }
    
    public int getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public boolean getHasTransferCompleted() {
        return this.hasTransferCompleted;
    }

    public void setHasTransferCompleted(boolean hasTransferCompleted) {
        this.hasTransferCompleted = hasTransferCompleted;
    }
}
