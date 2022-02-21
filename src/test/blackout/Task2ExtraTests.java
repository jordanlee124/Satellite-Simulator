package blackout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import unsw.blackout.BlackoutController;
import unsw.blackout.FileTransferException;
import unsw.response.models.FileInfoResponse;
import unsw.response.models.EntityInfoResponse;
import unsw.utils.Angle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

import java.util.Arrays;

import static blackout.TestHelpers.assertListAreEqualIgnoringOrder;

@TestInstance(value = Lifecycle.PER_CLASS)
public class Task2ExtraTests {
    @Test
    public void testRelayMovement() {
        // Task 2
        // Example from the specification
        BlackoutController controller = new BlackoutController();

        // Creates 1 satellite and 2 devices
        // Gets a device to send a file to a satellites and gets another device to download it.
        // StandardSatellites are slow and transfer 1 byte per minute.
        controller.createSatellite("Satellite1", "RelaySatellite", 100 + RADIUS_OF_JUPITER, Angle.fromDegrees(180));

        // moves in positive direction
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(180), 100 + RADIUS_OF_JUPITER, "RelaySatellite"), controller.getInfo("Satellite1"));
        controller.simulate();
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(181.23), 100 + RADIUS_OF_JUPITER, "RelaySatellite"), controller.getInfo("Satellite1"));
        controller.simulate();
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(182.46), 100 + RADIUS_OF_JUPITER, "RelaySatellite"), controller.getInfo("Satellite1"));
        controller.simulate();
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(183.69), 100 + RADIUS_OF_JUPITER, "RelaySatellite"), controller.getInfo("Satellite1"));
        
        // edge case
        controller.simulate(5);
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(189.82), 100 + RADIUS_OF_JUPITER, "RelaySatellite"), controller.getInfo("Satellite1"));
        controller.simulate(1);
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(191.05), 100 + RADIUS_OF_JUPITER, "RelaySatellite"), controller.getInfo("Satellite1"));
        
        // goes back down
        controller.simulate(1);
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(189.82), 100 + RADIUS_OF_JUPITER, "RelaySatellite"), controller.getInfo("Satellite1"));
        controller.simulate(5);
        assertEquals(new EntityInfoResponse("Satellite1", Angle.fromDegrees(183.69), 100 + RADIUS_OF_JUPITER, "RelaySatellite"), controller.getInfo("Satellite1"));
    }

    @Test
    public void testQuantumBehaviour() {
        // just some of them... you'll have to test the rest
        BlackoutController controller = new BlackoutController();

        // Creates 1 satellite and 2 devices
        controller.createSatellite("Satellite1", "ShrinkingSatellite", 1000 + RADIUS_OF_JUPITER, Angle.fromDegrees(320));
        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(320));
        controller.createDevice("DeviceB", "LaptopDevice", Angle.fromDegrees(315));

        // uploads at a rate of 15 per minute so we'll give it 21 bytes which when compressed is 14
        String msg = "hello quantum how are";
        controller.addFileToDevice("DeviceA", "FileAlpha", msg);
        assertDoesNotThrow(() -> controller.sendFile("FileAlpha", "DeviceA", "Satellite1"));
        assertEquals(new FileInfoResponse("FileAlpha", "", msg.length(), false), controller.getInfo("Satellite1").getFiles().get("FileAlpha"));

        // we still should have 6 bytes to send
        controller.simulate(1);
        assertEquals(new FileInfoResponse("FileAlpha", "hello quantum h", msg.length(), false), controller.getInfo("Satellite1").getFiles().get("FileAlpha"));

        // now that we are done we should see shrinkage
        controller.simulate(1);
        assertEquals(new FileInfoResponse("FileAlpha", msg, 14, true), controller.getInfo("Satellite1").getFiles().get("FileAlpha"));

        // sending file back down to other device it needs to send full 21 bytes, bandwidth out is 10 so it should take 3 ticks
        assertDoesNotThrow(() -> controller.sendFile("FileAlpha", "Satellite1", "DeviceB"));
        assertEquals(new FileInfoResponse("FileAlpha", "", msg.length(), false), controller.getInfo("DeviceB").getFiles().get("FileAlpha"));

        // we still should have 11 bytes to send
        controller.simulate(1);
        assertEquals(new FileInfoResponse("FileAlpha", "hello quan", msg.length(), false), controller.getInfo("DeviceB").getFiles().get("FileAlpha"));

        // and still 1 more byte to send
        controller.simulate(1);
        assertEquals(new FileInfoResponse("FileAlpha", "hello quantum how ar", msg.length(), false), controller.getInfo("DeviceB").getFiles().get("FileAlpha"));

        // done! and file size should not be shrunk, we aren't on the shrinking satellite
        controller.simulate(1);
        assertEquals(new FileInfoResponse("FileAlpha", "hello quantum how are", msg.length(), true), controller.getInfo("DeviceB").getFiles().get("FileAlpha"));
    }

    //=============================== My Test ===============================//

    @Test
    public void testRelayLink() {

        // Test multiple relay link
        BlackoutController controller = new BlackoutController();

        // Create 1 device and 4 relay satellites
        // Distance the relays to be in range of each other and check if the device can connect to all the relays .
        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(90));
        controller.createSatellite("Relay1", "RelaySatellite", 10000 + RADIUS_OF_JUPITER, Angle.fromDegrees(100));
        controller.createSatellite("Relay2", "RelaySatellite", 10000 + RADIUS_OF_JUPITER, Angle.fromDegrees(115));
        controller.createSatellite("Relay3", "RelaySatellite", 10000 + RADIUS_OF_JUPITER, Angle.fromDegrees(130));
        controller.createSatellite("Relay4", "RelaySatellite", 10000 + RADIUS_OF_JUPITER, Angle.fromDegrees(145));
        
        assertListAreEqualIgnoringOrder(Arrays.asList("Relay1","Relay2","Relay3","Relay4"), controller.communicableEntitiesInRange("DeviceA"));
    }

    @Test
    public void testSatelliteOutOfRange() {

        //Test file is removed if satellite goes out of range of device while file is being transfered
        BlackoutController controller = new BlackoutController();

        // Create 1 device and 1 satellite
        controller.createDevice("DeviceA", "HandheldDevice", Angle.fromDegrees(89));
        controller.createSatellite("Satellite1", "StandardSatellite", 18562 + RADIUS_OF_JUPITER, Angle.fromDegrees(116));

        String msg = "Hey Bro!";
        controller.addFileToDevice("DeviceA", "FileAlpha", msg);
        assertDoesNotThrow(() -> controller.sendFile("FileAlpha", "DeviceA", "Satellite1"));
        assertEquals(new FileInfoResponse("FileAlpha", "", msg.length(), false), controller.getInfo("Satellite1").getFiles().get("FileAlpha"));

        controller.simulate(msg.length());
        assertEquals(false, controller.getInfo("Satellite1").getFiles().containsKey("FileAlpha"));
    }

    @Test
    public void testDesktopToStandardConnection() {
        // Check if Satellite can communicate with Desktop Device
        BlackoutController controller = new BlackoutController();
        controller.createDevice("DesktopA", "DesktopDevice", Angle.fromDegrees(88));
        controller.createSatellite("Satellite1", "StandardSatellite", 10000 + RADIUS_OF_JUPITER, Angle.fromDegrees(100));

        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("DesktopA"));
        assertListAreEqualIgnoringOrder(Arrays.asList(), controller.communicableEntitiesInRange("Satellite1"));
    }

    @Test
    public void deviceSendsMultipleFilesToShrinkingSatellite() {
        //Check if one device can send multiple files and if Shrinking Satellite can receive multiple files
        BlackoutController controller = new BlackoutController();
        controller.createDevice("DesktopA", "DesktopDevice", Angle.fromDegrees(100));
        controller.createSatellite("Satellite1", "ShrinkingSatellite", 10000 + RADIUS_OF_JUPITER, Angle.fromDegrees(100));

        String msg1 = "123456789";
        String msg2 = "234567891";
        String msg3 = "345678912";
        String msg4 = "456789123";
        String msg5 = "567891234";

        controller.addFileToDevice("DesktopA", "File1", msg1);
        controller.addFileToDevice("DesktopA", "File2", msg2);
        controller.addFileToDevice("DesktopA", "File3", msg3);
        controller.addFileToDevice("DesktopA", "File4", msg4);
        controller.addFileToDevice("DesktopA", "File5", msg5);

        assertDoesNotThrow(() -> controller.sendFile("File1", "DesktopA", "Satellite1"));
        assertDoesNotThrow(() -> controller.sendFile("File2", "DesktopA", "Satellite1"));
        assertDoesNotThrow(() -> controller.sendFile("File3", "DesktopA", "Satellite1"));
        assertDoesNotThrow(() -> controller.sendFile("File4", "DesktopA", "Satellite1"));
        assertDoesNotThrow(() -> controller.sendFile("File5", "DesktopA", "Satellite1"));

        controller.simulate();

        assertEquals(new FileInfoResponse("File1", "123", msg1.length(), false), controller.getInfo("Satellite1").getFiles().get("File1"));
        assertEquals(new FileInfoResponse("File2", "234", msg2.length(), false), controller.getInfo("Satellite1").getFiles().get("File2"));
        assertEquals(new FileInfoResponse("File3", "345", msg3.length(), false), controller.getInfo("Satellite1").getFiles().get("File3"));
        assertEquals(new FileInfoResponse("File4", "456", msg4.length(), false), controller.getInfo("Satellite1").getFiles().get("File4"));
        assertEquals(new FileInfoResponse("File5", "567", msg5.length(), false), controller.getInfo("Satellite1").getFiles().get("File5"));

        controller.simulate(2);

        assertEquals(new FileInfoResponse("File1", msg1, msg1.length(), true), controller.getInfo("Satellite1").getFiles().get("File1"));
        assertEquals(new FileInfoResponse("File2", msg2, msg2.length(), true), controller.getInfo("Satellite1").getFiles().get("File2"));
        assertEquals(new FileInfoResponse("File3", msg3, msg3.length(), true), controller.getInfo("Satellite1").getFiles().get("File3"));
        assertEquals(new FileInfoResponse("File4", msg4, msg4.length(), true), controller.getInfo("Satellite1").getFiles().get("File4"));
        assertEquals(new FileInfoResponse("File5", msg5, msg5.length(), true), controller.getInfo("Satellite1").getFiles().get("File5"));
    }
}
