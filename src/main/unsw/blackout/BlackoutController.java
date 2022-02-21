package unsw.blackout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

import unsw.entities.Entity;
import unsw.entities.Device;
import unsw.entities.File;
import unsw.entities.Satellite;
import unsw.entities.satellites.RelaySatellite;
import unsw.entities.satellites.ShrinkingSatellite;
import unsw.entities.satellites.StandardSatellite;
import unsw.response.models.EntityInfoResponse;
import unsw.response.models.FileInfoResponse;
import unsw.utils.Angle;
import unsw.utils.MathsHelper;
import unsw.blackout.FileTransferException.VirtualFileAlreadyExistsException;
import unsw.blackout.FileTransferException.VirtualFileNoBandwidthException;
import unsw.blackout.FileTransferException.VirtualFileNoStorageSpaceException;
import unsw.blackout.FileTransferException.VirtualFileNotFoundException;

/** Communicates with front-end
 * @author z5312190
 */

public class BlackoutController {
    
    private List<Device> devices;
    private List<Satellite> satellites;
    private List<Entity> entities;
    private List<File> files;
    
    /**
     * Constructor for BlackoutController
     * @param devices
     * @param satellites
     * @param entities
     * @param files
     */
    public BlackoutController() {
        this.devices = new ArrayList<Device>();
        this.satellites = new ArrayList<Satellite>();
        this.entities = new ArrayList<Entity>();
        this.files = new ArrayList<File>();
    }
    
    /**
     * Create a device and add it to the devices and entities list
     * @param deviceId
     * @param type
     * @param position
     */
    public void createDevice(String deviceId, String type, Angle position) {
        // TODO: Task 1a)
        Device newDev = new Device(deviceId, type, position);
        devices.add(newDev);
        entities.add(newDev);
    }
    
    /**
     * Remove a device from devices and entities list
     * @param deviceId
     */
    public void removeDevice(String deviceId) {
        // TODO: Task 1b)
        for (Iterator<Device> it = devices.iterator(); it.hasNext();) {
            Device aDevice = it.next();
            if (aDevice.getId().equals(deviceId)) {
                it.remove();
            }
        }
        Entity.removeEntity(deviceId, entities);
    }
    
    /**
     * Create a new satellite and add it to satellites and entities list
     * @param satelliteId
     * @param type
     * @param height
     * @param position
     */
    public void createSatellite(String satelliteId, String type, double height, Angle position) {
        // TODO: Task 1c)
        Satellite newSatellite = null;
        if (type.equals("StandardSatellite")) {
            newSatellite = new StandardSatellite(satelliteId, height, position);
        } else if (type.equals("ShrinkingSatellite")) {
            newSatellite = new ShrinkingSatellite(satelliteId, type, height, position);;
        } else if (type.equals("RelaySatellite")) {
            newSatellite = new RelaySatellite(satelliteId, type, height, position);
        }
        satellites.add(newSatellite);
        entities.add(newSatellite);
    }
    
    /**
     * Remove a satellite from the satellites and entities list
     * @param satelliteId
     */
    public void removeSatellite(String satelliteId) {
        // TODO: Task 1d)
        for (Iterator<Satellite> it = satellites.iterator(); it.hasNext();) {
            Satellite aSatellite = it.next();
            if (aSatellite.getId().equals(satelliteId)) {
                it.remove();
            }
        }
        Entity.removeEntity(satelliteId, entities);
    }

    /**
     * @return a list of device IDs
     */
    public List<String> listDeviceIds() {
        // TODO: Task 1e)
        ArrayList<String> devIdList = new ArrayList<>();
        for (Device obj : devices) {
            devIdList.add(obj.getId());
        }
        return devIdList;
    }


    /**
     * @return a list of satellite IDs
     */
    public List<String> listSatelliteIds() {
        // TODO: Task 1f)
        ArrayList<String> satIdList = new ArrayList<>();
        for (Satellite obj : satellites) {
            satIdList.add(obj.getId());
        }
        return satIdList;
    }

