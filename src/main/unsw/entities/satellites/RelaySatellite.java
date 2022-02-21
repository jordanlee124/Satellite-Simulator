package unsw.entities.satellites;

import unsw.entities.Satellite;
import unsw.utils.Angle;

/**Represents a Relay Satellite which is a subclass of Satellite
 * @author z5312190
 */

public class RelaySatellite extends Satellite {

    private final double maxPosition = 190;
    private final double minPosition = 140;
    private boolean reverse = false;

    /**
     * Constructor for RelaySatellite
     * @param satelliteId
     * @param type
     * @param height
     * @param position
     */
    public RelaySatellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, type, height, position);
    }

    /**
     * Changes the Relay Satellite's position according to the uniqiue movement characteristics of relay satellites
     * @param angVelocity
     */
    @Override
    public void changePosition(Angle angVelocity) { 
        double positionChange = angVelocity.toDegrees();                        // The amount the satellite needs to move
        double currPosition = this.getPosition().toDegrees();                   // The satellite's current position
        if (reverse == true || (currPosition <= 345 && currPosition >= 190)) {  // Check if the satellite should be moving clockwise or anti-clockwise
            positionChange = -positionChange;
        }
        double toPosition = positionChange + currPosition;                      // The position of the satellite after the change in position
        if (currPosition > maxPosition || currPosition < minPosition) {         // Check if the satellite is currently not in between 140 and 190 degrees.
            setPosition(Angle.fromDegrees(toPosition % 360));
        } else {                                                                // If the satellite is in between 140 and 190 degrees
            if (toPosition > maxPosition) {                                     // Check if the toPosition is past the maxPosition
                reverse = true;                                                 // Change the satellite's direction to clockwise
                setPosition(Angle.fromDegrees(toPosition));
            } else if (toPosition < minPosition) {                              // Check if the toPosition is past the minPosition
                reverse = false;                                                // Change the satellite's direction to anti-clockwise
                setPosition(Angle.fromDegrees(toPosition));
            } else {                                                            // If the toPosition is within bounds of 140 and 190 degrees
                setPosition(Angle.fromDegrees(currPosition + positionChange));
            }
        }
    }     
}
