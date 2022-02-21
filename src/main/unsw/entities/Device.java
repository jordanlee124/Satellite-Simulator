package unsw.entities;

import unsw.utils.Angle;
import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

/** Represents a Device which is a subclass of Entity
 * @author z5312190
 */

public class Device extends Entity {

    /**
     * Constructor for Device
     * @param deviceId
     * @param type
     * @param position
     */
    public Device(String deviceId, String type, Angle position) {
        super(deviceId, type, position, RADIUS_OF_JUPITER);
    }
    
}