package com.blockninja.counterclockwise.ships

import com.blockninja.counterclockwise.CounterClockwise
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.nbt.CompoundTag
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.core.api.world.properties.DimensionId
import org.valkyrienskies.core.internal.joints.VSJoint
import org.valkyrienskies.core.internal.joints.VSJointId
import org.valkyrienskies.core.internal.world.VsiServerShipWorld
import org.valkyrienskies.mod.common.ValkyrienSkiesMod.getOrCreateGTPA
import org.valkyrienskies.physics_api.joints.JointAndId
import java.util.concurrent.CompletableFuture

@OptIn(GameTickOnly::class)
abstract class ConstraintManager(open val dimensionId: DimensionId, open val core: VsiServerShipWorld) {

    val constraintGroups = Int2ObjectOpenHashMap<ConstraintGroup>()
    val forceApplier = getOrCreateGTPA(this.dimensionId)


    fun addConstraintGroup(group: ConstraintGroup) {
        groupIdCounter += 1
        this.constraintGroups[groupIdCounter] = group
    }

    fun removeConstraintGroup(id: Int) {
        this.constraintGroups[id]?.let { this.onRemoveConstraintGroup(id, it) }
        this.constraintGroups.remove(id)
    }

    fun removeAllConstraintGroups() {
        this.onRemoveAllConstraintGroups(this.constraintGroups)
        this.constraintGroups.forEach { (id, group) ->
            this.onRemoveConstraintGroup(id, group)
        }
        this.constraintGroups.clear()
    }

    fun writeCompoundTag(tag: CompoundTag) {
        val nbt = CompoundTag()
        constraintGroups.forEach { (id, value) ->
            nbt.put(id.toString(), getCompoundTag(value))
        }
        tag.put(ID, nbt)
    }

    fun readCompoundTag(tag: CompoundTag) {
        val nbt = tag.getCompound(ID)
        nbt.allKeys.forEach {
            constraintGroups[it.toInt()] = createFormCompoundTag(nbt.getCompound(it))
        }
    }

    open fun getCompoundTag(group: ConstraintGroup): CompoundTag {
        return group.compoundTag
    }

    open fun createFormCompoundTag(tag: CompoundTag): ConstraintGroup {
        return ConstraintGroup.createFromTag(tag)
    }

    open fun onBodiesBeDeleted(core: VsiServerShipWorld, constraintAndId: JointAndId) {

    }

    open fun onBroken(core: VsiServerShipWorld, constraintAndId: JointAndId) {

    }

    fun removeConstraint(id: Int) {
        forceApplier.removeJoint(id)
    }

    @OptIn(PhysTickOnly::class)
    fun VSJoint.createConstraint() : CompletableFuture<VSJointId> {
        val future = CompletableFuture<VSJointId>()
        forceApplier.addJoint(this) { t ->
            future.complete(t)
        }
        return future
    }

    open fun onRemoveConstraintGroup(id: Int, group: ConstraintGroup) {
        group.constraintIds.forEach {
            this.removeConstraint(it)
        }
    }

    open fun onRemoveAllConstraintGroups(map: Int2ObjectOpenHashMap<ConstraintGroup>) {

    }

    companion object {
        const val ID = "${CounterClockwise.MOD_ID}_constraint_manager"
        var groupIdCounter = 0
    }
}
