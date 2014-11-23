/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.controls;

import cz.ascaria.zoneofuprising.engines.EnginesControl;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.scene.Spatial;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.messages.EntityActionMessage;
import cz.ascaria.network.messages.EntityAnalogMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.cameras.CamerasManager;
import cz.ascaria.zoneofuprising.guns.GunManagerControl;
import cz.ascaria.zoneofuprising.missiles.MissileManagerControl;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class UserInputControl extends ControlAdapter implements ActionListener, AnalogListener {

    protected InputManager inputManager;
    protected CamerasManager camerasManager;
    protected boolean inputsRegistered = false;

    public float mouseSensivity = 1f;
    protected boolean mouseControlEnabled = false;
    protected boolean mouseRolling = false;

    protected boolean lockingTarget = false;

    public JmeCursor gunSight;

    protected EntityProfile entityProfile;
    protected ClientWrapper gameClient;
    protected EnginesControl enginesControl;
    protected GunManagerControl gunManager;
    protected MissileManagerControl missilesManager;

    protected HashMap<String, Trigger[]> triggers = new HashMap<String, Trigger[]>() {{
        put("EntityMoveUp", new Trigger[] { new KeyTrigger(KeyInput.KEY_Q) });
        put("EntityMoveDown", new Trigger[] { new KeyTrigger(KeyInput.KEY_E) });
        put("EntityMoveLeft", new Trigger[] { new KeyTrigger(KeyInput.KEY_A) });
        put("EntityMoveRight", new Trigger[] { new KeyTrigger(KeyInput.KEY_D) });
        put("EntityMoveForward", new Trigger[] { new KeyTrigger(KeyInput.KEY_W) });
        put("EntityMoveBackward", new Trigger[] { new KeyTrigger(KeyInput.KEY_S) });

        put("EntityYawLeft", new Trigger[] { new KeyTrigger(KeyInput.KEY_U) });
        put("EntityYawRight", new Trigger[] { new KeyTrigger(KeyInput.KEY_O) });
        put("EntityPitchUp", new Trigger[] { new KeyTrigger(KeyInput.KEY_K) });
        put("EntityPitchDown", new Trigger[] { new KeyTrigger(KeyInput.KEY_I) });
        put("EntityRollLeft", new Trigger[] { new KeyTrigger(KeyInput.KEY_J) });
        put("EntityRollRight", new Trigger[] { new KeyTrigger(KeyInput.KEY_L) });

        /*put("EntityYawRollLeftMouse", new Trigger[] { new MouseAxisTrigger(MouseInput.AXIS_X, true) });
        put("EntityYawRollRightMouse", new Trigger[] { new MouseAxisTrigger(MouseInput.AXIS_X, false) });
        put("EntityPitchUpMouse", new Trigger[] { new MouseAxisTrigger(MouseInput.AXIS_Y, false) });
        put("EntityPitchDownMouse", new Trigger[] { new MouseAxisTrigger(MouseInput.AXIS_Y, true) });

        put("EntityToggleMouseControlEnabled", new Trigger[] { new KeyTrigger(KeyInput.KEY_LCONTROL) });
        put("EntityToggleMouseRolling", new Trigger[] { new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE) });*/

        put("EntityFireModeTurrets", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_1)
        });
        put("EntityFireModeMissiles", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_2)
        });

        put("GunLockUnlockTarget", new Trigger[] { new MouseButtonTrigger(MouseInput.BUTTON_RIGHT) });

        put("GunFireKey", new Trigger[] { new KeyTrigger(KeyInput.KEY_SPACE) });
        put("GunFireMouse", new Trigger[] { new MouseButtonTrigger(MouseInput.BUTTON_LEFT) });

        put("EntityClearForces", new Trigger[] { new KeyTrigger(KeyInput.KEY_C) });

        put("EntityToggleRotationControl", new Trigger[] { new KeyTrigger(KeyInput.KEY_B) });
        put("EntityToggleMovementControl", new Trigger[] { new KeyTrigger(KeyInput.KEY_V) });
    }};

    public UserInputControl(EntityProfile entityProfile, ClientWrapper gameClient, InputManager inputManager, CamerasManager camerasManager) {
        this.entityProfile = entityProfile;
        this.gameClient = gameClient;
        this.inputManager = inputManager;
        this.camerasManager = camerasManager;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if(null != spatial) {
            initialize();
        } else {
            cleanup();
        }
    }

    public void initialize() {
        enginesControl = spatial.getControl(EnginesControl.class);
        if(null == enginesControl) {
            throw new IllegalStateException("Cannot add UserInputControl to spatial without EnginesControl!");
        }
        gunManager = spatial.getControl(GunManagerControl.class);
        if(null == gunManager) {
            throw new IllegalStateException("Cannot add UserInputControl to spatial without GunManagerControl!");
        }
        missilesManager = spatial.getControl(MissileManagerControl.class);
        if(null == missilesManager) {
            throw new IllegalStateException("Cannot add UserInputControl to spatial without MissileManagerControl!");
        }
    }

    public void cleanup() {
        clearInputs();
        enginesControl = null;
        gunManager = null;
    }

    /**
     * Should mouse control the ship?
     * @param mouseControlEnabled 
     */
    public void setMouseControlEnabled(boolean mouseControlEnabled) {
        this.mouseControlEnabled = mouseControlEnabled;
        inputManager.setCursorVisible(!mouseControlEnabled);
    }

    /**
     * Is mouse control for ship enabled?
     * @return 
     */
    public boolean isMouseControlEnabled() {
        return mouseControlEnabled;
    }

    public void registerInputs() {
        if(!inputsRegistered) {
            inputsRegistered = true;
            Main.LOG.log(Level.INFO, "UserInputControl registering inputs");
            for(Map.Entry<String, Trigger[]> entry : triggers.entrySet()) {
                inputManager.addMapping(entry.getKey(), entry.getValue());
            }
            inputManager.addListener(this, triggers.keySet().toArray(new String[triggers.keySet().size()]));
            // Set mouse cursor to aiming cursor
            if(null != gunSight) {
                gunSight.setxHotSpot(gunSight.getWidth() / 2);
                gunSight.setyHotSpot(gunSight.getHeight() / 2);
                inputManager.setMouseCursor(gunSight);
            }
        }
    }

    public void clearInputs() {
        if(inputsRegistered) {
            inputsRegistered = false;
            Main.LOG.log(Level.INFO, "UserInputControl clearing inputs");
            for(String key : triggers.keySet()) {
                inputManager.deleteMapping(key);
            }
            inputManager.removeListener(this);
            // Reset mouse cursor
            inputManager.setMouseCursor(null);
            // Force cursor to be visible
            inputManager.setCursorVisible(true);
        }
    }

    /**
     * @param name
     * @param isPressed
     * @param tpf 
     */
    public void onAction(String name, boolean isPressed, float tpf) {
        if(enabled && !inputManager.isCursorVisible()) {
            // Create message
            EntityActionMessage m = new EntityActionMessage();
            m.setEntityName(entityProfile.getName());

            /**
             * Inform server about user using movement keys
             */
            if(name.startsWith("EntityMove")) {
                if(!name.contains("ward")) {
                    m.addAction("EntitySecondaryMovement", isPressed ? "On" : "Off");
                }
                m.addAction(name, isPressed ? "On" : "Off");
            }

            if(name.equals("EntityToggleMouseControlEnabled")) {
                setMouseControlEnabled(!isPressed);
            }
            if(mouseControlEnabled && name.equals("EntityToggleMouseRolling")) {
                mouseRolling = isPressed;
            }

            if(name.equals("ToggleSpotLights")) {
                m.addAction("ToggleSpotLight", isPressed ? "On" : "Off");
            }
            if(name.equals("TogglePointLights")) {
                m.addAction("TogglePointLight", isPressed ? "On" : "Off");
            }

            if(name.equals("EntityToggleRotationControl")) {
                m.addAction("EntityToggleRotationControl", isPressed ? "On" : "Off");
            }
            if(name.equals("EntityToggleMovementControl")) {
                m.addAction("EntityToggleMovementControl", isPressed ? "On" : "Off");
            }


            if(name.equals("EntityFireModeTurrets") && isPressed) {
                m.addAction("EntityFireMode", "Turrets");
            }
            if(name.equals("EntityFireModeMissiles") && isPressed) {
                m.addAction("EntityFireMode", "Missiles");
            }


            if(name.equals("GunLockUnlockTarget") && (gunManager.isTargetLocked() || missilesManager.isTargetLocked()) && isPressed) {
                m.addAction("GunUnlockTarget");
            }

            if(name.equals("GunFireKey") || (isMouseControlEnabled() && name.equals("GunFireMouse"))) {
                // TODO: fire mode all-at-once, one-by-one, all turret's barrels at once or something like that
                m.addAction("GunToggleFire", isPressed ? "Fire" : "Cease");
            }

            // If message is not empty
            if(!m.isEmpty()) {
                // Send message
                gameClient.send(m);
            }
        }
    }

    /**
     * @param name
     * @param value
     * @param tpf 
     */
    public void onAnalog(String name, float value, float tpf) {
        if(enabled && !inputManager.isCursorVisible()) {
            // Create message
            EntityAnalogMessage m = new EntityAnalogMessage();
            m.setEntityName(entityProfile.getName());

            if(name.equals("GunLockUnlockTarget") && !gunManager.isTargetLocked() && !missilesManager.isTargetLocked()) {
                m.addRay("GunLockTarget", camerasManager.rayFromCamera(1000f));
            }

            /**
             * strafe strafing force to the left (positive value) or right (negative value) side
             * ascent ascending (positive value) or descending (negative value)
             * accelerate accelerating (positive value) or reversing (negative value)
             * acceleration x - strafe, y - ascending, z - accelerating
             */
            /**
             * TODO: u joysticku zaridit aby m.force a m.torque slo od 0 do 1
             */
            if(name.equals("EntityMoveUp")) {
                m.force.y = 1f;
            }
            if(name.equals("EntityMoveDown")) {
                m.force.y = -1f;
            }
            if(name.equals("EntityMoveForward")) {
                m.force.z = 1f;
            }
            if(name.equals("EntityMoveBackward")) {
                m.force.z = -1f;
            }
            if(name.equals("EntityMoveLeft")) {
                m.force.x = 1f;
            }
            if(name.equals("EntityMoveRight")) {
                m.force.x = -1f;
            }

            /**
             * pitch - torque to the up (negative value) or down (positive value)
             * yaw - torque to the left (positive value) or to the right (negative value)
             * roll - torque to the left (negative value) or to the right (positive value)
             * rotation x - pitch, y - yaw, z - roll
             */
            if(name.equals("EntityYawLeft")) {
                m.torque.y = 1f;
            }
            if(name.equals("EntityYawRight")) {
                m.torque.y = -1f;
            }
            if(name.equals("EntityRollLeft")) {
                m.torque.z = -1f;
            }
            if(name.equals("EntityRollRight")) {
                m.torque.z = 1f;
            }
            if(name.equals("EntityPitchUp")) {
                m.torque.x = -1f;
            }
            if(name.equals("EntityPitchDown")) {
                m.torque.x = 1f;
            }

            if(mouseControlEnabled) {
                if(mouseRolling) {
                    if(name.equals("EntityYawRollLeftMouse")) {
                        //m.roll = Math.max(-0.02f, -value) * mouseSensivity;
                    }
                    if(name.equals("EntityYawRollRightMouse")) {
                        //m.roll = Math.min(0.02f, value) * mouseSensivity;
                    }
                } else {
                    if(name.equals("EntityYawRollLeftMouse")) {
                        //m.yaw = Math.min(0.02f, value) * mouseSensivity;
                    }
                    if(name.equals("EntityYawRollRightMouse")) {
                        //m.yaw = Math.max(-0.02f, -value) * mouseSensivity;
                    }
                }
                if(name.equals("EntityPitchUpMouse")) {
                    //m.pitch = Math.max(-0.02f, -value) * mouseSensivity;
                }
                if(name.equals("EntityPitchDownMouse")) {
                    //m.pitch = Math.min(0.02f, value) * mouseSensivity;
                }
            }

            // If message is not empty
            if(!m.isEmpty()) {
                // Send message
                gameClient.send(m);
                // Apply input locally
                //m.applyData(spaceShipControl);
            }
        }
    }
}
