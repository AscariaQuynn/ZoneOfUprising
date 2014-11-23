/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.guns;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import cz.ascaria.network.messages.ActionMessage;
import cz.ascaria.network.messages.EntityAnalogMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.projectiles.ProjectileBuilder;
import cz.ascaria.zoneofuprising.projectiles.ProjectileControl;
import cz.ascaria.zoneofuprising.projectiles.ProjectileLoaded;
import cz.ascaria.zoneofuprising.utils.NodeHelper;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import cz.ascaria.zoneofuprising.world.ServerWorldManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class GunManagerControl extends AbstractControl {

    private ZoneOfUprising app;
    private BaseWorldManager worldManager;
    private boolean isServer;

    private Entity entity;

    private LinkedList<GunEvents> gunEvents = new LinkedList<GunEvents>();

    private int gunNum = 0;
    private int gunMode = 1;

    private ArrayList<Node> gunSlots = new ArrayList<Node>();
    private HashMap<String, GunControl> gunControls = new HashMap<String, GunControl>();
    private HashMap<String, Spatial> barrels = new HashMap<String, Spatial>();

    private float aimVectorTimeout = 0f;
    private Vector3f aimVector;
    private Spatial lockedTarget;
    private boolean fire = false;

    /**
     * @param app
     */
    public void initialize(Entity entity, ZoneOfUprising app) {
        this.app = app;
        this.worldManager = app.getWorldManager();
        this.isServer = app.isServer();

        this.entity = entity;
    }

    public void cleanup() {
        worldManager = null;
        app = null;
    }

    public void addGunEventsListener(GunEvents listener) {
        gunEvents.add(listener);
    }

    public void removeGunEventsListener(GunEvents listener) {
        gunEvents.remove(listener);
    }

    /**
     * Add gun slot.
     * @param node 
     */
    public void addGunSlot(Node node) {
        if(null != node) {
            gunSlots.add(node);
        }
    }

    /**
     * Add gun at the next empty position.
     * @param gunBuilder
     * @return true if gun was added, false otherwise
     */
    public boolean addGun(GunBuilder gunBuilder) {
        if(gunSlots.size() > 0) {
            for(int i = 0; i < gunSlots.size(); i++) {
                if(gunSlots.get(i).getQuantity() == 0) {
                    return addGun(i, gunBuilder);
                }
            }
        }
        return false;
    }

    /**
     * Add gun at the specified position index.
     * @param index
     * @param gunBuilder
     * @return true if gun was added, false otherwise
     */
    public boolean addGun(int index, GunBuilder gunBuilder) {
        if(gunSlots.size() > index && gunSlots.get(index).getQuantity() == 0) {
            gunBuilder.initialize(app);
            final Node gun = gunBuilder.build(spatial.getUserData("entityName") + "-gun-" + gunNum++);
            final GunControl gunControl = gun.getControl(GunControl.class);
            if(null == gunControl) {
                throw new IllegalStateException("Gun pod does not have GunControl");
            }
            // Set gun manager
            // TODO: refactor
            gunControl.setGunManager(this);
            // Add gun
            gunSlots.get(index).attachChild(gun);
            gunControls.put((String)gun.getUserData("gunName"), gunControl);
            // Add barrel in gun
            // TODO: refactor
            // Prepare gun parts
            gun.depthFirstTraversal(new SceneGraphVisitor() {
                private int i = 0;
                public void visit(Spatial spatial) {
                    if("Barrel".equals(spatial.getName())) {
                        String barrelName = (String)gun.getUserData("gunName") + "-barrel-" + i++;
                        // TODO: refactor
                        if(addBarrel(barrelName, spatial)) {
                            gunControl.addBarrelName(barrelName);
                        }
                    }
                }
            });
            // Break cycle and return gun added
            return true;
        }
        return false;
    }

    public int getGunSlotsCount() {
        return gunSlots.size();
    }

    public Collection<GunControl> getGunControls() {
        return gunControls.values();
    }

    /**
     * Returns the quantity of projectiles remaining in the gun.
     * @return 
     */
    public int getRemainingProjectilesQuantity() {
        int quantity = 0;
        for(GunControl gunControl : gunControls.values()) {
            quantity += gunControl.getProjectilesQuantity();
        }
        return quantity;
    }

    /**
     * Toggle gun mode.
     * @param gunMode 
     */
    public void setGunMode(int gunMode) {
        this.gunMode = gunMode;
        System.out.println("Gun mode was set to " + gunMode);
    }

    /**
     * Returns gun mode.
     * @return
     */
    public int getGunMode() {
        return gunMode;
    }

    /**
     * Adds barrel, it is endpoint location and rotation, where projectiles are created.
     * @param barrel 
     * @return was added
     */
    public boolean addBarrel(String barrelName, Spatial barrel) {
        if(!barrels.containsKey(barrelName)) {
            barrels.put(barrelName, barrel);
            return true;
        }
        return false;
    }


    /**
     * Detect our linear velocity.
     * @return 
     */
    protected Vector3f getLinearVelocity() {
        PhysicsRigidBody rigidBody = spatial.getControl(RigidBodyControl.class);
        return null != rigidBody ? rigidBody.getLinearVelocity() : Vector3f.ZERO;
    }

    /**
     * Is any target locked?
     * @return 
     */
    public boolean isTargetLocked() {
        return null != lockedTarget;
    }

    /**
     * Is given target locked?
     * @param target
     * @return
     */
    public boolean isTargetLocked(Spatial target) {
        return (null != target && null != lockedTarget
            && target.getUserDataKeys().contains("entityName")
            && lockedTarget.getUserDataKeys().contains("entityName")
            && ((String)target.getUserData("entityName")).equals(lockedTarget.getUserData("entityName")));
    }

    /**
     * Lock target.
     * @param target
     */
    public void lockTarget(Spatial target) {
        if(null == target) {
            throw new IllegalArgumentException("Locked target cannot be null.");
        }
        lockedTarget = target;
        fireTargetLocked(target);
    }

    /**
     * Unlock target.
     * @param target 
     */
    public void unlockTarget() {
        fireTargetUnlocked(lockedTarget);
        lockedTarget = null;
        for(GunControl gunControl : gunControls.values()) {
            gunControl.aim(Vector3f.ZERO, null);
        }
    }

    private void fireTargetLocked(Spatial target) {
        for(GunEvents listener : gunEvents) {
            listener.targetLocked(this, target);
        }
    }

    private void fireTargetUnlocked(Spatial target) {
        for(GunEvents listener : gunEvents) {
            listener.targetUnlocked(this, target);
        }
    }

    /**
     * Sets aim vector to guns to look at. Resets automatically after 3 seconds if not refreshed.
     * @param aimVector 
     */
    public void setAimVector(Vector3f aimVector) {
        if(null == this.aimVector) {
            this.aimVector = aimVector;
        } else {
            this.aimVector.set(aimVector);
        }
        aimVectorTimeout = 3f;
    }

    /**
     * Returns true if all guns are in cooldown simultaneously, false otherwise.
     * @return 
     */
    public boolean isCooldown() {
        for(GunControl gunControl : gunControls.values()) {
            if(gunControl.isCooldown()) {
                return true;
            }
        }
        return false;
    }

    /**
     * I wanna kill some toasters!
     */
    public void fire() {
        if(gunMode == 1) {
            fire = true;
        }
    }

    public void cease() {
        fire = false;
    }

    /**
     * Fire from one barrel.
     * @param gunName
     * @param barrelName
     * @param projectileName
     * @return was shot fired?
     */
    public void fireFromBarrel(final String gunName, final String barrelName, String projectileName) {
        final Node barrel = (Node)barrels.get(barrelName);
        final GunControl gunControl = gunControls.containsKey(gunName) ? gunControls.get(gunName) : null;
        final ProjectileBuilder projectileBuilder = null != gunControl ? gunControl.getProjectileBuilder() : null;
        if(null != barrel && null != projectileBuilder && null != gunControl) {
            // Get linear velocity
            RigidBodyControl rigidBody = NodeHelper.tryFindRigidBody(barrel);
            final Vector3f linearVelocity = null != rigidBody ? rigidBody.getLinearVelocity() : new Vector3f();
            // Add scattering
            NodeHelper.scatter(barrel, gunControl.getScatterAmount() * projectileBuilder.getScatteringMultiplier());
            // Create projectile
            worldManager.loadProjectile(projectileName, projectileBuilder, new ProjectileLoaded() {
                public void before(String projectileName, Node projectile) {
                    float fireSpeed = projectileBuilder.getBaseProjectileSpeed() * gunControl.getProjectileSpeedModifier();
                    // If we want to attach projectile to entity's hiearchy instead of projectiles node
                    if(projectileBuilder.attachToEntity()) {
                        // Gimme shine, gimme light, gimme that which I want to fight, Ooh!
                        barrel.attachChild(projectile);
                    } else {
                        // Gimme fuel, Gimme fire, Gimme that which I desire, Ooh!
                        worldManager.moveInWorld("projectilesNode", projectile);
                        ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
                        projectileControl.setPosition(barrel.getWorldTranslation());
                        projectileControl.setRotation(barrel.getWorldRotation());
                        projectileControl.setLinearVelocity(linearVelocity.add(barrel.getWorldRotation().mult(new Vector3f(0f, 0f, fireSpeed))));
                        projectileControl.setAngularVelocity(new Vector3f(0f, 0f, 3f));
                    }
                }
                public void after(String projectileName, Node projectile) {
                    // TODO: refactor
                    ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
                    projectileControl.setEntity(entity);
                    worldManager.playEffectInstance(projectileControl.getShotSound(), barrel);
                    worldManager.showEffect(projectileControl.getShotEffect(), barrel);
                    // Broadcast projectile
                    // TODO: refactor
                    if(isServer) {
                        String entityName = spatial.getUserData("entityName");
                        ((ServerWorldManager)worldManager).projectileBroadcast(entityName, gunName, barrelName, projectileName);
                    }
                }
            });
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        // Refactor
        if(null != lockedTarget && null == lockedTarget.getParent()) {
            unlockTarget();
        }
        if(fire || null != lockedTarget || null != aimVector) {
            for(Map.Entry<String, GunControl> entry : gunControls.entrySet()) {
                GunControl gunControl = entry.getValue();
                if(null != lockedTarget) {
                    gunControl.aim(getLinearVelocity(), lockedTarget);
                } else if(null != aimVector) {
                    gunControl.aim(aimVector);
                }
                if(fire && !gunControl.isCooldown()) {
                    gunControl.fire(entry.getKey() /* key je gun name*/);
                }
            }
        }
        // Aim vector timeout
        if(null != aimVector && null == lockedTarget) {
            aimVectorTimeout -= tpf;
            if(aimVectorTimeout <= 0f) {
                aimVectorTimeout = 0f;
                aimVector = null;
                unlockTarget();
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    /**
     * Receive action message.
     * @param m
     * @param targetables
     */
    public void actionMessage(ActionMessage m, HashMap<String, Spatial> targetables) {
        // Apply fire modes
        String gunFireMode = m.getAction("GunFireMode");
        if(null != gunFireMode) {
            setGunMode(gunFireMode.equals("Turrets") ? 1 : 0);
        }
        // Apply gun actions
        if(getGunMode() == 1) {
            String gunLockTarget = m.getAction("GunLockTarget");
            if(null != gunLockTarget) {
                Spatial target = targetables.containsKey(gunLockTarget) ? targetables.get(gunLockTarget) : null;
               if(null != target) {
                    lockTarget(target);
                } else {
                    Main.LOG.log(Level.WARNING, "GunLockTarget was requested, but entity with name ''{0}'' was not found.", gunLockTarget);
                }
            }
            String gunToggleFire = m.getAction("GunToggleFire");
            if(null != gunToggleFire) {
                if(gunToggleFire.equals("Fire")) {
                    fire();
                } else {
                    cease();
                }
            }
        }
        // Unlock target even if gun is not active
        if(m.hasAction("GunUnlockTarget")) {
            unlockTarget();
        } 
    }

    /**
     * Receive analog message.
     * @param m
     */
    public void analogMessage(EntityAnalogMessage m) {
        // Apply guns aim vector
        if(!m.gunsAimVector.equals(Vector3f.ZERO)) {
            setAimVector(m.gunsAimVector);
        }
    }
}
