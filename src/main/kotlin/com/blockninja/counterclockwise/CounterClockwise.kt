package com.blockninja.counterclockwise

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer
import net.minecraftforge.fml.common.Mod
import org.valkyrienskies.core.api.VsBeta
import org.valkyrienskies.core.api.event.EventConsumer
import org.valkyrienskies.core.api.event.RegisteredListener
import org.valkyrienskies.core.api.events.CollisionEvent
import org.valkyrienskies.core.api.events.ShipLoadEvent
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.mod.api.events.RegisterBlockStateEvent
import org.valkyrienskies.mod.common.vsCore
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
        VSEvents.api.collisionPersistEvent.on(EventConsumer { collisionEvent: CollisionEvent, registeredListener: RegisteredListener ->
            VSEvents.collide(
                collisionEvent,
                registeredListener
            )
        })
        VSEvents.api.registerBlockStateEvent.on((Consumer { registerBlockStateEvent: RegisterBlockStateEvent -> }))

        val registration = VSEvents.api
            .newAttachmentRegistrationBuilder<BeltStorageForceInducer>(BeltStorageForceInducer::class.java)
            .useJacksonSerializer()
            .build()

        VSEvents.api.registerAttachment<BeltStorageForceInducer>(registration)

        VSEvents.api.shipLoadEvent.on(Consumer { event: ShipLoadEvent ->
            event.ship.setAttachment<BeltStorageForceInducer>(BeltStorageForceInducer())
        })
    }

    private fun doSomethihg() {

    }

    companion object {
        const val MOD_ID: String = "counterclockwise"
    }
}