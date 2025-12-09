package com.blockninja.counterclockwise.ships

import com.blockninja.counterclockwise.centerJOMLD
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
import org.valkyrienskies.mod.common.util.toJOMLD
import java.util.concurrent.ConcurrentHashMap

class FanForceInducer : ShipPhysicsListener {
    @Volatile
    private var fanLocations: ConcurrentHashMap<Long, FanData> = ConcurrentHashMap<Long, FanData>()

    @OptIn(PhysTickOnly::class)
    override fun physTick(physShip: PhysShip, physLevel: PhysLevel) {
        fanLocations.forEach{ long, data ->
            var blockPos = BlockPos.of(long)
            physShip.applyModelForce(
                data.direction.normal.toJOMLD().mul(1000.0),
                blockPos.centerJOMLD()
            )
        }
    }

    @JvmRecord
    data class FanData(val direction: Direction, val speed: Float)

    @GameTickOnly
    fun addFan(pos: BlockPos, data: FanData) {
        fanLocations[pos.asLong()] = data
    }

    @GameTickOnly
    fun removeFan(pos: BlockPos) {
        fanLocations.remove(pos.asLong())
    }

    companion object {
        @OptIn(GameTickOnly::class)
        @JvmStatic
        fun getOrCreate(ship: LoadedServerShip): FanForceInducer {
            var attachment = ship.getAttachment(FanForceInducer::class.java)
            if (attachment == null) {
                attachment = FanForceInducer()
                ship.setAttachment(attachment)
            }
            return attachment
        }

        @OptIn(GameTickOnly::class)
        fun get(level: Level, pos: BlockPos): FanForceInducer? {
            val serverLevel = level as ServerLevel?

            val ship = serverLevel.getShipObjectManagingPos(pos)

            return if (ship != null) getOrCreate(ship) else null
        }
    }
}