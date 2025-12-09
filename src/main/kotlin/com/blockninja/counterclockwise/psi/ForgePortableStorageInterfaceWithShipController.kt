package com.blockninja.counterclockwise.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity

open class ForgePortableStorageInterfaceWithShipController(override var be: PortableStorageInterfaceBlockEntity) : PSIWithShip(be) {
    override fun onContentTransferred() {
        be.onContentTransferred()
    }
}