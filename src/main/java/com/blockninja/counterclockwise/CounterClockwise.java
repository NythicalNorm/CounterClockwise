package com.blockninja.counterclockwise;

import com.blockninja.counterclockwise.jackson.BlockPosKeyDeserializer;
import com.blockninja.counterclockwise.jackson.BlockPosKeySerializer;
import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.common.Mod;
import org.valkyrienskies.core.api.attachment.AttachmentRegistration;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@Mod(CounterClockwise.MOD_ID)
public class CounterClockwise {
    public static final String MOD_ID = "counterclockwise";

    public CounterClockwise() {

        // region Register BlockPos so it can be serialized as a map key
        /*SimpleModule module = new SimpleModule();
        module.addKeySerializer(BlockPos.class, new BlockPosKeySerializer());
        module.addKeyDeserializer(BlockPos.class, new BlockPosKeyDeserializer());
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);*/
        // endregion

        ValkyrienSkies.api().getCollisionPersistEvent().on(VSEvents::collide);

        AttachmentRegistration<BeltStorageForceInducer> registration = ValkyrienSkies.api()
                .newAttachmentRegistrationBuilder(BeltStorageForceInducer.class)
                .useJacksonSerializer()
                .build();

        ValkyrienSkies.api().registerAttachment(registration);

        ValkyrienSkies.api().getShipLoadEvent().on((event) -> {
            event.getShip().setAttachment(new BeltStorageForceInducer());
        });
    }
}
