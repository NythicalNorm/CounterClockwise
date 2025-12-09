package com.blockninja.counterclockwise.ships

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.valkyrienskies.core.internal.joints.VSJointId

open class JointGroup(open val jointIds: Iterable<VSJointId>) {
    open val compoundTag : CompoundTag
        get() {
            val tag = CompoundTag()
            val list = ListTag()
            jointIds.forEach {
                val tmp = CompoundTag()
                tmp.putInt("jointId", it)
                list.add(tmp)
            }
            tag.put("jointGroup", list)
            return tag
        }

    companion object {
        @JvmStatic
        fun createFromTag(tag: CompoundTag): JointGroup {
            return JointGroup(getConstraintsFromTag(tag))
        }

        @JvmStatic
        fun getConstraintsFromTag(tag: CompoundTag): Iterable<VSJointId> {
            return (tag.get("jointGroup") as ListTag).map { (it as CompoundTag).getInt("jointId") }
        }
    }
}