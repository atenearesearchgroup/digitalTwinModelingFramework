package bluetooth;

import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTCommandConnector;
import lejos.pc.comm.NXTConnector;

import java.io.IOException;

/**
 * @author Paula Mu&ntilde;oz - University of M&atilde;laga
 */
public class BluetoothConnector {

    private final NXTConnector connector;

    /**
     * Default constructor
     */
    public BluetoothConnector() {
        this.connector = new NXTConnector();
    }


    /**
     * It starts the bluetooth connection between the Lego Car and the computer
     */
    public void openConnection() {
        this.connector.addLogListener(new NXTCommLogListener() {
            @Override
            public void logEvent(String message) {
                System.out.println(message);
            }

            @Override
            public void logEvent(Throwable throwable) {
                System.err.println(throwable.getMessage());
            }
        });

        this.connector.setDebug(true);
        if (!connector.connectTo("btspp://NXT", NXTComm.LCP)) {
            System.err.println("Failed to connect");
            System.exit(1);
        }

        NXTCommandConnector.setNXTCommand(new NXTCommand(connector.getNXTComm()));
    }

    /**
     * It closes the bluetooth connection between the Lego Car and the computer
     */
    public void closeConnection() {
        try {
            this.connector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
