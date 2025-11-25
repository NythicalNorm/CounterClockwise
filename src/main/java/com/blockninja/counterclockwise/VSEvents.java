package com.blockninja.counterclockwise;

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.event.RegisteredListener;
import org.valkyrienskies.core.api.events.CollisionEvent;
import org.valkyrienskies.core.api.physics.ContactPoint;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import net.minecraft.server.MinecraftServer;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;


public class VSEvents {

    public static void collide(CollisionEvent collisionEvent, RegisteredListener registeredListener) {
        PhysShip shipA = collisionEvent.getPhysLevel().getShipById(collisionEvent.getShipIdA());
        PhysShip shipB = collisionEvent.getPhysLevel().getShipById(collisionEvent.getShipIdB());

        // TODO: handle multiple ships later
        PhysShip mainShip = (shipA != null) ? shipA : shipB;

        // Shouldn't ever happen
        if (mainShip == null) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        // HELLA sussy
        // But ruby said I could do it
        LoadedServerShip serverShip;
        try {
            serverShip = VSGameUtilsKt.getShipObjectWorld(server).getLoadedShips().getById(mainShip.getId());
        } catch (ConcurrentModificationException cme) {
            // Bad luck, do nothing, better luck next time
            return;
        }

        if (serverShip != null) {
            BeltStorageForceInducer inducer = serverShip.getAttachment(BeltStorageForceInducer.class);
            if (inducer != null) {

                List<Pair<Vector3dc, BeltStorageForceInducer.BeltData>> matchingContacts = new ArrayList<>();

                for (ContactPoint contact : collisionEvent.getContactPoints()) {
                    Vector3dc contactWorldPos = contact.getPosition();

                    for (Long blockPosLong : inducer.beltLocations.keySet()) {
                        // Belt is in shipyard, but collision is in world, so we transform ship -> world
                        Vector3d blockWorldPos = mainShip.getTransform().getShipToWorld()
                                .transformPosition(VectorConversionsMCKt.toJOML(BlockPos.of(blockPosLong).getCenter()), new Vector3d());

                        // TODO: This is rather sussy
                        // We should replace it with an AABB of the belts voxel shape,
                        // and transform collision point world -> ship to check against the AABB
                        if (contactWorldPos.distance(blockWorldPos) <= 0.8) {
                            matchingContacts.add(new Pair<>(contactWorldPos, inducer.beltLocations.get(blockPosLong)));
                        }
                    }
                }

                for (Pair<Vector3dc, BeltStorageForceInducer.BeltData> pair : matchingContacts) {
                    // Collision point world -> ship
                    Vector3d forcePos = new Vector3d(pair.getFirst());
                    mainShip.getTransform().getWorldToShip().transformPosition(forcePos);

                    // Collision point ship -> COM offset
                    forcePos.add(0.5, 0.5, 0.5)
                            .sub(mainShip.getTransform().getPositionInShip());

                    pair.getSecond().speed()

                    mainShip.applyInvariantForceToPos(, 0), forcePos);
                }
            }
        }

        // check if belt
        //if (!AllBlocks.BELT.has(level.getBlockState(worldPosition)))
        //    return;

    }
}
