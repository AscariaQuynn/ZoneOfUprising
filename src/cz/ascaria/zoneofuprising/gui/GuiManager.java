/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Node;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.central.messages.AlertMessage;
import cz.ascaria.network.central.messages.UserFleetMessage;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.audio.AudioManager;
import cz.ascaria.zoneofuprising.audio.VolumeChangedListener;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.zoneofuprising.entities.ClientEntitiesManager;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.entities.EntityEventsAdapter;
import cz.ascaria.zoneofuprising.gui.custom.DefaultAlertBox;
import cz.ascaria.zoneofuprising.guns.GunManagerControl;
import cz.ascaria.zoneofuprising.input.GuiManagerInputListener;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import cz.ascaria.zoneofuprising.world.ClientWorldManager;
import cz.ascaria.zoneofuprising.world.WorldEventsAdapter;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import tonegod.gui.controls.windows.AlertBox;
import tonegod.gui.core.Screen;

/**
 *
 * @author Ascaria Quynn
 */
public class GuiManager {

    private Node guiNode;

    public Screen screen;

    private ZoneOfUprising app;
    private AppStateManager stateManager;
    private ClientWorldManager worldManager;
    private ClientEntitiesManager entitiesManager;

    private GuiManagerInputListener guiListener;
    private AudioListener audioListener;
    private CentralClientListener centralClientListener;
    private WorldEventsListener worldEventsListener;
    private EntityEventsListener entityEventsListener;

    private HashMap<Class<?extends Layout>, Layout> layouts = new HashMap<Class<?extends Layout>, Layout>();
    private Layout shown;

    private DefaultAlertBox alertBox;

    public GuiManager(Node guiNode) {
        this.guiNode = guiNode;
    }

    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        this.app = app;
        this.stateManager = stateManager;
        this.worldManager = (ClientWorldManager)app.getWorldManager();
        this.entitiesManager = (ClientEntitiesManager)app.getEntitiesManager();
        if(null == this.worldManager) {
            throw new IllegalStateException("World Manager not found.");
        }

        screen = new Screen(app);
        screen.setUseUIAudio(true);
        screen.setUIAudioVolume(app.getAudioManager().getGuiVolume());
        guiNode.addControl(screen);

        audioListener = new AudioListener();
        audioListener.initialize(this, app);

        centralClientListener = new CentralClientListener();
        centralClientListener.initialize(this, app);

        worldEventsListener = new WorldEventsListener();
        worldEventsListener.initialize(this, app);
        worldManager.addWorldEventsListener(worldEventsListener);

        entityEventsListener = new EntityEventsListener();
        entityEventsListener.initialize(this, app);
        entitiesManager.addEntityEventsListener(entityEventsListener);

