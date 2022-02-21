package unsw.entities.satellites;

import unsw.entities.Satellite;
import unsw.utils.Angle;

/** Represents a Shrinking Satellite which is a subclass of Satellite
 * @author z5312190
 */

public class ShrinkingSatellite extends Satellite {

    /**
     * Constructor for ShrinkingSatellite 
     * @param satelliteId
     * @param type
     * @param height
     * @param position
     */

    public ShrinkingSatellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, type, height, position);
    }

}
