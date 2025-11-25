package com.blockninja.counterclockwise.ships;

import com.blockninja.counterclockwise.jackson.BlockPosKeySerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.*;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.concurrent.ConcurrentHashMap;

public final class BeltStorageForceInducer implements ShipPhysicsListener {

    public ConcurrentHashMap<Long, BeltData> beltLocations = new ConcurrentHashMap<>();

    @Override
    public void physTick(@NotNull PhysShip physShip, @NotNull PhysLevel physLevel) {
        // Do fuck all
    }

    public static BeltStorageForceInducer getOrCreate(LoadedServerShip ship) {
        BeltStorageForceInducer attachment = ship.getAttachment(BeltStorageForceInducer.class);
        if (attachment == null) {
            attachment = new BeltStorageForceInducer();
            ship.setAttachment(attachment);
        }
        return attachment;
    }

    public static BeltStorageForceInducer get(Level level, BlockPos pos) {
        ServerLevel serverLevel = (ServerLevel) level;

        LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos(serverLevel, pos);

        return ship != null ? getOrCreate(ship) : null;
    }

    public record BeltData(BeltSlope slop, BeltPart part, float speed) {}
}
