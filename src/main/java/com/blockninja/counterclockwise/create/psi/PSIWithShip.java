package com.blockninja.counterclockwise.create.psi;

import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.joml.primitives.AABBd;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

// WIP
// Taken from VS addition, converted to java, slightly tweaked.
// TODO: find other PSI is NOT IMPLEMENTED YET here because I don't understand the kotlin
public class PSIWithShip {
    public PortableStorageInterfaceBlockEntity be;
    public PSIWithShip other = null;
    public boolean isPassive = false;
    public LerpedFloat connectionAnimation = LerpedFloat.linear().startWithValue(0.0);
    public int transferTimer = 0;
    public Vector3d latestOtherPos = new Vector3d();
    public double latestDistance = 0.0;


    public PSIWithShip(PortableStorageInterfaceBlockEntity be) {
        this.be = be;
    }

    public void startTransferringTo(PSIWithShip otherController) {
        if (this.other == otherController) return;
        otherController.isPassive = true;

        this.other = otherController;
        otherController.other = this;

        startConnecting();
        otherController.startConnecting();

        this.be.notifyUpdate();
    }

    public void stopTransferring() {
        this.other = null;
        if (this.be.getLevel() != null) {
            this.be.getLevel().updateNeighborsAt(be.getBlockPos(), be.getBlockState().getBlock());
        }
    }

    public boolean canTransfer() {
        if (!isConnected()) {
            stopTransferring();
        }
        return this.other!= null && this.isConnected() && this.other.isConnected();
    }

    public void tick(CallbackInfo ci) {
        if (((IPSIWithShipBehavior) be).getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
            boolean wasConnected = isConnected();
            if (!isPassive) {
                if (!be.isPowered() && this.other == null) {
                    findOtherController();
                }
            }
            if (this.other != null && !this.other.isConnected()) {
                stopTransferring();
            }
            updateDistance();

            if (isConnected() != wasConnected) {
                be.setChanged();
            }
            ci.cancel();
        } else {
            if (this.other != null) {
                stopTransferring();
            }
        }
    }

    public float getExtensionDistance(float partialTicks) {
        if (be.getLevel() == null) return 0f;

        if (other != null) {
            Vector3d pos0 = getConnectionPos(be.getLevel(), this.be);
            Vector3d pos1 = getConnectionPos(be.getLevel(), other.be);
            latestDistance = pos0.distance(pos1);
        }

        return (float) (Math.pow(connectionAnimation.getValue(partialTicks), 2.0F) * latestDistance / 2);
    }

    public Vector3d getConnectionCenter() {
        if (be.getLevel() == null) return new Vector3d();

        Vector3d pos0 = getConnectionPos(be.getLevel(), this.be);
        if (other != null) {
            latestOtherPos.set(getConnectionPos(be.getLevel(), other.be));
        }

        return pos0.lerp(latestOtherPos, 0.5);
    }

    public void updateDistance() {
        double progress = 0.0;
        int timeUnit = getTransferTimeout();
        int animation = PortableStorageInterfaceBlockEntity.ANIMATION;
        if (isConnected()) {
            progress = 1.0;
        } else if (transferTimer >= timeUnit + animation) {
            progress = Mth.lerp((transferTimer - timeUnit - animation) / (float) animation, 1f, 0f);
        } else if (transferTimer < animation) {
            progress = Mth.lerp(transferTimer / (float) animation, 0f, 1f);
        }
        this.connectionAnimation.setValue(progress);
    }

    public void findOtherController() {
        if (be.getLevel() == null) return;

        BlockPos blockPos = be.getBlockPos();
        BlockState blockState = be.getBlockState();
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(be.getLevel(), blockPos);

        Vector3d center = VectorConversionsMCKt.toJOML(blockPos.getCenter());

        AABBd aabb = new AABBd(1.5, 1.5, 1.5, -1.5, -1.5, -1.5);
        aabb.translate(VectorConversionsMCKt.toJOMLD(blockState.getValue(PortableStorageInterfaceBlock.FACING).getNormal()).mul(2.0).add(center));

        AABBd worldAabb = VSGameUtilsKt.transformAabbToWorld(be.getLevel(), new AABBd(aabb));

        // TODO: un-kotlin
        /*matrices = VSGameUtilsKt.getShipsIntersecting(be.getLevel(), worldAabb).
                .filter(shipObj -> !shipObj.equals(ship))
                .map(shipObj -> shipObj.transform.worldToShip)
                .collect(Collectors.toList());

        fun findPSI(aabb: AABBdc) : Boolean {
            val aabbi = aabb.toAABBi()
            val mutableBlockPos = BlockPos.MutableBlockPos()
            for (x in aabbi.minX .. aabbi.maxX) {
                for (y in aabbi.minY .. aabbi.maxY) {
                    for (z in aabbi.minZ .. aabbi.maxZ) {
                        mutableBlockPos.set(x, y, z)
                        val entity = level.getBlockEntity(mutableBlockPos)
                        if (entity is PortableStorageInterfaceBlockEntity && !entity.isPowered && entity is IPSIWithShipBehavior && entity.workingMode.get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && entity.controller != null && entity.controller::class == this::class) {
                            startTransferringTo(entity.controller)
                            return true
                        }
                    }
                }
            }
            return false
        }

        if (matrices.isNotEmpty()) {
            val tmpAabb = AABBd()
            matrices.forEach { matrix ->
                    tmpAabb.set(worldAabb)
                tmpAabb.transform(matrix)
                if (findPSI(tmpAabb)) return
            }
        }

        findPSI(worldAabb)
        */
    }

    public void startConnecting() {
        transferTimer = 6 + PortableStorageInterfaceBlockEntity.ANIMATION * 2;
    }

    public void onContentTransferred() {
        int timeUnit = getTransferTimeout();
        transferTimer = timeUnit + PortableStorageInterfaceBlockEntity.ANIMATION;
        be.award(AllAdvancements.PSI);
        be.sendData();
    }

    public int getTransferTimeout() {
        return AllConfigs.server().logistics.psiTimeout.get();
    }

    public boolean isConnected() {
        if (be.isRemoved()) return false;

        // Redstone control
        if (!be.isPowered()) return false;


        boolean thisConnected = ((IPSIWithShipBehavior) be).getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP;

        if (other == null || other.be.isRemoved()) return false;

        // Redstone control
        if (!other.be.isPowered()) return false;

        boolean otherConnected = ((IPSIWithShipBehavior) other.be).getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP;

        return thisConnected && otherConnected;
    }

    public static Vector3d getConnectionPos(Level level, PortableStorageInterfaceBlockEntity be) {
        Vector3d center = VectorConversionsMCKt.toJOML(be.getBlockPos().getCenter());
        Ship ship = VSGameUtilsKt.getShipObjectManagingPos(level, center);

        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(PortableStorageInterfaceBlock.FACING);

        Vector3d pos = VectorConversionsMCKt.toJOML(be.getBlockPos().getCenter()).add(facing.step().mul(0.5F));

        if (ship == null) return pos;

        if (ship instanceof ClientShip clientShip) {
            clientShip.getRenderTransform().getShipToWorld().transformPosition(pos);
        } else {
            ship.getTransform().getShipToWorld().transformPosition(pos);
        }

        return pos;
    }
}