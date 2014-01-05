package de.locked.weatherstation.tinkerforge;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * IPConnection#setAutoReconnect(). Aktiviert oder deaktiviert die automatische Wiederverbindung. Falls die
 * Wiederverbindung aktiviert ist, versucht die IP Connection eine Verbindung zum vorher angegebenen Host und Port
 * wieder herzustellen, falls die Verbindung verloren geht.
 *
 * Und wenn die Verbindung gar nicht zusatnde kommt bleibt sie zu?
 */
public class Connector extends Thread {

    private static final Logger log = Logger.getLogger(Connector.class.getName());
    private final IPConnection ipcon;
    private final String host;
    private final int port;

    public Connector(IPConnection ipcon, String host, int port) {
        setDaemon(true);
        this.ipcon = ipcon;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        if (ipcon.getConnectionState() == IPConnection.CONNECTION_STATE_PENDING) {
            log.info("Connection is pending.");
        } else if (ipcon.getConnectionState() == IPConnection.CONNECTION_STATE_CONNECTED) {
            log.info("Connection is up.");
        } else {
            log.info("Connection down, connecting");
            try {
                ipcon.connect(host, port);
                log.info("Connecting succeededc");
            } catch (IOException | AlreadyConnectedException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
