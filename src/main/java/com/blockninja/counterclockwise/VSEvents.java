package com.blockninja.counterclockwise;

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.core.jmx.Server;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.valkyrienskies.core.api.event.RegisteredListener;
import org.valkyrienskies.core.api.events.CollisionEvent;
import org.valkyrienskies.core.api.physics.ContactPoint;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.api.VsApi;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import net.minecraft.server.MinecraftServer;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.physics_api.voxel.CollisionPoint;

import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;


public class VSEvents {
    public static VsApi api = ValkyrienSkies.api();

    public static void collide(CollisionEvent collisionEvent, RegisteredListener registeredListener) {
        PhysShip shipA = collisionEvent.getPhysLevel().getShipById(collisionEvent.getShipIdA());
        PhysShip shipB = collisionEvent.getPhysLevel().getShipById(collisionEvent.getShipIdB());

        // TODO: handle multiple ships later
        PhysShip mainShip = (shipA != null) ? shipA : shipB;

        // Shouldn't ever happen
        if (mainShip == null) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        ServerLevel level = registeryDimToLevel(collisionEvent.getDimensionId());

        try {
            if (level != null) {
                for (ContactPoint point : collisionEvent.getContactPoints()) {
                    Vec3 pointA = VectorConversionsMCKt.toMinecraft(point.getPosition());

                    /*Entity marker = new Marker(EntityType.MARKER, level);
                    marker.setPos(pointA);
                    level.addFreshEntity(marker);*/
                }
            }
        } catch (ConcurrentModificationException cme) {
            System.out.println("Cme wuh oh");
        }
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
                        // Belt is in shipyard, but collision is in world, so we transform world -> ship to check against belt AABB
                        Vector3d shipCollisionPos = mainShip.getTransform().getWorldToShip().transformPosition(contactWorldPos, new Vector3d());

                        // TODO: replace with collision shape
                        BlockPos beltPos = BlockPos.of(blockPosLong);
                        AABB beltAABB = new AABB(beltPos.getX(), beltPos.getY(), beltPos.getZ(), beltPos.getX() + 1, beltPos.getY() + 1, beltPos.getZ() + 1);
                        beltAABB = beltAABB.inflate(0.1);

                        if (beltAABB.contains(VectorConversionsMCKt.toMinecraft(shipCollisionPos))) {
                            System.out.println("e");
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

                    Vector3f forceDir = pair.getSecond().direction().step();
                    mainShip.getTransform().getShipToWorld().transformDirection(forceDir);

                    mainShip.applyInvariantForceToPos(new Vector3d(forceDir).mul(mainShip.getMass()).mul(Math.abs(pair.getSecond().speed())*48), forcePos);
                }
            }
        }


        // check if belt
        //if (!AllBlocks.BELT.has(level.getBlockState(worldPosition)))
        //    return;

    }

    /**
     * Get {@link net.minecraft.server.level.ServerLevel ServerLevel} from a VS dimension ID.
     *
     * @param dimension The dimension ID string in format registry_namespace:registry_name:dimension_namespace:dimension_name
     * @return A {@link net.minecraft.server.level.ServerLevel ServerLevel} instance with the dimension ID given
     */
    @SuppressWarnings("removal")
    public static ServerLevel registeryDimToLevel(final String dimension) {
        // Split 'minecraft:dimension:namespace:dimension_name' into [minecraft, dimension, namespace, dimension_name]
        final String[] parts = dimension.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Unexpected dimension ID: " + dimension);
        }
        final ResourceKey levelId = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(parts[0], parts[1])), new ResourceLocation(parts[2], parts[3]));
        return ValkyrienSkiesMod.getCurrentServer().getLevel(levelId);
    }
}
