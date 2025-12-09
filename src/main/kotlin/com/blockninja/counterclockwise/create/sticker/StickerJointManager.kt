package com.blockninja.counterclockwise.create.sticker

import com.blockninja.counterclockwise.centerJOMLD
import com.blockninja.counterclockwise.ships.JointGroup
import com.blockninja.counterclockwise.ships.JointManager
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluids
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.Vector3fc
import org.joml.Vector3i
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.LoadedShip
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.core.internal.joints.VSFixedJoint
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque
import org.valkyrienskies.core.internal.joints.VSJointPose
import org.valkyrienskies.core.internal.world.VsiServerShipWorld
import org.valkyrienskies.core.util.expand
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.isTickingChunk
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.squaredDistanceBetweenInclShips
import org.valkyrienskies.mod.common.toWorldCoordinates
import org.valkyrienskies.mod.common.util.DimensionIdProvider
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.util.logger
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.toVector3d
import java.util.function.Consumer

open class StickerJointManager(val level: ServerLevel, val ship: ServerShip?, val blockPos: BlockPos, val getFacing: () -> Direction, override val core: VsiServerShipWorld = level.shipObjectWorld) : JointManager(level.dimensionId, core) {
    open val bodyId: ShipId?
        get() = this.ship?.id ?: core.dimensionToGroundBodyIdImmutable[level.dimensionId]
    open val checkPos: Vector3dc
        get() = this.blockPos.centerJOMLD().add(getFacing().normal.toVector3d().mul(0.5625))

    @OptIn(PhysTickOnly::class)
    open fun createStickerJoint() {
        if (this.bodyId == null) return
        val checkPos = this.level.toWorldCoordinates(Vector3d(this.checkPos))
        var shouldPlaySound = false
        this.level.transformFromWorldToNearbyLoadedShipsAndWorld(AABBd(checkPos, checkPos).expand(0.25)) { aabb ->
            val pos = aabb.center(Vector3d())

            val otherShip = level.getShipManagingPos2(pos)
            val otherId = otherShip?.id ?: level.shipObjectWorld.dimensionToGroundBodyIdImmutable[level.dimensionId] ?: return@transformFromWorldToNearbyLoadedShipsAndWorld
            if (otherId == this.bodyId) return@transformFromWorldToNearbyLoadedShipsAndWorld

            if (isAirOrFluid(level.getBlockState(pos.toBlockPos()))) return@transformFromWorldToNearbyLoadedShipsAndWorld


            val otherPos = Vector3d(pos)
            otherShip?.transform?.worldToShip?.transformPosition(otherPos)


            val localPos0 : Vector3dc?
            val localPos1 : Vector3dc?
            when {
                this.ship == null -> {
                    localPos0 = otherShip!!.transform.positionInWorld
                    localPos1 = otherShip.transform.positionInShip
                }
                otherShip == null -> {
                    localPos0 = this.ship.transform.positionInShip
                    localPos1 = this.ship.transform.positionInWorld
                }
                else -> {
                    localPos0 = this.ship.transform.worldToShip.transformPosition(otherShip.transform.positionInWorld, Vector3d())!!
                    localPos1 = otherShip.transform.positionInShip
                }
            }

            val compliance = 1e-10f//VSAdditionConfig.SERVER.create.stickerCompliance
            val maxForce = 1e10f//VSAdditionConfig.SERVER.create.stickerMaxForce

            val jointPos0 = VSJointPose(localPos0!!, (ship?.transform?.shipToWorldRotation ?: Quaterniond()).invert(Quaterniond()))
            val jointPos1 = VSJointPose(localPos1!!, (otherShip?.transform?.shipToWorldRotation ?: Quaterniond()).invert(Quaterniond()))
            val maxJointForce = VSJointMaxForceTorque(maxForce, maxForce)

            val fixedJoint = VSFixedJoint(bodyId!!, jointPos0, otherId, jointPos1, maxJointForce)

            this.addJointGroup(
                StickerJointGroup(
                    listOf(
                        fixedJoint.createJoint().get() ?: return@transformFromWorldToNearbyLoadedShipsAndWorld
                    ), pos.toBlockPos()
                )
            )
            shouldPlaySound = true
        }
        if (!level.isClientSide() && shouldPlaySound) {
            // TODO: re-add this networking stuff
            //StickerSoundPacketS2CPacket(blockPos, true).sendToPlayers(level.players())
        }
    }

