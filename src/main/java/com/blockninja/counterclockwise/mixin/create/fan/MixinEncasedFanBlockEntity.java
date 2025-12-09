package com.blockninja.counterclockwise.mixin.create.fan;

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import com.blockninja.counterclockwise.ships.FanForceInducer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.fan.EncasedFanBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(EncasedFanBlockEntity.class)
public abstract class MixinEncasedFanBlockEntity extends KineticBlockEntity {
    public MixinEncasedFanBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Shadow
    public abstract Level getAirCurrentWorld();

    @Override
    public void setSpeed(float speed) {
        super.setSpeed(speed);

        EncasedFanBlockEntity thiz = EncasedFanBlockEntity.class.cast(this);
        BlockPos pos = thiz.getBlockPos();

        if (getAirCurrentWorld() instanceof ServerLevel serverLevel) {
            LoadedServerShip ship = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, pos);
            if (ship == null) return;
            FanForceInducer inducer = FanForceInducer.getOrCreate(ship);
            inducer.addFan(pos, new FanForceInducer.FanData(
                    // Opposite so that we push in opposite of direction particles are going
                    thiz.getAirFlowDirection().getOpposite(),
                    speed
            ));
        }
    }
}
