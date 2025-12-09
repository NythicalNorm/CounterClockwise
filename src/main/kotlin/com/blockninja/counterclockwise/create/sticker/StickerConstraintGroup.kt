package com.blockninja.counterclockwise.create.sticker

import com.blockninja.counterclockwise.ships.ConstraintGroup
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import org.valkyrienskies.core.internal.joints.VSJointId

open class StickerConstraintGroup(override val constraintIds: Iterable<VSJointId>, open val blockPos: BlockPos) : ConstraintGroup(constraintIds) {
    override val compoundTag : CompoundTag
        get() {
            val tag = super.compoundTag
            tag.putLong("blockPos", blockPos.asLong())
            return tag
        }

    companion object {
        @JvmStatic
        fun createFromTag(tag: CompoundTag): StickerConstraintGroup {
            return StickerConstraintGroup(getConstraintsFromTag(tag), BlockPos.of(tag.getLong("blockPos")))
        }
    }
}