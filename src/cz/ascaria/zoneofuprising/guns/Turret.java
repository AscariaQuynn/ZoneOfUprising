/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.guns;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import cz.ascaria.zoneofuprising.projectiles.ProjectileBuilder;
import cz.ascaria.zoneofuprising.utils.NodeHelper;
import cz.ascaria.zoneofuprising.utils.QuaternionHelper;
import java.util.LinkedList;

/**
 *
 * @author Ascaria Quynn
 */
public class Turret extends AbstractControl implements GunControl {

    protected GunManagerControl gunManager;

    protected Spatial sight;
    protected Spatial traverser;
    protected Spatial elevator;

    protected LinkedList<String> barrelNames = new LinkedList<String>();
    protected ProjectileBuilder projectileBuilder;
    protected float projectileSpeedModifier = 1.0f;
    protected int quantity = 0;

    protected float fireInitialCooldown = 0.2f;
    protected float fireActualCooldown = 0f;

    protected Vector3f up = new Vector3f();

    protected float rotationSpeed = 1f;
    protected Vector2f elevationLimit = new Vector2f(70f * FastMath.DEG_TO_RAD, 5f * FastMath.DEG_TO_RAD);
    protected Vector2f traversionLimit = new Vector2f(70f * FastMath.DEG_TO_RAD, 70f * FastMath.DEG_TO_RAD);

    protected float scatterAmount = 0.3f;

    public Turret() {
    }

    /**
     * Sets gun manager.
     * @param gunManager
     */
    public void setGunManager(GunManagerControl gunManager) {
        this.gunManager = gunManager;
    }

    /**
     * Set Spatial which aims at target.
     * @param sight 
     */
    public void setSight(Spatial sight) {
        this.sight = sight;
    }

    /**
     * Set Spatial which rotates gun horizontally.
     * @param traverser 
     */
    public void setTraverser(Spatial traverser) {
        this.traverser = traverser;
    }

    /**
     * Set Spatial which rotates gun vertically.
     * @param elevator 
     */
    public void setElevator(Spatial elevator) {
        this.elevator = elevator;
    }

    /**
     * Adds barrel name.
     * @param barrelName
     */
    public void addBarrelName(String barrelName) {
        barrelNames.add(barrelName);
    }

    /**
     * Sets projectile builder to the gun.
     * @param projectileBuilder
     */
    public void setProjectileBuilder(ProjectileBuilder projectileBuilder) {
        this.projectileBuilder = projectileBuilder;
    }

    /**
     * Get gun's projectile builder.
     * @return
     */
    public ProjectileBuilder getProjectileBuilder() {
        return projectileBuilder;
    }

    /**
     * Sets the number of remaining projectiles in the gun.
     * @param quantity 
     */
    public void setProjectilesQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Returns the number of remaining projectiles in the gun.
     * @return 
     */
    public int getProjectilesQuantity() {
        return quantity;
    }

    /**
     * Sets speed modifier of projectile.
     * @param modifier 
     */
    public void setProjectileSpeedModifier(float projectileSpeedModifier) {
        this.projectileSpeedModifier = projectileSpeedModifier;
    }

    /**
     * Gets speed modifier of projectile.
     * @return
     */
    public float getProjectileSpeedModifier() {
        return projectileSpeedModifier;
    }

    /**
     * Sets rotation speed.
     * @param rotationSpeed 
     */
    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * Sets rotation limit angles in degrees, only positive values, all values are Math.abs()'ed.
     * @param maxElevationUp
     * @param maxElevationDown
     * @param maxTraversionLeft
     * @param maxTraversionRight
     */
    public void setRotationLimits(float maxElevationUp, float maxElevationDown, float maxTraversionLeft, float maxTraversionRight) {
        elevationLimit.set(Math.abs(maxElevationUp) * FastMath.DEG_TO_RAD, Math.abs(maxElevationDown) * FastMath.DEG_TO_RAD);
        traversionLimit.set(Math.abs(maxTraversionLeft) * FastMath.DEG_TO_RAD, Math.abs(maxTraversionRight) * FastMath.DEG_TO_RAD);
    }

