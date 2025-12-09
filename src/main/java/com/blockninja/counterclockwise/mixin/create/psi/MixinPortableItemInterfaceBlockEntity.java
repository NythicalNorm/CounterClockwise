package com.blockninja.counterclockwise.mixin.create.psi;

import com.blockninja.counterclockwise.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import com.blockninja.counterclockwise.create.psi.PSIWithShip;
import com.blockninja.counterclockwise.create.psi.PortableItemInterfaceWithShipController;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(PortableItemInterfaceBlockEntity.class)
public abstract class MixinPortableItemInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {
    public MixinPortableItemInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(
            method = "invalidateCapability",
            at = @At("TAIL"),
            remap = false
    )
    private void invalidateCapability(CallbackInfo ci) {
        if (this instanceof IPSIWithShipBehavior behavior) {
            final PSIWithShip controller = behavior.getController();
            if (controller != null) {
                controller.invalidateCapability();
            }
        }
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void onInit(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getController() instanceof PortableItemInterfaceWithShipController controller) {
            controller.setCapability(controller.createEmptyHandler(behavior));
        }
    }

    @ModifyExpressionValue(
            method = "getCapability",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/util/LazyOptional;cast()Lnet/minecraftforge/common/util/LazyOptional;"
            ),
            remap = false
    )
    private <X> LazyOptional<X> modify(LazyOptional<X> original) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && behavior.getController() instanceof PortableItemInterfaceWithShipController controller) {
            final LazyOptional<IItemHandlerModifiable> capability = controller.getCapability();
            if (capability != null) {
                return capability.cast();
            }
        }
        return original;
    }
}
