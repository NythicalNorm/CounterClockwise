package com.blockninja.counterclockwise.mixin.create.mechanical_arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Pseudo
@Mixin(ArmBlockEntity.class)
public interface ArmBlockEntityMixinAccessor {
    @Accessor(remap = false)
    List<ArmInteractionPoint> getInputs();

    @Accessor(remap = false)
    List<ArmInteractionPoint> getOutputs();
}
