/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import cz.ascaria.network.ClientManager;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.logging.Level;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.lists.SelectList;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.controls.windows.Window;

/**
 *
 * @author Ascaria Quynn
 */
public class ServerListLayout extends BaseLayout {

    private ClientManager clientManager;

    private JmDNS jmdnsService;

    private Panel panel;

    @Override
    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        super.initialize(stateManager, app);

        clientManager = app.getClientManager();
    }

    

    public boolean isOpened() {
        return null != panel;
    }

    @Override
    public void open() {
        super.open();
        check();
        if(isOpened()) {
            close();
        }

        // Create panel
        panel = new Panel(screen, Vector2f.ZERO, dimensions, Vector4f.ZERO, null);
        panel.setIsResizable(false);
        panel.setIgnoreMouse(true);

        // Create logo
        panel.addChild(getLogo());

        // Create window
        Window win = new Window(screen, Vector2f.ZERO, new Vector2f(430f, 480f));
        win.setWindowTitle("Zone of Uprising Server List");
        win.setWindowIsMovable(true);
        win.setIsResizable(false);
        win.setGlobalAlpha(0.5f);

        // Server selection
        final SelectList selectList = new SelectList(screen, new Vector2f(15f, 50f), new Vector2f(400f, 300f)) {
            @Override
            public void onChange() {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        selectList.addListItem("localhost", "localhost");
        for(int i = 110; i <= 112; i++) {
            selectList.addListItem("192.168.100." + i, "192.168.100." + i);
        }
        selectList.addSelectedIndex(0);
        win.addWindowContent(selectList);        

        // Join server button
        ButtonAdapter joinServer = new ButtonAdapter(screen, new Vector2f(50f, 400f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                // Get vars
                List<SelectList.ListItem> items = selectList.getSelectedListItems();
                // Connect to game server
                if(!items.isEmpty()) {
                    try {
                        String host = (String)items.get(0).getValue();
                        clientManager.connectGameClient(host);
                    } catch(Exception ex) {
                        Main.LOG.log(Level.SEVERE, null, ex);
                        guiManager.showAlert("Connect Game Server error", ex.getLocalizedMessage());
                    }
                } else {
                    guiManager.showAlert("Connect Game Server error", "No host server was selected.");
                }
            }
        };
        joinServer.setText("Join Server");
        win.addWindowContent(joinServer);

        // Add server button
        ButtonAdapter addServer = new ButtonAdapter(screen, new Vector2f(170f, 400f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                AddServerLayout asl = guiManager.getLayout(AddServerLayout.class);
                asl.selectList = selectList;
                asl.open();
            }
        };
        addServer.setText("Add Server");
        win.addWindowContent(addServer);

        // Find server button
        ButtonAdapter findServer = new ButtonAdapter(screen, new Vector2f(290f, 360f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                findServer();
            }
        };
        findServer.setText("Find Server");
        win.addWindowContent(findServer);

        // Back button
        ButtonAdapter back = new ButtonAdapter(screen, new Vector2f(290f, 400f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                guiManager.show(MyProfileLayout.class);
            }
        };
        back.setText("Back");
        win.addWindowContent(back);

        panel.addChild(win);
        win.centerToParent();

        screen.addElement(panel);
    }

    @Override
    public void close() {
        super.close();
        if(null != panel) {
            panel.hide();
            screen.removeElement(panel);
            panel = null;
        }
    }

    private void findServer() {
        System.out.println("Finding server.");


        ServiceListener jmdnsServiceListener = new ServiceListener() {
            public void serviceAdded(ServiceEvent serviceEvent) {
                // Test service is discovered. requestServiceInfo() will trigger serviceResolved() callback.
                jmdnsService.requestServiceInfo("my-service-type", serviceEvent.getName());
                System.out.println("serviceAdded");
            }

            public void serviceRemoved(ServiceEvent serviceEvent) {
                // Test service is disappeared.
            }

            public void serviceResolved(ServiceEvent serviceEvent) {
                // Test service info is resolved.
                String serviceUrl = serviceEvent.getInfo().getURL();
                // serviceURL is usually something like http://192.168.11.2:6666/my-service-name
            }
        };
        try {
            String type = "_http._tcp.local.";

            jmdnsService = JmDNS.create(InetAddress.getLocalHost());
            jmdnsService.addServiceListener(type, jmdnsServiceListener);
            ServiceInfo[] infos = jmdnsService.list(type);
            jmdnsService.printServices();
            System.out.println("found " + infos.length + " services");
            // Retrieve service info from either ServiceInfo[] returned here or listener callback method above.
            jmdnsService.removeServiceListener(type, jmdnsServiceListener);
            jmdnsService.close();
        } catch (IOException ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
        }

    }
}
