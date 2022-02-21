package unsw.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import unsw.utils.Angle;

/**
 * This class is a super of Device and Satellite.
 * @author z5312190
 */

public class Entity {

    private String id;
    private String type;
    private Angle position;
    private List<File> files;
    private double height;
    private double maxRange;
    private List<String> supports = new ArrayList<String>();
    private boolean visited;

    /**
     * Constructor for Entity class
     * @param id
     * @param type
     * @param position
     * @param height
     */
    public Entity(String id, String type, Angle position, double height) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.height = height;
        this.files = new ArrayList<File>();
        this.visited = false;
        this.setMaxRange(type);
        this.setSupports(type);
    }

    /**
     * Appends file to file list
     * @param file
     */
    public void addFile(File file) {
        this.files.add(file);
    }

    /**
     * Removes file from file list
     * @param file
     */
    public void removeFile(File file) {
        this.files.remove(file);
    }

    /**
     * Helper function that sets all entity's visited state to false
     */
    public static void setVisitedFalse(List<Entity> entities) {
        for (Entity obj : entities) {
            obj.setVisited(false);
        }
    }

    /**
     * Method to transfer file byte for byte
     * @param transferRate
     * @param file
     * @param progress
     * @param editableFileInBytes
     * @param originalFileInBytes
     */
    public static void transfer(int transferRate, File file, int progress, List<String> editableFileInBytes,  List<String> originalFileInBytes, List<File> files) {
        for (int i = 0; i < transferRate; i++) {
            int j = i + 1;  // Helper variable to check 
            if (progress + j >= File.retrieveFile(file.getFilename(), files).getFileSize()) { // Check if we've reached the end of the file
                editableFileInBytes.add(originalFileInBytes.get(progress + i));
                file.setHasTransferCompleted(true); // Set File transfer status as complete
                break;
            } else {
                editableFileInBytes.add(originalFileInBytes.get(progress + i));
            }
        }
        file.setData(String.join("", editableFileInBytes));
        file.setBytesTransfered(progress + transferRate);
    }

    /**
     * Retrieves the entity with given ID
     * @param id
     * @return  entity that holds the given ID
     */
    public static Entity retrieveEntity (String id, List<Entity> entities) {
        for (Entity obj : entities) {
            if (obj.getId().equals(id)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * Remove an entity from the entities list
     * @param id
     */
    public static void removeEntity(String id, List<Entity> entities) {
        for (Iterator<Entity> it = entities.iterator(); it.hasNext();) {
            Entity anEntity = it.next();
            if (anEntity.getId().equals(id)) {
                it.remove();
            }
        }
    }
    
    /////////////    Getters And Setters    /////////////

    public void setMaxRange(String type) {
        if (type.equals("HandheldDevice")) {
            this.maxRange = 50000;
        } else if (type.equals("LaptopDevice")) {
            this.maxRange = 100000;
        } else if (type.equals("DesktopDevice")) {
            this.maxRange = 200000;
        } else if (type.equals("StandardSatellite")) {
            this.maxRange = 150000;
        } else if (type.equals("ShrinkingSatellite")) {
            this.maxRange = 200000;
        } else if (type.equals("RelaySatellite")) {
            this.maxRange = 300000;
        }
    }
    
    public void setSupports(String type) {
        if (type.matches("(.*)Satellite")) {
            this.supports.add("HandheldDevice");
            this.supports.add("LaptopDevice");
            this.supports.add("StandardSatellite");
            this.supports.add("ShrinkingSatellite");
            this.supports.add("RelaySatellite");
            if (type.equals("ShrinkingSatellite") || type.equals("RelaySatellite")) {
                this.supports.add("DesktopDevice");
            }
        } else if (type.matches("(.*)Device")) {
            this.supports.add("ShrinkingSatellite");
            this.supports.add("RelaySatellite");
            if (!type.equals("DesktopDevice")) {
                this.supports.add("StandardSatellite");
            }
        }
    }
    
    public void setVisited(boolean status) {
        this.visited = status;
    }

    public boolean getVisited() {
        return this.visited;
    }

    public List<String> getSupports() {
        return this.supports;
    }

    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public Angle getPosition() {
        return this.position;
    }

    public void setPosition(Angle position) {
        this.position = position;
    }

    public List<File> getFiles() {
        return this.files;
    }

    public double getHeight() {
        return this.height;
    }

    public double getMaxRange() {
        return this.maxRange;
    }

}