    /**
     * Set amount for random scattering. Clamped from 0f (no scattering) to 1f (huge scattering).
     * @param scatterAmount
     */
    public void setScatterAmount(float scatterAmount) {
        this.scatterAmount = FastMath.clamp(scatterAmount, 0f, 1f);
    }
    
    /**
     * Returns amount for random scattering.
     * @return
     */
    public float getScatterAmount() {
        return scatterAmount;
    }
    
    /**
     * Is gun at cooldown?
     * @return 
     */
    public boolean isCooldown() {
        return fireActualCooldown > 0f;
    }

    /**
     * Aims gun at given target.
     * @param myLinearVelocity
     * @param target
     */
    public void aim(Vector3f myLinearVelocity, Spatial target) {
        if(null != sight) {
            if(null != target) {
                // Get world up vector of the gun and aim sight at predicted location
                spatial.getWorldRotation().getRotationColumn(1, up);
                sight.lookAt(predictLocation(myLinearVelocity, target), up);
            } else {
                sight.setLocalRotation(Quaternion.IDENTITY);
            }
        }
    }

    /**
     * Aim gun at given vector.
     * @param aimVector
     */
    public void aim(Vector3f aimVector) {
        // Get world up vector of the gun and aim sight at aim vector
        spatial.getWorldRotation().getRotationColumn(1, up);
        sight.lookAt(aimVector, up);
    }

    /**
     * Guns, guns, guns!
     * @param gunName
     */
    public void fire(String gunName) {
        if(null != gunManager) {
            // Fire projectiles
            fireActualCooldown = fireInitialCooldown;
            for(String barrelName : barrelNames) {
                if(quantity > 0) {
                    quantity--;
                    String projectileName = gunName + "-" + barrelName + "-" + "-projectile-" + quantity;
                    gunManager.fireFromBarrel(gunName, barrelName, projectileName);
                }
            }
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        // Do fire cooldown
        if(fireActualCooldown > 0f) {
            fireActualCooldown -= tpf;
        }
        // Aim gun at target
        if(null != sight && null != elevator) {
            // Calculate angles between sight and gun
            float[] angles = QuaternionHelper.rotateTowards(traverser.getLocalRotation().mult(elevator.getLocalRotation()), sight.getLocalRotation(), rotationSpeed, tpf).toAngles(null);
            // Clamp angles
            float elevation = FastMath.clamp(angles[0], -elevationLimit.x, elevationLimit.y);
            float traversion = FastMath.clamp(angles[1], -traversionLimit.x, traversionLimit.y);

            // Rotate actual gun
            rotateGun(elevation, traversion);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        Turret turret = new Turret();
        turret.setSpatial(spatial);
        return turret;
    }

    /**
     * Predicts location of target in future.
     * @param target
     * @return
     * TODO: refactor, asi je to dost blbe
     */
    protected Vector3f predictLocation(Vector3f myLinearVelocity, Spatial target) {
        if(projectileBuilder.getBaseProjectileSpeed() > 0f) {
            // Clone entity's linear velocity
            Vector3f linearVelocity = myLinearVelocity.clone();
            // Try to get target's velocity
            RigidBodyControl targetRigidBody = NodeHelper.tryFindRigidBody(target);
            linearVelocity.subtractLocal(null != targetRigidBody ? targetRigidBody.getLinearVelocity() : Vector3f.ZERO);
            // Compute needed lead offset
            float distance = NodeHelper.getLocalTranslation(target, spatial).length();
            linearVelocity.multLocal(distance / (linearVelocity.length() + (projectileBuilder.getBaseProjectileSpeed() * getProjectileSpeedModifier())));
            // Get predicted direction
            return target.getWorldTranslation().subtract(linearVelocity);
        } else {
            return target.getWorldTranslation();
        }
    }

    /**
     * Instantly rotates gun to given angles.
     * @param elevation angle
     * @param traversion angle
     */
    protected void rotateGun(float elevation, float traversion) {
        // Set elevator rotation (vertical only)
        if(null != elevator) {
            elevator.setLocalRotation(new Quaternion().fromAngleAxis(elevation, Vector3f.UNIT_X));
        }
        // Set traverser rotation (horizontal only)
        if(null != traverser) {
            traverser.setLocalRotation(new Quaternion().fromAngleAxis(traversion, Vector3f.UNIT_Y));
        }
    }
}
