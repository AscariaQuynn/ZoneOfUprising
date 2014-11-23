/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import cz.ascaria.network.ClientManager;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.central.Main;
import cz.ascaria.network.central.messages.SelectEntityMessage;
import cz.ascaria.network.central.messages.UserFleetMessage;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.zoneofuprising.entities.ClientEntitiesManager;
import cz.ascaria.zoneofuprising.utils.AssetsHelper;
import cz.ascaria.zoneofuprising.world.ClientWorldManager;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.buttons.RadioButtonGroup;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.controls.windows.Window;

/**
 *
 * @author Ascaria Quynn
 */
public class MyProfileLayout extends BaseLayout {

    private ClientManager clientManager;
    private ClientWorldManager worldManager;
    private ClientEntitiesManager entitiesManager;
    private InputManager inputManager;

    private ClientWrapper centralClient;
    private MyProfileCentralClientListener myProfileCentralClientListener;

    private ArrayList<EntityProfile> fleet = new ArrayList<EntityProfile>();

    private Panel panel;
    private Window shipWindow;

    @Override
    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        super.initialize(stateManager, app);

        clientManager = ((ZoneOfUprising)app).getClientManager();
        worldManager = (ClientWorldManager)app.getWorldManager();
        entitiesManager = (ClientEntitiesManager)app.getEntitiesManager();
        inputManager = app.getInputManager();

        centralClient = app.getCentralClient();
        myProfileCentralClientListener = new MyProfileCentralClientListener();
        myProfileCentralClientListener.initialize(this, app);
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

        // TODO: refactor
        inputManager.setCursorVisible(true);

        // Create panel
        panel = new Panel(screen, Vector2f.ZERO, dimensions, Vector4f.ZERO, null);
        panel.setIsMovable(false);
        panel.setIsResizable(false);
        panel.setIgnoreMouse(true);

        // Create logo
        panel.addChild(getLogo());

        // Create nav buttons
        createNavButtons();

        // Create ship customizing window
        createShipWindow();

