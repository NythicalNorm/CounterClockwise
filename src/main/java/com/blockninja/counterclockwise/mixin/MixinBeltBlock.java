package com.blockninja.counterclockwise.mixin;

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(BeltBlock.class)
public class MixinBeltBlock {
    @Inject(
            method = "onRemove",
            at = @At("HEAD"),
            remap = false
    )
    private void injectOnRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        if (world instanceof ServerLevel serverLevel) {
            LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pos);
            if (ship == null) return;
            BeltStorageForceInducer inducer = BeltStorageForceInducer.getOrCreate(ship);
            inducer.beltLocations.remove(pos.asLong());
        }
    }
}
