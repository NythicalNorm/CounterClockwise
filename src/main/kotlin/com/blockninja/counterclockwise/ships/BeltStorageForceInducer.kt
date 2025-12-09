package com.blockninja.counterclockwise.ships

import com.simibubi.create.content.kinetics.belt.BeltPart
import com.simibubi.create.content.kinetics.belt.BeltSlope
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.ShipPhysicsListener
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.core.api.world.PhysLevel
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import java.util.concurrent.ConcurrentHashMap

class BeltStorageForceInducer : ShipPhysicsListener {
    @JvmField
    var beltLocations: ConcurrentHashMap<Long, BeltData> = ConcurrentHashMap<Long, BeltData>()

    @OptIn(PhysTickOnly::class)
    override fun physTick(physShip: PhysShip, physLevel: PhysLevel) {
        // Do fuck all
    }

    @JvmRecord
    data class BeltData(val slop: BeltSlope, val part: BeltPart, val speed: Float, val direction: Direction)

    companion object {
        @OptIn(GameTickOnly::class)
        @JvmStatic
        fun getOrCreate(ship: LoadedServerShip): BeltStorageForceInducer {
            var attachment = ship.getAttachment(BeltStorageForceInducer::class.java)
            if (attachment == null) {
                attachment = BeltStorageForceInducer()
                ship.setAttachment(attachment)
            }
            return attachment
        }

        @OptIn(GameTickOnly::class)
        fun get(level: Level, pos: BlockPos): BeltStorageForceInducer? {
            val serverLevel = level as ServerLevel?

            val ship = serverLevel.getShipObjectManagingPos(pos)

            return if (ship != null) getOrCreate(ship) else null
        }
    }
}