    override fun onRemoveAllJointGroups(map: Int2ObjectOpenHashMap<JointGroup>) {
        if (map.size > 0) {
            // TODO: re-add this networking stuff
            //StickerSoundPacketS2CPacket(blockPos, false).sendToPlayers(level.players())
        }
    }

    open fun checkStickerJoint() {
        this.jointGroups.forEach { (id, group) ->
            group as StickerJointGroup
            val blockPos = group.blockPos
            if (this.level.isTickingChunk(ChunkPos(blockPos)) && isAirOrFluid(this.level.getBlockState(blockPos)) || this.level.squaredDistanceBetweenInclShips(this.blockPos, blockPos) >= 128.0) {
                this.removeJointGroup(id)
            }
        }
    }

    override fun getCompoundTag(group: JointGroup): CompoundTag {
        return (group as StickerJointGroup).compoundTag
    }

    override fun createFormCompoundTag(tag: CompoundTag): JointGroup {
        return StickerJointGroup.createFromTag(tag)
    }

    companion object {
        val logger by logger()
        fun isAirOrFluid(state: BlockState): Boolean {
            return state.isAir || state.fluidState != Fluids.EMPTY.defaultFluidState()
        }
    }
}

private fun Level.squaredDistanceBetweenInclShips(inputPos1: Any, inputPos2: Any) : Double {
    val vector1 = toVector3d(inputPos1)
    val vector2 = toVector3d(inputPos2)
    return this.squaredDistanceBetweenInclShips(vector1.x(),vector1.y(), vector1.z(), vector2.x(), vector2.y(), vector2.z())
}

private fun toVector3d(inputPos: Any) : Vector3d {
    return when (inputPos) {
        is Vec3i -> inputPos.centerJOMLD()
        is Position -> inputPos.toJOML()
        is Vector3i -> inputPos.centerJOMLD()
        is Vector3fc -> Vector3d(inputPos.x().toDouble(), inputPos.y().toDouble(), inputPos.z().toDouble())
        is Vector3dc -> Vector3d(inputPos)
        else -> throw IllegalArgumentException("Unsupported type: ${inputPos::class.simpleName}")
    }
}

private fun Vec3i.centerJOMLD(): Vector3d {
    return Vector3d(this.x + 0.5, this.y + 0.5, this.z + 0.5)
}

private fun Vector3i.centerJOMLD(): Vector3d {
    return Vector3d(this.x + 0.5, this.y + 0.5, this.z + 0.5)
}

private fun Level.transformFromWorldToNearbyLoadedShipsAndWorld(aabb: AABBdc, cb: Consumer<AABBdc>) {
    cb.accept(aabb)
    val tmpAABB = AABBd()
    getLoadedShipsIntersecting(aabb).forEach { ship ->
        cb.accept(tmpAABB.set(aabb).transform(ship.worldToShip))
    }
}

private fun Level?.getLoadedShipsIntersecting(aabb: AABBdc): Iterable<LoadedShip> {
    return this.shipObjectWorld.loadedShips.getIntersecting(aabb).filter { it.chunkClaimDimension == (this as DimensionIdProvider).dimensionId }
}

private fun Level?.getShipManagingPos2(position: Any) : Ship? {
    val pos = toVector3d(position)
    return this.getShipManagingPos(pos)
}

private fun Vector3dc.toBlockPos(): BlockPos {
    return BlockPos(Mth.floor(this.x()), Mth.floor(this.y()), Mth.floor(this.z()))
}
