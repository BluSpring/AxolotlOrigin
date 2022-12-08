package xyz.bluspring.axolotlorigin.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class BucketItemMixin {
    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void axolotl_workaroundWaterPlaceBug(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient) // Do not handle this on the client.
            return;

        var itemStack = user.getStackInHand(hand);

        if (itemStack.hasNbt() && itemStack.getNbt().contains("BucketedPlayerCreated")) {
            var created = itemStack.getNbt().getInt("BucketedPlayerCreated");

            if (world.getServer().getTicks() - created <= 3) { // There is a 3-tick window for this to not work.
                cir.setReturnValue(TypedActionResult.fail(itemStack));
            }
        }
    }
}
