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
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.core.internal.joints.VSFixedJoint
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque
import org.valkyrienskies.core.internal.joints.VSJointPose
import org.valkyrienskies.core.internal.world.VsiServerShipWorld
import org.valkyrienskies.core.util.expand
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.getLoadedShipManagingPos
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import org.valkyrienskies.mod.common.isTickingChunk
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.squaredDistanceBetweenInclShips
import org.valkyrienskies.mod.common.toWorldCoordinates
import org.valkyrienskies.mod.common.util.DimensionIdProvider
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.util.toJOMLD
import org.valkyrienskies.mod.common.util.toMinecraft
import org.valkyrienskies.mod.util.logger
import java.util.function.Consumer

open class StickerJointManager(val level: ServerLevel, val ship: ServerShip?, val blockPos: BlockPos, val getFacing: () -> Direction, override val core: VsiServerShipWorld = level.shipObjectWorld) : JointManager(level.dimensionId, core) {
    open val bodyId: ShipId?
        get() = this.ship?.id ?: core.dimensionToGroundBodyIdImmutable[level.dimensionId]
    open val checkPos: Vector3dc
        get() = this.blockPos.centerJOMLD().add(getFacing().normal.toJOMLD().mul(0.5625))

    @OptIn(PhysTickOnly::class, GameTickOnly::class)
    open fun createStickerJoint() {
        if (this.bodyId == null) return
        val checkPos = this.level.toWorldCoordinates(Vector3d(this.checkPos))
        var shouldPlaySound = false
        this.level.transformFromWorldToNearbyLoadedShipsAndWorld(AABBd(checkPos, checkPos).expand(0.25)) { aabb ->
            val pos = aabb.center(Vector3d())

            val otherShip = level.getLoadedShipManagingPos(pos)
            val otherId = otherShip?.id ?: level.shipObjectWorld.dimensionToGroundBodyIdImmutable[level.dimensionId] ?: return@transformFromWorldToNearbyLoadedShipsAndWorld
            if (otherId == this.bodyId) return@transformFromWorldToNearbyLoadedShipsAndWorld

            if (isAirOrFluid(level.getBlockState(BlockPos.containing(pos.toMinecraft())))) return@transformFromWorldToNearbyLoadedShipsAndWorld


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

            val compliance = 1.0e-10//VSAdditionConfig.SERVER.create.stickerCompliance
            val maxForce = 1e10f//VSAdditionConfig.SERVER.create.stickerMaxForce

            val jointPos0 = VSJointPose(localPos0, (ship?.transform?.shipToWorldRotation ?: Quaterniond()).invert(Quaterniond()))
            val jointPos1 = VSJointPose(localPos1, (otherShip?.transform?.shipToWorldRotation ?: Quaterniond()).invert(Quaterniond()))
            val maxJointForce = VSJointMaxForceTorque(maxForce, maxForce)

            val fixedJoint = VSFixedJoint(bodyId!!, jointPos0, otherId, jointPos1, null, compliance)

            this.addJointGroup(
                StickerJointGroup(
                    listOf(
                        fixedJoint.createJoint().get() ?: return@transformFromWorldToNearbyLoadedShipsAndWorld
                    ), BlockPos.containing(pos.toMinecraft())
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
            if (this.level.isTickingChunk(ChunkPos(blockPos)) && isAirOrFluid(this.level.getBlockState(blockPos)) || this.level.squaredDistanceBetweenInclShips(this.blockPos.center.toJOML(), blockPos.center.toJOML()) >= 128.0) {
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

private fun Level.squaredDistanceBetweenInclShips(vector1: Vector3d, vector2: Vector3d) : Double {
    return this.squaredDistanceBetweenInclShips(vector1.x(),vector1.y(), vector1.z(), vector2.x(), vector2.y(), vector2.z())
}

private fun Level.transformFromWorldToNearbyLoadedShipsAndWorld(aabb: AABBdc, cb: Consumer<AABBdc>) {
    cb.accept(aabb)
    val tmpAABB = AABBd()
    getLoadedShipsIntersecting(aabb).forEach { ship ->
        cb.accept(tmpAABB.set(aabb).transform(ship.worldToShip))
    }
}

private fun Level.getLoadedShipsIntersecting(aabb: AABBdc): Iterable<LoadedShip> {
    return this.shipObjectWorld.loadedShips.getIntersecting(aabb, this.dimensionId)
}
