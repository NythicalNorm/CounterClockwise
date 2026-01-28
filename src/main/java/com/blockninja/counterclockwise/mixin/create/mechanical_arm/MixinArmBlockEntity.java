package com.blockninja.counterclockwise.mixin.create.mechanical_arm;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import static com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity.getRange;

@Pseudo
@Mixin(ArmBlockEntity.class)
public abstract class MixinArmBlockEntity extends KineticBlockEntity {

    @Shadow(remap = false)
    boolean updateInteractionPoints;

    public MixinArmBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @WrapOperation(
            method = "searchForItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/mechanicalArm/ArmInteractionPoint;isValid()Z"
            ),
            remap = false
    )
    public boolean searchForItem(ArmInteractionPoint instance, Operation<Boolean> original, @Local ArmInteractionPoint armInteractionPoint) {
        Vec3 armPos = getBlockPos().getCenter();
        Vec3 pointPos = instance.getPos().getCenter();
        if (VSGameUtilsKt.squaredDistanceBetweenInclShips(getLevel(), armPos, pointPos, null) > Mth.square(getRange())) {
            return false;
        } else {
            // I don't know why it just calls the original if the shipdist < maxdist,
            // but it seems to work so I'm not going to touch it
            return original.call(instance);
        }
    }


    @WrapOperation(
            method = "searchForDestination",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/mechanicalArm/ArmInteractionPoint;isValid()Z"
            ),
            remap = false
    )
    public boolean searchForDestination(ArmInteractionPoint instance, Operation<Boolean> original, @Local ArmInteractionPoint armInteractionPoint) {
        Vec3 armPos = getBlockPos().getCenter();
        Vec3 pointPos = instance.getPos().getCenter();
        if (VSGameUtilsKt.squaredDistanceBetweenInclShips(getLevel(), armPos, pointPos, null) > Mth.square(getRange())) {
            return false;
        } else {
            return original.call(instance);
        }
    }

    @Inject(
            method = "lazyTick",
            at = @At("HEAD"),
            remap = false
    )
    public void updatePoints(CallbackInfo ci) {
        updateInteractionPoints = true;
    }
}
