package com.blockninja.counterclockwise.mixinducks.create.mechanical_arm;

import net.minecraft.core.BlockPos;

public interface ArmInteractionPointMixinDuck {
    void setPos(BlockPos pos);
    BlockPos getPos();
}
