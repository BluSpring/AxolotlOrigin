package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.CrossbowAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.axolotlorigin.AxolotlOrigin;

@Mixin(CrossbowAttackGoal.class)
public class CrossbowAttackGoalMixin<T extends HostileEntity & RangedAttackMob & CrossbowUser> {
    //@Shadow @Final private T actor;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isAlive()Z"), method = "hasAliveTarget")
    public boolean axolotl_trickIsDead(LivingEntity instance) {
        return instance.isAlive() && !AxolotlOrigin.Companion.getFakeDeathActiveSince().containsKey(instance.getUuid());
    }
    // why doesn't this work
    /*
    @Inject(at = @At("HEAD"), method = "hasAliveTarget", cancellable = true)
    public void axolotl_trickIsDead(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.actor.getTarget() != null && this.actor.getTarget().isAlive() && !AxolotlOrigin.Companion.getFakeDeathActiveSince().containsKey(this.actor.getTarget().getUuid()));
    }*/
}
