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
import java.util.concurrent.CompletableFuture

@OptIn(GameTickOnly::class)
abstract class JointManager(open val dimensionId: DimensionId, open val core: VsiServerShipWorld) {

    val jointGroups = Int2ObjectOpenHashMap<JointGroup>()
    val forceApplier = getOrCreateGTPA(this.dimensionId)


    fun addJointGroup(group: JointGroup) {
        groupIdCounter += 1
        this.jointGroups[groupIdCounter] = group
    }

    fun removeJointGroup(id: Int) {
        this.jointGroups[id]?.let { this.onRemoveJointGroup(id, it) }
        this.jointGroups.remove(id)
    }

    fun removeAllJointGroups() {
        this.onRemoveAllJointGroups(this.jointGroups)
        this.jointGroups.forEach { (id, group) ->
            this.onRemoveJointGroup(id, group)
        }
        this.jointGroups.clear()
    }

    fun writeCompoundTag(tag: CompoundTag) {
        val nbt = CompoundTag()
        jointGroups.forEach { (id, value) ->
            nbt.put(id.toString(), getCompoundTag(value))
        }
        tag.put(ID, nbt)
    }

    fun readCompoundTag(tag: CompoundTag) {
        val nbt = tag.getCompound(ID)
        nbt.allKeys.forEach {
            jointGroups[it.toInt()] = createFormCompoundTag(nbt.getCompound(it))
        }
    }

    open fun getCompoundTag(group: JointGroup): CompoundTag {
        return group.compoundTag
    }

    open fun createFormCompoundTag(tag: CompoundTag): JointGroup {
        return JointGroup.createFromTag(tag)
    }

    fun removeJoint(id: Int) {
        forceApplier.removeJoint(id)
    }

    @OptIn(PhysTickOnly::class)
    fun VSJoint.createJoint() : CompletableFuture<VSJointId> {
        val future = CompletableFuture<VSJointId>()
        forceApplier.addJoint(this) { t ->
            future.complete(t)
        }
        return future
    }

    open fun onRemoveJointGroup(id: Int, group: JointGroup) {
        group.jointIds.forEach {
            this.removeJoint(it)
        }
    }

    open fun onRemoveAllJointGroups(map: Int2ObjectOpenHashMap<JointGroup>) {

    }

    companion object {
        const val ID = "${CounterClockwise.MOD_ID}_joint_manager"
        var groupIdCounter = 0
    }
}
