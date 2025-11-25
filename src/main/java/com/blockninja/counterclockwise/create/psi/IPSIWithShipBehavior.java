package com.blockninja.counterclockwise.create.psi;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.lang.Lang;

public interface IPSIWithShipBehavior {

    void setController(PSIWithShip controller);

    PSIWithShip getController();

    ScrollOptionBehaviour<WorkigMode> getWorkingMode();

    enum WorkigMode implements INamedIconOptions {

        ORIGINAL(AllIcons.I_MOVE_PLACE_RETURNED),
        WITH_SHIP(AllIcons.I_MOVE_PLACE),

        ;

        private String translationKey;
        private AllIcons icon;

        private WorkigMode(AllIcons icon) {
            this.icon = icon;
            translationKey = "vs_addition.working_mode." + Lang.asId(name());
        }

        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }

    }
}