    /**
     * Add a file to a device.
     * @param deviceId
     * @param filename
     * @param content
     */
    public void addFileToDevice(String deviceId, String filename, String content) {
        // TODO: Task 1g)
        Entity device = Entity.retrieveEntity(deviceId, entities);
        File newFile = new File(filename, content, content.length(), true, device);
        device.addFile(newFile);
        files.add(newFile);
    }

    /**
     * @param id
     * @return all relevant information of an entity given its ID
     */
    public EntityInfoResponse getInfo(String id) {
        // TODO: Task 1h)
        for (Entity obj : entities) {
            if (obj.getId().equals(id) && obj.getFiles().equals(null)) {
                EntityInfoResponse entity = new EntityInfoResponse(obj.getId(), obj.getPosition(), obj.getHeight(), obj.getType());
                return entity;
            } else if (obj.getId().equals(id) && !obj.getFiles().equals(null)) {
                List<File> files = obj.getFiles();
                Map<String, FileInfoResponse> fileMap = new HashMap<>();
                for(File file : files) {
                    fileMap.put(file.getFilename(), new FileInfoResponse(file.getFilename(), file.getData(), file.getFileSize(), file.getHasTransferCompleted()));
                }
                EntityInfoResponse entity = new EntityInfoResponse(obj.getId(), obj.getPosition(), obj.getHeight(), obj.getType(), fileMap);
                return entity;
            }
        }
        return null;
    }

    /**
     * Simulate the world. This includes satellite movement and file transfer
     */
    public void simulate() {
        // TODO Task 2a)
        //Simulate satellite movement
        for (Satellite obj : satellites) {
            Angle angVelocity = Angle.fromRadians(obj.getLinearVelocity()/obj.getHeight());
            obj.changePosition(angVelocity);
        }
        
        //Removes incomplete file transfers as recieving entity moves out of communicable range of sending entity     
        for (Entity entity : entities) {
            for (Iterator<File> it = entity.getFiles().iterator(); it.hasNext();) {
                File file = it.next();
                if (!communicableEntitiesInRange(file.getFromEntity().getId()).contains(entity.getId()) && !file.getHasTransferCompleted()) {
                    it.remove();
                }
            }
        }

        /**
         * Simulate file transfer
         */
        for (Entity entity : entities) {                // Loop through entities
            for (File file : entity.getFiles()) {       // Loop through entity's files
                if (!file.getHasTransferCompleted()) {  // Check if file is a complete file

                    /**
                     * Setting up variables that will be needed
                     * progress: transfer progress of file
                     * fileInBytes: gets the content of incomplete file and split characters into an Arraylist
                     * editableFileInBytes: variable to allow the size of the Arraylist fileInByte to be flexible
                     * originalFileInBytes: gets the content of the original file and splits the characters into an Arraylist
                     */
                    int progress = file.getBytesTransfered();
                    List<String> fileInBytes = Arrays.asList(file.getData().split(""));
                    List<String> editableFileInBytes = new ArrayList<>(fileInBytes);
                    List<String> originalFileInBytes = Arrays.asList(File.retrieveFile(file.getFilename(), files).getData().split(""));
                    int transferRate = 0;

                    /**
                     * Determine the value transferRate will take 
                     */
                    if (entity instanceof Device && file.getFromEntity() instanceof Satellite) {        // When Satellite is sending to a Device
                        Satellite from = (Satellite) file.getFromEntity();
                        transferRate = (int) from.getOutRate()/from.outboundingFiles(entities);
                    } else if (entity instanceof Satellite && file.getFromEntity() instanceof Device) { // When Device is sending to a Satellite
                        Satellite to = (Satellite) entity;
                        transferRate = (int) to.getInRate()/to.inboundFiles(entities);
                    } else {                                                                            // When Satellite is sending to a Satellite
                        Satellite to = (Satellite) entity;
                        Satellite from = (Satellite) file.getFromEntity();
                        if (to.getInRate()/to.inboundFiles(entities) >= from.getOutRate()/from.outboundingFiles(entities)) {        // Choose the value that is smaller
                            transferRate = from.getOutRate()/from.outboundingFiles(entities);                                       // 
                        } else if (to.getInRate()/to.inboundFiles(entities) <= from.getOutRate()/from.outboundingFiles(entities)) { //
                            transferRate = to.getInRate()/to.inboundFiles(entities);
                        }
                    }

                    //Simulate the file transfer
                    Entity.transfer(transferRate, file, progress, editableFileInBytes, originalFileInBytes, files);   

                    //Check for quantum compressor quality of ShrinkingSatellite only if the file transfer is complete
                    if (entity instanceof ShrinkingSatellite && file.getHasTransferCompleted() && file.getData().contains("quantum")) {
                        file.setFileSize(2 * (file.getFileSize() / 3));
                    }
                }
            }
        }
    }