        guiListener = new GuiManagerInputListener(this, app);
        guiListener.registerInputs();
    }
    
    public void cleanup() {
        shown = null;

        guiListener.clearInputs();
        entitiesManager.removeEntityEventsListener(entityEventsListener);
        worldManager.removeWorldEventsListener(worldEventsListener);

        centralClientListener.cleanup();
        audioListener.cleanup();

        guiNode.removeControl(screen);
    }

    /**
     * Is given layout actually shown?
     * @param layout
     * @return
     */
    public boolean isShown(Class<?extends Layout> layout) {
        return null != shown && layout.isAssignableFrom(shown.getClass());
    }

    /**
     * Show given layout, hide others.
     * @param layout
     * @return true if layout was shown, false on fail
     */
    public boolean show(Class<?extends Layout> layout) {
        // Create layout
        if(!layouts.containsKey(layout)) {
            initLayout(layout);
        }
        // Destroy all layouts
        for(Layout l : layouts.values()) {
            if(l.isOpened()) {
                l.close();
            }
        }
        boolean wasShown = false;
        // Show specified layout
        for(Class<?extends Layout> key : layouts.keySet()) {
            if(layout.equals(key)) {
                shown = layouts.get(key);
                shown.open();
                wasShown = true;
                break;
            }
        }
        // Refresh alert window
        if(null != alertBox && alertBox.getIsVisible()) {
            alertBox.hide();
            alertBox.showAsModal(false);
        }
        // Inform if layout was shown
        return wasShown;
    }

    /**
     * Initializes layout.
     * @param layout
     * @return
     * @throws IllegalStateException if layout is already initialized
     */
    public boolean initLayout(Class<?extends Layout> layout) {
        if(layouts.containsKey(layout)) {
            throw new IllegalStateException("Layout is initialized.");
        }
        try {
            // Try to create layout
            Layout instance = layout.newInstance();
            instance.initialize(stateManager, app);
            instance.setGuiManager(this);
            instance.setScreen(screen);
            layouts.put(layout, instance);
            return true;
        } catch (Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Shows modal default alert box in the center of screen. Repeating calls destroys old alert and shows new alert.
     * @param title
     * @param message
     * @return returns AlertBox for additional configuration
     */
    public AlertBox showAlert(String title, String message) {
        return showAlert(title, message, new Vector2f(350f, 250f));
    }

    /**
     * Shows modal default alert box in the center of screen. Repeating calls destroys old alert and shows new alert.
     * @param title
     * @param message
     * @param dimensions
     * @return returns AlertBox for additional configuration
     */
    public AlertBox showAlert(String title, String message, Vector2f dimensions) {
        destroyAlert();
        alertBox = new DefaultAlertBox(screen, Vector2f.ZERO, dimensions) {
            @Override
            public void onButtonOkPressed(MouseButtonEvent evt, boolean toggled) {
                super.onButtonOkPressed(evt, toggled); //To change body of generated methods, choose Tools | Templates.
                alertBox = null;
            }
        };
        alertBox.setWindowTitle(title);
        alertBox.setMsg(message);
        //alertBox.setZOrder(10000f);
        alertBox.hide();
        screen.addElement(alertBox);
        alertBox.centerToParent();
        alertBox.showAsModal(false);
        return alertBox;
    }

    /**
     * Destroys alert. Repeating calls does not have effect.
     */
    public void destroyAlert() {
        if(null != alertBox) {
            alertBox.hide();
            screen.removeElement(alertBox);
            alertBox = null;
        }
    }

    /**
     * Get specific layout.
     * @param layout
     * @return
     */
    public Layout getLayout2(Class<?extends Layout> layout) {
        // Create layout
        if(!layouts.containsKey(layout)) {
            initLayout(layout);
        }
        return layouts.get(layout);
    }

    /**
     * Get specific layout.
     * @param layout
     * @return
     */
    public <T extends Layout> T getLayout(Class<T> layout) {
        // Create layout
        if(!layouts.containsKey(layout)) {
            initLayout(layout);
        }
        return (T)layouts.get(layout);
    }



    private class EntityEventsListener extends EntityEventsAdapter {

        private GuiManager guiManager;
        private HudLayout.GunEventsListener gunEventsListener;

        public void initialize(GuiManager guiManager, ZoneOfUprising app) {
            this.guiManager = guiManager;
        }

        public void cleanup() {
        }

        /**
         * @param entitiesManager
         * @param entity
         */
        @Override
        public void entityLoaded(BaseEntitiesManager entitiesManager, Entity entity) {
            if(((ClientEntitiesManager)entitiesManager).isSelectedEntity(entity)) {
                HudLayout hudLayout = getLayout(HudLayout.class);
                // Start listening to targeting
                GunManagerControl gunManager = entity.getNode().getControl(GunManagerControl.class);
                if(null != gunManager) {
                    gunEventsListener = hudLayout.new GunEventsListener();
                    gunManager.addGunEventsListener(gunEventsListener);
                }

                // Load Layout dependent on world type Hangar
                if(worldManager.isWorldType(WorldProfile.Type.Hangar)) {
                    guiManager.show(MyProfileLayout.class);
                }
                // Load Layout dependent on world type Gameplay
                if(worldManager.isWorldType(WorldProfile.Type.Gameplay)) {
                    guiManager.show(HudLayout.class);
                }
            }
        }

        @Override
        public void entityUnloaded(BaseEntitiesManager entitiesManager, Entity entity) {
            if(((ClientEntitiesManager)entitiesManager).isSelectedEntity(entity)) {
                // Stop listening to targeting
                GunManagerControl gunManager = entity.getNode().getControl(GunManagerControl.class);
                if(null != gunManager && null != gunEventsListener) {
                    gunManager.removeGunEventsListener(gunEventsListener);
                }
            }
        }
    }

    private class WorldEventsListener extends WorldEventsAdapter {

        private GuiManager guiManager;
        private ClientWrapper centralClient;

        public void initialize(GuiManager guiManager, ZoneOfUprising app) {
            this.guiManager = guiManager;
            this.centralClient = app.getCentralClient();
        }

        public void cleanup() {
        }

        @Override
        public void worldLoaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
            super.worldLoaded(worldManager, worldProfile, world);

            // Load things dependent on world type gameplay
            if(worldProfile.isType(WorldProfile.Type.Gameplay)) {
                // When gameplay world is loaded, we should destroy any potential alerts
                guiManager.destroyAlert();
            }
        }

        /**
         * @param worldManager
         * @param worldProfile
         * @param world
         */
        @Override
        public void worldUnloaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
            // Pokud není zažádáno o jiný svět pomocí requestWorld(WorldProfile), zobrazíme přihlášení
            // TODO: refactor
            if(!worldManager.isWorldLoaded() && !worldManager.isWorldLoading()) {
                if(null != centralClient && centralClient.isConnected()) {
                    guiManager.show(MyProfileLayout.class);
                } else {
                    guiManager.show(LoginLayout.class);
                }
            }
        }
    }



    private class AudioListener implements VolumeChangedListener {

        private AudioManager audioManager;

        public void initialize(GuiManager guiManager, ZoneOfUprising app) {
            this.audioManager = app.getAudioManager();
            audioManager.addVolumeChangedListener(this);
        }

        public void cleanup() {
            audioManager.removeVolumeChangedListener(this);
        }


        public void onVolumeChanged(String volumeType, float newVolume) {
            screen.setUIAudioVolume(audioManager.getGuiVolume());
        }
    }



    private class CentralClientListener implements MessageListener<Client> {

        private ClientWrapper centralClient;

        public void initialize(GuiManager guiManager, ZoneOfUprising app) {
            this.centralClient = app.getCentralClient();
            centralClient.addMessageListener(this,
                AlertMessage.class,
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
                    if(m instanceof AlertMessage) {
                        alertMessage((AlertMessage)m);
                    } else if(m instanceof UserFleetMessage) {
                        userFleetMessage((UserFleetMessage)m);
                    }
                    return null; 
                }
            });
        }

        public void alertMessage(AlertMessage m) {
            showAlert(m.title, m.message);
        }

        /**
         * Make sure to show my profile layout even if no ship is selected.
         * @param m
         */
        public void userFleetMessage(UserFleetMessage m) {
            boolean isSelected = false;
            if(null != m.fleet) {
                for(EntityProfile entityProfile : m.fleet) {
                    if(entityProfile.isSelected()) {
                        isSelected = true;
                        break;
                    }
                }
            }
            if(!isSelected) {
                show(MyProfileLayout.class);
            }
        }
    }
}
