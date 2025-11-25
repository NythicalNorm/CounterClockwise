package com.blockninja.counterclockwise;

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import net.minecraftforge.fml.common.Mod;
import org.valkyrienskies.core.api.attachment.AttachmentRegistration;
import org.valkyrienskies.mod.api.ValkyrienSkies;

@Mod(CounterClockwise.MOD_ID)
public class CounterClockwise {
    public static final String MOD_ID = "counterclockwise";

    public CounterClockwise() {
        ValkyrienSkies.api().getCollisionPersistEvent().on(VSEvents::collide);

        AttachmentRegistration<BeltStorageForceInducer> registration = ValkyrienSkies.api()
                .newAttachmentRegistrationBuilder(BeltStorageForceInducer.class)
                .useLegacySerializer()
                .build();

        ValkyrienSkies.api().registerAttachment(registration);

        ValkyrienSkies.api().getShipLoadEvent().on((event) -> {
            event.getShip().setAttachment(new BeltStorageForceInducer());
        });
    }
}