    /**
     * Simulate for the specified number of minutes.
     * You shouldn't need to modify this function.
     */
    public void simulate(int numberOfMinutes) {
        for (int i = 0; i < numberOfMinutes; i++) {
            simulate();
        }
    }
    
    /**
     * @param id
     * @return a list of entity IDs that are within communicable range of an entity
     */
    public List<String> communicableEntitiesInRange(String id) {
        // TODO Task 2b)
        List<String> returnList = entityInRange(id);
        Entity entity = Entity.retrieveEntity(id, entities);
        
        //Remove any entity IDs that are not compatible with the source entity
        for (Iterator<String> it = returnList.iterator(); it.hasNext();) {
            String entityId = it.next();
            Entity entity2 = Entity.retrieveEntity(entityId, entities);
            if (!entity.getSupports().contains(entity2.getType())) {
                it.remove();
            }
        }

        //Set visited boolean to false for all entities
        Entity.setVisitedFalse(entities);
        return returnList;
    }
    
    /**
     * Helper function that does all the calculations and logic for communicableEntitiesInRange()
     * @param id
     * @return a list of entitie IDs that are in range of source entity
     */
    public List<String> entityInRange(String id) {
        List<String> returnList = new ArrayList<String>();
        Entity sourceEntity = Entity.retrieveEntity(id, entities);
        
        // Set sourceEntity as has been visited
        sourceEntity.setVisited(true); 
    
        // Loop through all entities within entity list
        for (Entity obj : entities) {
            double distance = 0;
            boolean isVisible = false;
            if (sourceEntity instanceof Device && obj instanceof Satellite) {           // Check if sourceEntity is of Device type and obj is of Satellite
                distance = MathsHelper.getDistance(obj.getHeight(), obj.getPosition(), sourceEntity.getPosition());
                isVisible = MathsHelper.isVisible(obj.getHeight(), obj.getPosition(), sourceEntity.getPosition());
            } else if (sourceEntity instanceof Satellite && obj instanceof Device) {    // Check if sourceEntity is of Satellite type and obj is of Device
                distance = MathsHelper.getDistance(sourceEntity.getHeight(), sourceEntity.getPosition(), obj.getPosition());
                isVisible = MathsHelper.isVisible(sourceEntity.getHeight(), sourceEntity.getPosition(), obj.getPosition());
            } else {                                                                    // Check if sourceEntity is of Satellite type and obj is of Satellite
                distance = MathsHelper.getDistance(sourceEntity.getHeight(), sourceEntity.getPosition(), obj.getHeight(), obj.getPosition());
                isVisible = MathsHelper.isVisible(sourceEntity.getHeight(), sourceEntity.getPosition(), obj.getHeight(), obj.getPosition());
            }
            
            /**
             * Check if the distance between obj and sourceEntity is less than sourceEntity's max range,
             * obj is visible to sourceEntity, obj != sourceEntity, and the obj hasn't been visited 
             * before (prevention for infinite loop within recursion)
             */ 
            if (distance <= sourceEntity.getMaxRange() && isVisible && !obj.getId().equals(id) && obj.getVisited() == false) {
                returnList.add(obj.getId());
                obj.setVisited(true);
                if (obj.getType().equals("RelaySatellite")) {               // If the obj is a relay satellite
                    List<String> relayList = entityInRange(obj.getId());    // Recursively look through the entity list for entities that can be linked to the sourceEntity via relay satellite link
                    for (String relayListId : relayList) {
                        if (!returnList.contains(relayListId)) {            // Prevention for duplication within list
                            returnList.add(relayListId);                    //
                        }
                    }
                }
            }
        } 
        return returnList;
    }

