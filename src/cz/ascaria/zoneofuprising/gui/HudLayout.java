/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Spatial;
import cz.ascaria.network.messages.ChatCommandMessage;
import cz.ascaria.network.messages.ChatMessage;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.cameras.CamerasManager;
import cz.ascaria.zoneofuprising.controls.DamageControl;
import cz.ascaria.zoneofuprising.engines.EnginesControl;
import cz.ascaria.zoneofuprising.controls.LightsControl;
import cz.ascaria.zoneofuprising.controls.UserInputControl;
import cz.ascaria.zoneofuprising.engines.MovementCompensator;
import cz.ascaria.zoneofuprising.engines.RotationCompensator;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.guns.GunEventsAdapter;
import cz.ascaria.zoneofuprising.guns.GunManagerControl;
import cz.ascaria.zoneofuprising.utils.AssetsHelper;
import cz.ascaria.zoneofuprising.utils.FasterMath;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.extras.ChatBox;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;

/**
 *
 * @author Ascaria Quynn
 */
public class HudLayout extends BaseLayout {

    public UserInputControl userControl;
    public Entity playerEntity;
    public RigidBodyControl rigidBody;

    private CamerasManager camerasManager;
    private InputManager inputManager;
    private ClientWrapper gameClient;

    private GameClientListener gameClientListener;

    private Panel panel;
    private Element gunSight;
    private ChatBox chatBox;
    private HashMap<String, Label> labels = new HashMap<String, Label>();

    private Window targetWindow;
    private Element targetMark;
    private Spatial target;

    private boolean isFocusedChat = false;
    private boolean msgSent = false;
    private boolean isOpened = false;

    @Override
    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        super.initialize(stateManager, app);