        // Add panel to screen
        screen.addElement(panel);
    }

    @Override
    public void close() {
        super.close();

        if(null != panel) {
            panel.hide();
            screen.removeElement(panel);
            panel = null;
            shipWindow = null;
        }
    }

    private void createNavButtons() {
        // Logout button
        Vector2f logoutPos = AssetsHelper.scaleOf(dimensions, new Vector2f(0.95f, 0.95f)).subtractLocal(216f, 80f);
        ButtonAdapter bLogout = new ButtonAdapter(screen, logoutPos, new Vector2f(216f, 80f), new Vector4f(0f, 100f, 100f, 0f), "Interface/bg-button2.png") {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                clientManager.logout();
            }
        };
        bLogout.setButtonHoverInfo("Interface/bg-button2-hover.png", ColorRGBA.White);
        bLogout.setButtonPressedInfo("Interface/bg-button2-hover.png", ColorRGBA.LightGray);
        bLogout.setTextAlign(BitmapFont.Align.Left);
        bLogout.setTextPadding(105f, 0f, 0f, 0f);
        bLogout.setText("Logout");
        panel.addChild(bLogout);

        // Settings button
        Vector2f settingsPos = AssetsHelper.scaleOf(dimensions, new Vector2f(0.95f, 0.95f)).subtractLocal(216f * 2.2f, 80f);
        ButtonAdapter bSettings = new ButtonAdapter(screen, settingsPos, new Vector2f(216f, 80f), new Vector4f(0f, 100f, 100f, 0f), "Interface/bg-button2.png") {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                guiManager.show(SettingsLayout.class);
            }
        };
        bSettings.setButtonHoverInfo("Interface/bg-button2-hover.png", ColorRGBA.White);
        bSettings.setButtonPressedInfo("Interface/bg-button2-hover.png", ColorRGBA.LightGray);
        bSettings.setTextAlign(BitmapFont.Align.Left);
        bSettings.setTextPadding(105f, 0f, 0f, 0f);
        bSettings.setText("Settings");
        panel.addChild(bSettings);

        // Server list button
        Vector2f serverListPos = AssetsHelper.scaleOf(dimensions, new Vector2f(0.95f, 0.95f)).subtractLocal(216f * 3.4f, 80f);
        ButtonAdapter bServerList = new ButtonAdapter(screen, serverListPos, new Vector2f(216f, 80f), new Vector4f(0f, 100f, 100f, 0f), "Interface/bg-button2.png") {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                guiManager.show(ServerListLayout.class);
            }
        };
        bServerList.setButtonHoverInfo("Interface/bg-button2-hover.png", ColorRGBA.White);
        bServerList.setButtonPressedInfo("Interface/bg-button2-hover.png", ColorRGBA.LightGray);
        bServerList.setTextAlign(BitmapFont.Align.Left);
        bServerList.setTextPadding(105f, 0f, 0f, 0f);
        bServerList.setText("Server list");
        panel.addChild(bServerList);
    }

    /**
     * Creates ship window with user's fleet.
     */
    private void createShipWindow() {
        if(null != shipWindow) {
            panel.removeChild(shipWindow);
            shipWindow = null;
        }

        // Create Customize Ship window
        shipWindow = new Window(screen, new Vector2f(40f, 160f), new Vector2f(350f, 500f));
        shipWindow.setWindowTitle("Customize ship");
        shipWindow.setWindowIsMovable(true);
        shipWindow.setIsResizable(false);
        shipWindow.setGlobalAlpha(0.5f);


        // Create button for every ship in fleet
        RadioButtonGroup rbg = new RadioButtonGroup(screen) {
            @Override
            public void onSelect(int index, Button value) {
                try {
                    // Send central server message that user wants to select entity
                    EntityProfile entityProfile = fleet.get(index);
                    if(entitiesManager.entityExist(entityProfile)) {
                        centralClient.send(new SelectEntityMessage(clientManager.getUserProfile(), entityProfile));
                    }
                } catch(Exception ex) {
                    Main.LOG.log(Level.SEVERE, null, ex);
                }
            }
        };
        int marginTop = 15;
        for(final EntityProfile entityProfile : fleet) {
            // Create button for entity
            ButtonAdapter button = new ButtonAdapter(screen, new Vector2f(15f, marginTop), new Vector2f(170f, 30f));
            button.setText(entityProfile.getName());
            if(entityProfile.isSelected()) {
                button.setIsToggledNoCallback(true);
            }
            rbg.addButton(button);
            shipWindow.addWindowContent(button);

            marginTop += 40;
        }

        panel.addChild(shipWindow);
    }


    public void setFleet(ArrayList<EntityProfile> fleet) {
        this.fleet.clear();
        this.fleet.addAll(fleet);
        if(isOpened()) {
            createShipWindow();
        }
    }



    private class MyProfileCentralClientListener implements MessageListener<Client> {

        private ClientWrapper centralClient;

        public void initialize(MyProfileLayout myProfileLayout, ZoneOfUprising app) {
            this.centralClient = app.getCentralClient();
            centralClient.addMessageListener(this,
                UserFleetMessage.class
            );
        }

        public void cleanup() {
            centralClient.removeMessageListener(this);
        }

        /**
         * Forward received messages.
         * @param source
         * @param m
         */
        public void messageReceived(Client source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof UserFleetMessage) {
                        userFleetMessage((UserFleetMessage)m);
                    }
                    return null; 
                }
            });
        }

        /**
         * Generate fleet.
         * @param fleet
         */
        public void userFleetMessage(UserFleetMessage m) {
            if(null != m.fleet) {
                fleet.clear();
                fleet.addAll(m.fleet);
                if(isOpened()) {
                    createShipWindow();
                }
            } else {
                guiManager.showAlert("User Fleet Profile error", m.error);
            }
        }
    }
}