    /**
     * Method that throws exceptions regarding file transfer. If all conditions are met and no exceptions are thrown, 
     * add file entity with empty content to begin file transfer in Simulate method.
     * @param fileName
     * @param fromId
     * @param toId
     * @throws FileTransferException
     */
    public void sendFile(String fileName, String fromId, String toId) throws FileTransferException {
        // TODO Task 2c)
        
        // Check if file name exists at all
        if (!File.fileExists(fileName, files)) {
            throw new VirtualFileNotFoundException(fileName);
        }

        Entity fromEntity = Entity.retrieveEntity(fromId, entities);
        Entity toEntity = Entity.retrieveEntity(toId, entities);
        
        File fileToSend = File.retrieveFile(fileName, files);
        File newFile = null;
        newFile = new File(fileToSend.getFilename(), "", fileToSend.getFileSize(), false, fromEntity);
    
        
        EntityInfoResponse fromEntityResponse = getInfo(fromId);
        EntityInfoResponse toEntityResponse = getInfo(toId);


        // If entity with ID fromId does not contain the file
        if (!fromEntityResponse.getFiles().containsKey(fileName)){
            throw new VirtualFileNotFoundException(fileName);
        }

        // The file hasn't been completely transfered
        if (fromEntityResponse.getFiles().get(fileName) != null) {
            if (!fromEntityResponse.getFiles().get(fileName).hasTransferCompleted()) {
                throw new VirtualFileNotFoundException(fileName);
            }
        }

        // If entity with ID toId already contains the file
        if (toEntityResponse.getFiles().containsKey(fileName)) {
            throw new VirtualFileAlreadyExistsException(fileName);
        }

        // The file is already being transfered
        if (toEntityResponse.getFiles().get(fileName) != null) {
            if (!toEntityResponse.getFiles().get(fileName).hasTransferCompleted()) {
                throw new VirtualFileAlreadyExistsException(fileName);
            }
        }

        // Check output bandwith of Satellite
        if (fromEntity instanceof Satellite) {
            Satellite fromSatellite = (Satellite) fromEntity;
            if (fromSatellite.outboundingFiles(entities) == fromSatellite.getOutRate()) {
                throw new VirtualFileNoBandwidthException(fromId);
            }
        }

        // Exception handling for entity if we are sending to Satellite
        if (toEntity instanceof Satellite) {
            Satellite toSatellite = (Satellite) toEntity;

            // Satellite max capacity in byte condition
            if (toSatellite.totalBytesStored() + fileToSend.getFileSize() > toSatellite.getMaxBytes()) {
                throw new VirtualFileNoStorageSpaceException(toId);
            }

            // Satellite download bandwith condition
            if (toSatellite.inboundFiles(entities) == toSatellite.getInRate()) {
                throw new VirtualFileNoBandwidthException(toId);
            }

            // If satellite is of type StandardSatellite, check its max file condition
            if (toSatellite instanceof StandardSatellite) {
                StandardSatellite staToSatellite = (StandardSatellite) toSatellite;
                if (staToSatellite.getMaxFiles() == staToSatellite.numberofFiles()) {
                    throw new VirtualFileNoStorageSpaceException(toId);
                }
            }
        }

        // If all conditions are met, add file stub to entity
        toEntity.addFile(newFile);
    }

}