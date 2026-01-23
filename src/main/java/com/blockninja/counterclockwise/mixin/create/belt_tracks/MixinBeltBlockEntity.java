package com.blockninja.counterclockwise.mixin.create.belt_tracks;

import com.blockninja.counterclockwise.ships.BeltStorageForceInducer;
import com.blockninja.counterclockwise.ships.WheelEntityForceInducer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.common.aliasing.qual.Unique;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.impl.game.ships.ShipInertiaDataImpl;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.core.internal.physics.PhysicsEntityData;
import org.valkyrienskies.core.internal.physics.PhysicsEntityServer;
import org.valkyrienskies.core.internal.physics.RigidBodyDefaults;
import org.valkyrienskies.core.internal.physics.VSSphereCollisionShapeData;
import org.valkyrienskies.mod.api.BlockEntityPhysicsListener;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(BeltBlockEntity.class)
public abstract class MixinBeltBlockEntity extends BlockEntity implements BlockEntityPhysicsListener {

    @Shadow
    public abstract float getBeltMovementSpeed();

    @Shadow
    public abstract float getDirectionAwareBeltMovementSpeed();

    @Shadow
    public abstract Direction getMovementFacing();

    @Unique
    private Long entityId;

    public MixinBeltBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Inject(
            method = "tick",
            at = @At("RETURN"),
            remap = false
    )
    private void injectTick(CallbackInfo ci) {
        if (!AllBlocks.BELT.has(level.getBlockState(worldPosition)))
            return;

        //System.out.println(getDirectionAwareBeltMovementSpeed());

        if (level instanceof ServerLevel serverLevel) {
            /*LoadedServerShip ship = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, worldPosition);
            if (ship == null) return;

            BeltStorageForceInducer inducer = BeltStorageForceInducer.getOrCreate(ship);
            inducer.beltLocations.put(worldPosition.asLong(), new BeltStorageForceInducer.BeltData(
                    getBlockState().getValue(BeltBlock.SLOPE),
                    getBlockState().getValue(BeltBlock.PART),
                    getBeltMovementSpeed(),
                    getMovementFacing()
            ));*/

            if (entityId == null) {
                long newId = VSGameUtilsKt.getShipObjectWorld(serverLevel).allocateShipId(VSGameUtilsKt.getDimensionId(serverLevel));

                PhysicsEntityServer entity = VSGameUtilsKt.getShipObjectWorld(serverLevel).createPhysicsEntity(new PhysicsEntityData(
                        newId,
                        ShipTransformImpl.Companion.create(VectorConversionsMCKt.toJOML(worldPosition.getCenter()), //pos
                                new Vector3d(), // com pos
                                new Quaterniond(), // ship rot
                                new Vector3d(1.0, 1.0, 1.0) // scale
                        ),
                        new ShipInertiaDataImpl(new Vector3d(), 1.0, sphereInertiaTensor(1.0, 2.0)),
                        new Vector3d(), new Vector3d(),
                        new VSSphereCollisionShapeData(2.0),
                        RigidBodyDefaults.DEFAULT_COLLISION_MASK,
                        RigidBodyDefaults.DEFAULT_STATIC_FRICTION_COEFFICIENT,
                        RigidBodyDefaults.DEFAULT_DYNAMIC_FRICTION_COEFFICIENT,
                        RigidBodyDefaults.DEFAULT_RESTITUTION_COEFFICIENT,
                        false
                ), VSGameUtilsKt.getDimensionId(serverLevel));

                // TODO: use a revolute joint to hold the physics entity in place
                // then just use the phys tick listener (possibly in this BEPL instead of an attachment)
                // to apply a rotational force based on rpm.
                WheelEntityForceInducer inducer = new WheelEntityForceInducer();
                ConcurrentHashMap map = new ConcurrentHashMap();
                map.put(0, new Vector3d(0.0, 5.0, 0.0));
                inducer.setRotations(map);

                entity.getPhysicsListeners().add(inducer);

                entityId = newId;
            }
        }

    }

    @Override
    public void physTick(@Nullable PhysShip physShip, @NotNull PhysLevel physLevel) {

    }

    private static Matrix3d sphereInertiaTensor(double mass, double radius) {
        Matrix3d mat = new Matrix3d();
        mat.m00 = (2.0/5.0) * mass * radius * radius;
        mat.m11 = (2.0/5.0) * mass * radius * radius;
        mat.m22 = (2.0/5.0) * mass * radius * radius;
        return mat;
    }

    @Override
    public void setDimension(@NotNull String s) {

    }
}
