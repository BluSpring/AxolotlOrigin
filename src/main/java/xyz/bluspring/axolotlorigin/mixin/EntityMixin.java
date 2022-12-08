package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.axolotlorigin.powers.AxolotlBucketManager;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract EntityType<?> getType();

    // This is to trick Origins into thinking the person is
    // submerged in water when the axolotl origin is bucketed
    @Inject(at = @At("RETURN"), method = "isSubmergedIn", cancellable = true)
    public void axolotlTrickOrigins(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> cir) {
        if (fluidTag == FluidTags.LAVA)
            return;

        if (this.getType() != EntityType.PLAYER)
            return;

        if (!AxolotlBucketManager.INSTANCE.isBucketed((PlayerEntity) (Object) this))
            return;

        cir.setReturnValue(true);
    }
}
