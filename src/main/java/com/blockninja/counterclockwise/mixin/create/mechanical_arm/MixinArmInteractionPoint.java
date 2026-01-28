package com.blockninja.counterclockwise.mixin.create.mechanical_arm;

import com.blockninja.counterclockwise.mixinducks.create.mechanical_arm.ArmInteractionPointMixinDuck;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(ArmInteractionPoint.class)
public abstract class MixinArmInteractionPoint implements ArmInteractionPointMixinDuck {
    @Shadow @Final @Mutable
    protected BlockPos pos;

    @Override
    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public BlockPos getPos() {
        return this.pos;
    }
}
