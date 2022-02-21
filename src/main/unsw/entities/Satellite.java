package unsw.entities;

import java.util.List;

import unsw.utils.Angle;

/** Represents a Satellite which is a subclass of Entity
 * @author z5312190
 */

public class Satellite extends Entity {

    private double linearVelocity;
    private int maxBytes;
    private int inRate; //Bytes per min
    private int outRate;//

    /**
     * Constructor for Satellite
     * @param satelliteId
     * @param type
     * @param height
     * @param position
     */
    public Satellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, type, position, height);
        this.setLinearVelocity(type);
        this.setMaxByte(type);
        this.setinRate(type);
        this.setoutRate(type);
    }
    
    /**
     * Changes the satellite's position by using the provided change.
     * @param changeInAngle
     */
    public void changePosition(Angle changeInAngle) {
        setPosition(Angle.fromDegrees((getPosition().toDegrees() + changeInAngle.toDegrees()) % 360));
    }

    /**
     * @return total number of bytes stored on entity across all its files
     */
    public int totalBytesStored() {
        int totalBytes = 0;
        for (File file : getFiles()) {
            totalBytes += file.getFileSize();
        }
        return totalBytes;
    }

    /**
     * @return number of files in the middle of being transfered
     */
    public int numOfTransferingFiles() {
        int count = 0;
        for (File file : getFiles()) {
            if (file.getHasTransferCompleted() == false) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param entities
     * @return how many files are being downloaded to satellite.
     */
    public int inboundFiles(List<Entity> entities) {
        int count = 0;
        for (File file : this.getFiles()) {
            if (file.getHasTransferCompleted() == false) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param entities
     * @return how many files are being uploaded by satellite.
     */
    public int outboundingFiles(List<Entity> entities) {
        int outGoingFiles = 0;
        for (Entity entity : entities) {
            for (File file : entity.getFiles()) {
                if (file.getFromEntity().getId().equals(this.getId()) && !file.getHasTransferCompleted()) {
                    outGoingFiles++;
                }
            }
        }
        return outGoingFiles;
    }

    /**
     * @return number of files in satellite
     */
    public int numberofFiles() {
        if (this.getFiles() == null) {
            return 0;
        }
        return this.getFiles().size();
    }

    /////////////    Getters And Setters    /////////////

    public void setLinearVelocity(String type) {
        if (type.equals("StandardSatellite")) {
            this.linearVelocity = 2500;
        } else if (type.equals("ShrinkingSatellite")) {
            this.linearVelocity = 1000;
        } else if (type.equals("RelaySatellite")) {
            this.linearVelocity = 1500;
        }
    }

    public void setMaxByte(String type) {
        if (type.equals("StandardSatellite")) {
            this.maxBytes = 80;
        } else if (type.equals("ShrinkingSatellite")) {
            this.maxBytes = 150;
        }
    }

    public void setinRate(String type) {
        if (type.equals("StandardSatellite")) {
            this.inRate = 1;
        } else if (type.equals("ShrinkingSatellite")) {
            this.inRate = 15;
        }
    }

    public void setoutRate(String type) {
        if (type.equals("StandardSatellite")) {
            this.outRate = 1;
        } else if (type.equals("ShrinkingSatellite")) {
            this.outRate = 10;    
        }
    }

    public double getLinearVelocity() {
        return this.linearVelocity;
    }

    public int getInRate() {
        return this.inRate;
    }


    public int getOutRate() {
        return this.outRate;
    }

    public int getMaxBytes() {
        return this.maxBytes;
    }


}

