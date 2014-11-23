package cz.ascaria.zoneofuprising;

import com.jme3.system.JmeContext;
import cz.ascaria.zoneofuprising.utils.SettingsLoader;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Zone of Uprising.
 * @author Jaroslav Ascaria Svoboda
 */
public class Main {

    /**
     * The type of context.
     */
    public enum Mode {
        Client,
        Server;
    }

    /**
     * Logger for whole app.
     */
    final public static Logger LOG = Logger.getLogger("cz.ascaria.zoneofuprising");

    /**
     * Runs Zone of Uprising.
     * @param args 
     */
    public static void main(String[] args) {

        // Client mode switch (Default)
        Mode mode = Mode.Client;

        // Game Server mode switch
        if(args.length > 0 && args[0].equals("-server")) {
            mode = Mode.Server;
        }

        switch(mode) {
            case Client:
                logToFile("client");
                runGame(false, JmeContext.Type.Display);
                break;
            case Server:
                logToFile("server");
                JmeContext.Type contextType = args.length > 1 && args[1].equals("-withhead") ? JmeContext.Type.Display : JmeContext.Type.Headless;
                runGame(true, contextType);
                break;
        }
    }

    private static void logToFile(String filePart) {
        try {  
            // Save logs to file, this block configure the logger with handler and formatter
            FileHandler fh = new FileHandler(System.getProperty("user.dir") + "/zou-" + filePart + "-" + System.getProperty("user.name") + ".log");  
            LOG.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());  
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        } 
    }

    /**
     * Run game
     * @param isServer
     * @param contextType
     */
    private static void runGame(boolean isServer, JmeContext.Type contextType) {
        // Run client or server
        ZoneOfUprising app = new ZoneOfUprising();
        app.setIsServer(isServer);
        app.setSettings(new SettingsLoader().load());
        app.setPauseOnLostFocus(false);
        app.start(contextType);
    }
}
