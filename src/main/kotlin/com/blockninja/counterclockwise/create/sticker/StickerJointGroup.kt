package com.blockninja.counterclockwise.create.sticker

import com.blockninja.counterclockwise.ships.JointGroup
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import org.valkyrienskies.core.internal.joints.VSJointId

open class StickerJointGroup(override val jointIds: Iterable<VSJointId>, open val blockPos: BlockPos) : JointGroup(jointIds) {
    override val compoundTag : CompoundTag
        get() {
            val tag = super.compoundTag
            tag.putLong("blockPos", blockPos.asLong())
            return tag
        }

    companion object {
        @JvmStatic
        fun createFromTag(tag: CompoundTag): StickerJointGroup {
            return StickerJointGroup(getConstraintsFromTag(tag), BlockPos.of(tag.getLong("blockPos")))
        }
    }
}