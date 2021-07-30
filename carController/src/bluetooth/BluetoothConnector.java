package bluetooth;

import java.io.IOException;

import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommLogListener;
import lejos.pc.comm.NXTCommandConnector;
import lejos.pc.comm.NXTConnector;

/**
 * 
 * @author Paula Muñoz - University of Malaga
 * 
 */
public class BluetoothConnector {

	NXTConnector conn;

	public BluetoothConnector() {
		this.conn = new NXTConnector();
	}

	public void openConnection() {
		this.conn.addLogListener(new NXTCommLogListener() {
			@Override
			public void logEvent(String message) {
				System.out.println(message);
			}

			@Override
			public void logEvent(Throwable throwable) {
				System.err.println(throwable.getMessage());
			}
		});

		this.conn.setDebug(true);
		if (!conn.connectTo("btspp://NXT", NXTComm.LCP)) {
			System.err.println("Failed to connect");
			System.exit(1);
		}

		NXTCommandConnector.setNXTCommand(new NXTCommand(conn.getNXTComm()));
	}

	public void closeConnection() {
		try {
			this.conn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
