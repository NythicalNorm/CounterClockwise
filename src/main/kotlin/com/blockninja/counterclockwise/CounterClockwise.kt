package com.blockninja.counterclockwise

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer
import net.minecraft.core.BlockPos
import net.minecraftforge.fml.common.Mod
import org.joml.Vector3d
import org.valkyrienskies.core.api.VsBeta
import org.valkyrienskies.core.api.event.EventConsumer
import org.valkyrienskies.core.api.event.RegisteredListener
import org.valkyrienskies.core.api.events.CollisionEvent
import org.valkyrienskies.core.api.events.ShipLoadEvent
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.mod.api.events.RegisterBlockStateEvent
import org.valkyrienskies.mod.api.toJOML
import java.util.function.Consumer

@OptIn(PhysTickOnly::class, VsBeta::class, GameTickOnly::class)
@Mod(CounterClockwise.MOD_ID)
class CounterClockwise {

    init {

        // region Register BlockPos so it can be serialized as a map key
        /*SimpleModule module = new SimpleModule();
        module.addKeySerializer(BlockPos.class, new BlockPosKeySerializer());
        module.addKeyDeserializer(BlockPos.class, new BlockPosKeyDeserializer());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);*/
        // endregion

        ApiGetter.vsApi.collisionPersistEvent.on(EventConsumer { collisionEvent: CollisionEvent, registeredListener: RegisteredListener ->
            VSEvents.collide(
                collisionEvent,
                registeredListener
            )
        })

        ApiGetter.vsApi.registerBlockStateEvent.on((Consumer { registerBlockStateEvent: RegisterBlockStateEvent -> }))

        val registration = ApiGetter.vsApi
            .newAttachmentRegistrationBuilder(BeltStorageForceInducer::class.java)
            .useJacksonSerializer()
            .build()

        ApiGetter.vsApi.registerAttachment<BeltStorageForceInducer>(registration)

        ApiGetter.vsApi.shipLoadEvent.on(Consumer { event: ShipLoadEvent ->
            event.ship.setAttachment<BeltStorageForceInducer>(BeltStorageForceInducer())
        })
    }

    companion object {
        const val MOD_ID: String = "counterclockwise"
    }
}

fun BlockPos.centerJOMLD(): Vector3d {
    return this.center.toJOML()
}