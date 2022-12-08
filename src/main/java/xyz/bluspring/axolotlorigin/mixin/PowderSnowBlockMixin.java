package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.bluspring.axolotlorigin.powers.AxolotlPowerTypes;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {
    @Inject(at = @At("HEAD"), method = "canWalkOnPowderSnow", cancellable = true)
    private static void allowWalkingOnPowderedSnow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (AxolotlPowerTypes.INSTANCE.getAntiFreeze().isActive(entity)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setInPowderSnow(Z)V", shift = At.Shift.AFTER), method = "onEntityCollision")
    public void preventFreeze(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!AxolotlPowerTypes.INSTANCE.getAntiFreeze().isActive(entity)) {
            entity.setInPowderSnow(false);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;slowMovement(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Vec3d;)V", shift = At.Shift.AFTER), method = "onEntityCollision")
    public void axolotl$noFreezeSlowdown(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!AxolotlPowerTypes.INSTANCE.getAntiFreeze().isActive(entity)) {
            // This should be a bit more supported in terms of preventing slowed movement
            // and mod compatibility
            entity.slowMovement(state, new Vec3d(1 / 0.8999999761581421, 1 / 1.5, 1 / 0.8999999761581421));
        }
    }
    /*@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;slowMovement(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Vec3d;)V"), method = "onEntityCollision")
    public void noFreezeSlowdown(Entity instance, BlockState state, Vec3d multiplier) {
        if (!AxolotlPowerTypes.INSTANCE.getAntiFreeze().isActive(instance)) {
            instance.slowMovement(state, multiplier);
        }
    }*/
}
