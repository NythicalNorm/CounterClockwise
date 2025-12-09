package com.blockninja.counterclockwise.mixin.create.fan;

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import com.blockninja.counterclockwise.ships.FanForceInducer;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(EncasedFanBlock.class)
public class MixinEncasedFanBlock extends Block {
    public MixinEncasedFanBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (pLevel instanceof ServerLevel serverLevel) {
            LoadedServerShip ship = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, pPos);
            if (ship == null) return;
            FanForceInducer inducer = FanForceInducer.getOrCreate(ship);
            inducer.removeFan(pPos);
        }
    }
}
