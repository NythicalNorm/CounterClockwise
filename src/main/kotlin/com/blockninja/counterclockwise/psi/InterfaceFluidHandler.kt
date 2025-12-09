package com.blockninja.counterclockwise.psi

import com.blockninja.counterclockwise.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction

class InterfaceFluidHandler(private val wrapped: IFluidHandler, private val behavior: IPSIWithShipBehavior) : IFluidHandler {
    override fun getTanks(): Int {
        return wrapped.tanks
    }

    override fun getFluidInTank(tank: Int): FluidStack {
        return wrapped.getFluidInTank(tank)
    }

    override fun getTankCapacity(tank: Int): Int {
        return wrapped.getTankCapacity(tank)
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        return wrapped.isFluidValid(tank, stack)
    }

    override fun fill(resource: FluidStack, action: FluidAction): Int {
        if (!behavior.controller.isConnected()) return 0
        val fill = wrapped.fill(resource, action)
        if (fill > 0 && action.execute()) this.keepAlive()
        return fill
    }

    override fun drain(resource: FluidStack, action: FluidAction): FluidStack {
        if (!behavior.controller.canTransfer()) return FluidStack.EMPTY
        val drain = wrapped.drain(resource, action)
        if (!drain.isEmpty && action.execute()) this.keepAlive()
        return drain
    }

    override fun drain(maxDrain: Int, action: FluidAction): FluidStack {
        if (!behavior.controller.canTransfer()) return FluidStack.EMPTY
        val drain = wrapped.drain(maxDrain, action)
        if (!drain.isEmpty && action.execute()) this.keepAlive()
        return drain
    }

    private fun keepAlive() {
        behavior.controller.onContentTransferred()
        behavior.controller.other?.onContentTransferred()
    }
}
