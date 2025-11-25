package com.blockninja.counterclockwise.mixin;

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(BeltBlockEntity.class)
public abstract class MixinBeltBlockEntity extends BlockEntity {

    @Shadow
    public abstract float getBeltMovementSpeed();

    public MixinBeltBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Inject(
            method = "tick",
            at = @At("RETURN"),
            remap = false
    )
    private void injectTick(CallbackInfo ci) {
        if (!AllBlocks.BELT.has(level.getBlockState(worldPosition)))
            return;

        if (level instanceof ServerLevel serverLevel) {
            LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, worldPosition);
            if (ship == null) return;

            BeltStorageForceInducer inducer = BeltStorageForceInducer.getOrCreate(ship);
            inducer.beltLocations.put(worldPosition.asLong(), new BeltStorageForceInducer.BeltData(
                    getBlockState().getValue(BeltBlock.SLOPE),
                    getBlockState().getValue(BeltBlock.PART),
                    getBeltMovementSpeed()
            ));
        }

    }
}
