package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.axolotlorigin.powers.AxolotlBucketManager;
import xyz.bluspring.axolotlorigin.powers.AxolotlPowerTypes;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getRespiration(Lnet/minecraft/entity/LivingEntity;)I"), method = "getNextAirUnderwater")
    public int increaseRespirationLevel(LivingEntity entity) {
        var respiration = EnchantmentHelper.getRespiration(entity);

        if (!AxolotlPowerTypes.INSTANCE.getWaterLocked().isActive(entity))
            return respiration;

        return 7 + respiration;
    }

    @Inject(at = @At("HEAD"), method = "stopRiding", cancellable = true)
    public void axolotl_preventDismountIfBucketed(CallbackInfo ci) {
        if (((LivingEntity) (Object) this).getType() != EntityType.PLAYER)
            return;

        if (AxolotlBucketManager.INSTANCE.isBucketed((PlayerEntity) (Object) this)) {
            ci.cancel();
        }
    }
}
