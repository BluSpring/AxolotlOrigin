package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.axolotlorigin.AxolotlOrigin;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {
    @Shadow public abstract void stop();

    @Shadow @Final protected PathAwareEntity mob;

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void axolotl_preventMobAttack(CallbackInfo ci) {
        if (this.mob.getTarget() != null && AxolotlOrigin.Companion.getFakeDeathActiveSince().containsKey(this.mob.getTarget().getUuid())) {
            this.stop();
            ci.cancel();
        }
    }
}
