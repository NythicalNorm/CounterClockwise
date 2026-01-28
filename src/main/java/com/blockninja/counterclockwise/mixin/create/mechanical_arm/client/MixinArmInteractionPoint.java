package com.blockninja.counterclockwise.mixin.create.mechanical_arm.client;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmAngleTarget;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSClientGameUtils;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Pseudo
@Mixin(ArmInteractionPoint.class)
public abstract class MixinArmInteractionPoint {

    @Shadow
    protected abstract Vec3 getInteractionPositionVector();

    @Shadow
    protected abstract Direction getInteractionDirection();

    @Shadow public abstract Level getLevel();

    @Shadow(remap = false) protected ArmAngleTarget cachedAngles;

    @Inject(
            method = "getTargetAngles",
            at = @At("HEAD"),
            remap = false
    )
    public void vs_addition$getTargetAngles(BlockPos armPos, boolean ceiling, CallbackInfoReturnable<ArmAngleTarget> cir) {
        final Vector3d interactionPositionVector = VectorConversionsMCKt.toJOML(getInteractionPositionVector());
        final ClientShip armShip = VSClientGameUtils.getClientShip(armPos.getX(), armPos.getY(), armPos.getZ());
        final ClientShip targetShip = VSClientGameUtils.getClientShip(interactionPositionVector.x, interactionPositionVector.y, interactionPositionVector.z);
        if (armShip != targetShip) {
            final Vector3d target = new Vector3d();
            if(armShip == null) {
                targetShip.getRenderTransform().getShipToWorld().transformPosition(interactionPositionVector, target);
            } else if(targetShip == null) {
                armShip.getRenderTransform().getWorldToShip().transformPosition(interactionPositionVector, target);
            } else {
                targetShip.getRenderTransform().getShipToWorld().transformPosition(interactionPositionVector, target);
                armShip.getRenderTransform().getWorldToShip().transformPosition(target);
            }
            cachedAngles = new ArmAngleTarget(armPos, VectorConversionsMCKt.toMinecraft(target) , getInteractionDirection(), ceiling);
        }
    }
}
