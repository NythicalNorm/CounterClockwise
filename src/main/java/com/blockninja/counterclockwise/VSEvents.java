package com.blockninja.counterclockwise;

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import net.minecraftforge.event.server.ServerLifecycleEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.event.RegisteredListener;
import org.valkyrienskies.core.api.events.CollisionEvent;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import net.minecraft.server.MinecraftServer;

import java.util.ConcurrentModificationException;


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
                System.out.println(inducer.map);
            }
        }
    }
}
