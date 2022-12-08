package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.AttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.axolotlorigin.AxolotlOrigin;

@Mixin(AttackGoal.class)
public abstract class AttackGoalMixin {
    @Shadow private LivingEntity target;

    @Shadow public abstract void stop();

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void axolotl_preventMobAttack(CallbackInfo ci) {
        if (AxolotlOrigin.Companion.getFakeDeathActiveSince().containsKey(this.target.getUuid())) {
            this.stop();
            ci.cancel();
        }
    }
}
