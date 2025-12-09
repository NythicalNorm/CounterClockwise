package com.blockninja.counterclockwise.ships

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.valkyrienskies.core.internal.joints.VSJointId

open class ConstraintGroup(open val constraintIds: Iterable<VSJointId>) {
    open val compoundTag : CompoundTag
        get() {
            val tag = CompoundTag()
            val list = ListTag()
            constraintIds.forEach {
                val tmp = CompoundTag()
                tmp.putInt("constraintId", it)
                list.add(tmp)
            }
            tag.put("constraintGroup", list)
            return tag
        }

    companion object {
        @JvmStatic
        fun createFromTag(tag: CompoundTag): ConstraintGroup {
            return ConstraintGroup(getConstraintsFromTag(tag))
        }

        @JvmStatic
        fun getConstraintsFromTag(tag: CompoundTag): Iterable<VSJointId> {
            return (tag.get("constraintGroup") as ListTag).map { (it as CompoundTag).getInt("constraintId") }
        }
    }
}