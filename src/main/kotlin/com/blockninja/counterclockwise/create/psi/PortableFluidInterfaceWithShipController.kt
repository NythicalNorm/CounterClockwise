package com.blockninja.counterclockwise.create.psi

import com.blockninja.counterclockwise.mixinducks.create.portable_interface.IPSIWithShipBehavior
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.templates.FluidTank

open class PortableFluidInterfaceWithShipController(be: PortableFluidInterfaceBlockEntity) : ForgePortableStorageInterfaceWithShipController(be) {
    var capability: LazyOptional<IFluidHandler>? = null

    fun createEmptyHandler(behavior: IPSIWithShipBehavior): LazyOptional<IFluidHandler> {
        return LazyOptional.of { InterfaceFluidHandler(FluidTank(0), behavior) }
    }

    override fun startTransferringTo(otherController: PSIWithShip) {
        if (otherController !is PortableFluidInterfaceWithShipController || this == otherController || this.other == otherController) {
            return
        }
        val oldCap0 = capability
        val oldCap1 = otherController.capability
        capability = LazyOptional.of { InterfaceFluidHandler(FluidTank(5), be as IPSIWithShipBehavior) }
        otherController.capability = capability
        oldCap0?.invalidate()
        oldCap1?.invalidate()

        super.startTransferringTo(otherController)
    }

    override fun stopTransferring() {
        val oldCap = capability
        capability = createEmptyHandler(be as IPSIWithShipBehavior)
        oldCap?.invalidate()
        other?.let { it.isPassive = false }
        super.stopTransferring()
    }

    override fun invalidateCapability() {
        capability?.invalidate()
    }
}