        this.camerasManager = app.getCamerasManager();
        this.inputManager = app.getInputManager();
        this.gameClient = app.getGameClient();
        gameClientListener = new GameClientListener();
        gameClientListener.initialize(this, app);
    }

    public boolean isOpened() {
        return isOpened;
    }

    @Override
    public void open() {
        super.open();
        check();
        if(isOpened()) {
            close();
        }
        isOpened = true;

        inputManager.setCursorVisible(false);

        // Create panel
        panel = new Panel(screen, Vector2f.ZERO, dimensions);
        panel.setAsContainerOnly();
        panel.setIsMovable(false);
        panel.setIsResizable(false);
        //panel.setIgnoreMouse(true);

        createGunSight();

        createChatWindow();
        createDashboard();

        createInfo();

        // Add panel to screen
        screen.addElement(panel);

        labelsIgnoreMouse();
    }

    @Override
    public void close() {
        super.close();
        if(null != panel) {
            isOpened = false;
            destroyTargetWindow();
            panel.hide();
            screen.removeElement(panel);
            panel = null;
            gunSight = null;
            chatBox = null;
            labels.clear();
        }
    }

    public void labelsIgnoreMouse() {
        for(Label label : labels.values()) {
            //label.setIgnoreMouse(true);
        }
    }

    /**
     * Create gun sight.
     */
    public void createGunSight() {
        String imgPath = "Interface/GunSights/WoT-Marsoff-Sniper.png";
        Vector2f imgDimensions = AssetsHelper.getTextureDimensions(app.getAssetManager(), imgPath);
        Vector2f imgPos = FasterMath.floor(dimensions.divide(2f).subtract(imgDimensions.divide(2f)));

        gunSight = new Element(screen, "gunSight", imgPos, imgDimensions, Vector4f.ZERO, imgPath);
        gunSight.setGlobalAlpha(0.75f);
        gunSight.setIsResizable(false);
        gunSight.setIsMovable(false);
        //gunSight.setIgnoreMouse(true);

        panel.addChild(gunSight);
    }

    /**
     * Create chat window.
     */
    private void createChatWindow() {
        int chWidth = (int)(dimensions.x / 4f);
        int chHeight = (int)(dimensions.y / 3f);

        chatBox = new ChatBox(screen, "HudChat", new Vector2f(15f, dimensions.y - chHeight - 15f), new Vector2f(chWidth, chHeight)) {
            @Override
            public void onSendMsg(String msg) {
                boolean sent = false;
                if(gameClient.isConnected()) {
                    if(msg.startsWith("/")) {
                        // Try to recognize and send command
                        Matcher m = Pattern.compile("^([a-z/]+) \"([^\"]+)\" (.+)").matcher(msg);
                        if(m.find()) {
                            gameClient.send(new ChatCommandMessage(m.group(1), playerEntity.getName(), m.group(2), m.group(3)));
                            sent = true;
                        }
                        if(!sent) {
                            Matcher m2 = Pattern.compile("^([a-z/]+)").matcher(msg);
                            if(m2.find()) {
                                gameClient.send(new ChatCommandMessage(m2.group(1), playerEntity.getName()));
                                sent = true;
                            }
                        }
                    }
                    if(!sent) {
                        // Send normal mesage
                        gameClient.send(new ChatMessage(playerEntity.getName(), msg));
                    }
                }
                msgSent = true;
                blurChat();
            }
        };
        chatBox.setGlobalAlpha(0.8f);
        chatBox.setSendKey(KeyInput.KEY_RETURN);
        chatBox.setIsResizable(false);
        chatBox.setIsMovable(false);
        //chatBox.setIgnoreMouse(true);

        panel.addChild(chatBox);
    }

    /**
     * Create dashboard.
     */
    private void createDashboard() {
        EnginesControl enginesControl = null != playerEntity ? playerEntity.getNode().getControl(EnginesControl.class) : null;

        Panel dashBoard = new Panel(screen, new Vector2f(dimensions.x - 415f, dimensions.y - 90f), new Vector2f(400f, 75f));
        dashBoard.setIsResizable(false);
        dashBoard.setIsMovable(false);
        //dashBoard.setIgnoreMouse(true);

        LightsControl lightsControl = null != playerEntity ? playerEntity.getNode().getControl(LightsControl.class) : null;
        Button spotlights = new ButtonAdapter(screen, new Vector2f(5f, 5f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                super.onButtonMouseLeftUp(evt, toggled);
                if(null != userControl) {
                    userControl.onAction("ToggleSpotLights", toggled, 1f/60f);
                }
            }
        };
        if(null != lightsControl) {
            spotlights.setIsToggledNoCallback(lightsControl.isEnabledSpotLights());
        }
        spotlights.setText("Spotlights");
        spotlights.setIsToggleButton(true);
        //spotlights.setIgnoreMouse(true);
        dashBoard.addChild(spotlights);

        Button pointlights = new ButtonAdapter(screen, new Vector2f(110f, 5f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                super.onButtonMouseLeftUp(evt, toggled);
                if(null != userControl) {
                    userControl.onAction("TogglePointLights", toggled, 1f/60f);
                }
            }
        };
        if(null != lightsControl) {
            pointlights.setIsToggledNoCallback(lightsControl.isEnabledPointLights());
        }
        pointlights.setText("Pointlights");
        pointlights.setIsToggleButton(true);
        //pointlights.setIgnoreMouse(true);
        dashBoard.addChild(pointlights);

        Button rotationControl = new ButtonAdapter(screen, new Vector2f(5f, 40f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                super.onButtonMouseLeftUp(evt, toggled);
                if(null != userControl) {
                    userControl.onAction("EntityToggleRotationControl", toggled, 1f/60f);
                }
            }
        };
        if(null != enginesControl) {
            rotationControl.setIsToggledNoCallback(enginesControl.hasCompensator(RotationCompensator.class));
        }
        rotationControl.setWidth(150f);
        rotationControl.setText("Rotation Control");
        rotationControl.setIsToggleButton(true);
        //rotationControl.setIgnoreMouse(true);
        dashBoard.addChild(rotationControl);

        Button movementControl = new ButtonAdapter(screen, new Vector2f(160f, 40f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                super.onButtonMouseLeftUp(evt, toggled);
                if(null != userControl) {
                    userControl.onAction("EntityToggleMovementControl", toggled, 1f/60f);
                }
            }
        };
        if(null != enginesControl) {
            movementControl.setIsToggledNoCallback(enginesControl.hasCompensator(MovementCompensator.class));
        }
        movementControl.setWidth(150f);
        movementControl.setText("Movement Control");
        movementControl.setIsToggleButton(true);
        //movementControl.setIgnoreMouse(true);
        dashBoard.addChild(movementControl);

        panel.addChild(dashBoard);
    }

    /**
     * Create info window.
     */
    private void createInfo() {
        // Create info
        Panel info = new Panel(screen, new Vector2f(dimensions.x - 315f, 15f), new Vector2f(300f, 300f));
        info.setIsResizable(false);
        info.setIsMovable(false);
        //info.setIgnoreMouse(true);

        Label iLinearSpeed = new Label(screen, new Vector2f(30f, 30f), new Vector2f(240f, 11f));
        iLinearSpeed.setText("Linear Speed:");
        info.addChild(iLinearSpeed);
        labels.put("iLinearSpeed", iLinearSpeed);

        Label iAngularSpeed = new Label(screen, new Vector2f(30f, 50f), new Vector2f(240f, 11f));
        iAngularSpeed.setText("Angular Speed:");
        info.addChild(iAngularSpeed);
        labels.put("iAngularSpeed", iAngularSpeed);

        Label iDistanceTraveled = new Label(screen, new Vector2f(30f, 70f), new Vector2f(240f, 11f));
        iDistanceTraveled.setText("Distance Travelled:");
        info.addChild(iDistanceTraveled);
        labels.put("iDistanceTraveled", iDistanceTraveled);

        Label iPlayerHp = new Label(screen, new Vector2f(30f, 90f), new Vector2f(240f, 11f));
        iPlayerHp.setText("HP:");
        info.addChild(iPlayerHp);
        labels.put("iPlayerHp", iPlayerHp);

        Label iPlayerXp = new Label(screen, new Vector2f(30f, 110f), new Vector2f(240f, 11f));
        iPlayerXp.setText("XP:");
        info.addChild(iPlayerXp);
        labels.put("iPlayerXp", iPlayerXp);

        panel.addChild(info);
    }

    /**
     * Creates target window.
     * @param target 
     */
    private void createTargetWindow(Spatial target) {
        destroyTargetWindow();
        this.target = target;

        // Target stats window
        targetWindow = new Window(screen, new Vector2f(dimensions.x - 315f, 330f), new Vector2f(300f, 150f));
        targetWindow.setWindowTitle("Target: " + target.getUserData("entityName"));
        targetWindow.setWindowIsMovable(true);
        targetWindow.setIsResizable(false);
        //targetWindow.setIgnoreMouse(true);

        Label iTargetHp = new Label(screen, new Vector2f(15f, 15f), new Vector2f(260f, 11f));
        iTargetHp.setText("HP:");
        targetWindow.addWindowContent(iTargetHp);
        labels.put("iTargetHp", iTargetHp);

        Label iTargetDistance = new Label(screen, new Vector2f(15f, 35f), new Vector2f(260f, 11f));
        iTargetDistance.setText("Distance:");
        targetWindow.addWindowContent(iTargetDistance);
        labels.put("iTargetDistance", iTargetDistance);

        panel.addChild(targetWindow);

        // Target mark element
        String imgPath = "Interface/GunSights/TargetMark.png";
        Vector2f imgDimensions = AssetsHelper.getTextureDimensions(app.getAssetManager(), imgPath);
        Vector2f imgPos = FasterMath.floor(dimensions.divide(2f).subtract(imgDimensions.divide(2f)));

        targetMark = new Element(screen, "targetMark", imgPos, imgDimensions, Vector4f.ZERO, imgPath);
        targetMark.setGlobalAlpha(0.75f);
        targetMark.setIsResizable(false);
        targetMark.setIsMovable(false);
        //targetMark.setIgnoreMouse(true);

        panel.addChild(targetMark);

        labelsIgnoreMouse();
    }

    private void destroyTargetWindow() {
        if(null != targetWindow && null != panel) {
            target = null;

            panel.removeChild(targetWindow);
            targetWindow = null;

            panel.removeChild(targetMark);
            targetMark = null;

            labels.remove("iTargetHp");
            labels.remove("iTargetDistance");
        }
    }

    public boolean isFocusedChat() {
        if(msgSent) {
            msgSent = false;
            return true;
        }
        return isFocusedChat;
    }

    public void focusChat() {
        TextField chatText = (TextField)screen.getElementById("HudChat:ChatInput");
        if(null != chatText) {
            chatText.setTabFocus();
            isFocusedChat = true;
        }
    }

    public void blurChat() {
        TextField chatText = (TextField)screen.getElementById("HudChat:ChatInput");
        if(null != chatText) {
            chatText.setText("");
            chatText.resetTabFocus();
            isFocusedChat = false;
        }
    }

    public void update(float tpf) {
        if(isOpened()) {
            if(null != playerEntity) {
                DamageControl damageControl = playerEntity.getNode().getControl(DamageControl.class);
                if(null != damageControl) {
                    labels.get("iPlayerHp").setText(FasterMath.format("HP: %.2f", damageControl.getHitPoints()));
                }
                labels.get("iPlayerXp").setText("XP: " + playerEntity.getExperience());
            }
            if(null != rigidBody) {
                float linearSpeed = rigidBody.getLinearVelocity().length() * 3.6f;
                float angularSpeed = rigidBody.getAngularVelocity().length();
                labels.get("iLinearSpeed").setText(FasterMath.format("Linear Speed: %.2f km/h", linearSpeed));
                labels.get("iAngularSpeed").setText(FasterMath.format("Angular Speed: %.2f angular", angularSpeed));
            }
            if(null != playerEntity && null != target) {
                // Calculate distance
                float distance = playerEntity.getNode().getWorldTranslation().distance(target.getWorldTranslation());
                labels.get("iTargetDistance").setText(FasterMath.format("Distance: %.2f m", distance));
                // Move with aim cross
                if(null != targetMark) {
                    Vector3f coords = camerasManager.getScreenCoords(target);
                    if(coords.z < 1f && FasterMath.between(coords.x, 0f, dimensions.x) && FasterMath.between(coords.y, 0f, dimensions.y)) {
                        targetMark.setPosition((int)(coords.x - targetMark.getWidth() / 2f), (int)(coords.y - targetMark.getHeight() / 2f));
                    }
                }
            }
            if(null != target && labels.containsKey("iTargetHp")) {
                // Show hitpoints
                DamageControl damageControl = target.getControl(DamageControl.class);
                if(null != damageControl) {
                    labels.get("iTargetHp").setText(FasterMath.format("HP: %.2f", damageControl.getHitPoints()));
                }
            }
        }
    }

    protected class GunEventsListener extends GunEventsAdapter {

        @Override
        public void targetLocked(GunManagerControl gunManager, Spatial target) {
            System.out.println("Hud Target locked: " + target);
            createTargetWindow(target);
        }

        @Override
        public void targetUnlocked(GunManagerControl gunManager, Spatial target) {
            System.out.println("Hud Target unlocked: " + target);
            destroyTargetWindow();
        }
    }



    private class GameClientListener implements MessageListener<Client> {

        private ClientWrapper gameClient;

        public void initialize(HudLayout hudLayout, ZoneOfUprising app) {
            this.gameClient = app.getGameClient();
            gameClient.addMessageListener(this,
                ChatMessage.class
            );
        }

        public void cleanup() {
            gameClient.removeMessageListener(this);
        }

        /**
         * Forward received messages.
         * @param source
         * @param m
         */
        public void messageReceived(Client source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof ChatMessage) {
                        chatMessage((ChatMessage)m);
                    }
                    return null; 
                }
            });
        }

        /**
         * Receive in-game chat message.
         * @param fleet
         */
        public void chatMessage(ChatMessage m) {
            if(isOpened()) {
                chatBox.receiveMsg(m.name + ": " + m.message);
            }
        }
    }
}
