package dev.lazurite.rayon.impl.element.hooks.entity.common;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import dev.lazurite.rayon.impl.Rayon;
import dev.lazurite.rayon.api.element.PhysicsElement;
import dev.lazurite.rayon.impl.element.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.space.MinecraftSpace;
import dev.lazurite.rayon.impl.element.network.EntityElementS2C;
import dev.lazurite.rayon.impl.util.math.QuaternionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public World world;
    @Shadow public float yaw;
    @Shadow public float pitch;

    @Shadow public abstract void updatePosition(double x, double y, double z);

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo info) {
        if (this instanceof PhysicsElement) {
            if (!world.isClient()) {
                EntityElementS2C.send((PhysicsElement) this);
            }

            ElementRigidBody body = ((PhysicsElement) this).getRigidBody();
            Vector3f pos = body.getPhysicsLocation(new Vector3f());

            updatePosition(pos.x, pos.y, pos.z);
            yaw = QuaternionHelper.getYaw(body.getPhysicsRotation(new Quaternion()));
            pitch = QuaternionHelper.getPitch(body.getPhysicsRotation(new Quaternion()));
        }
    }

    @Inject(
            method = "toTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V"
            )
    )
    public void toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info) {
        if (this instanceof PhysicsElement) {
            ((PhysicsElement) this).getRigidBody().toTag(tag);
        }
    }

    @Inject(
            method = "fromTag",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V"
            )
    )
    public void fromTag(CompoundTag tag, CallbackInfo info) {
        if (this instanceof PhysicsElement) {
            ((PhysicsElement) this).getRigidBody().fromTag(tag);
        }
    }

    /**
     * This method cleans up after the {@link MinecraftSpace}
     * by removing any {@link ElementRigidBody}s that have had
     * their entity removed.
     * {@link Entity} removed from the world.
     */
    @Inject(method = "remove", at = @At("HEAD"))
    public synchronized void remove(CallbackInfo info) {
        if (this instanceof PhysicsElement) {
            ElementRigidBody body = ((PhysicsElement) this).getRigidBody();

            if (body.isInWorld()) {
                Rayon.THREAD.get(((Entity) (Object) this).getEntityWorld())
                        .execute(space -> space.removeCollisionObject(body));
            }
        }
    }
}