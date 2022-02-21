package unsw.entities.satellites;

import unsw.entities.Satellite;
import unsw.utils.Angle;

/** Represents a Standard Satellite which is a subclass of Satellite
 * @author z5312190
 */

public class StandardSatellite extends Satellite {
    
    private final int maxFiles = 3;

    /**
     * Constructor for StandardSatellite 
     * @param satelliteId
     * @param type
     * @param height
     * @param position
     */

    public StandardSatellite(String satelliteId, double height, Angle position) {
        super(satelliteId, "StandardSatellite", height, position);
    }

    /////////////    Getters And Setters    /////////////

    public int getMaxFiles() {
        return this.maxFiles;
    }

}
