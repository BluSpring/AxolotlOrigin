package xyz.bluspring.axolotlorigin.mixin.apoli;

import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ActiveCooldownPower.class, remap = false)
public abstract class ActiveCooldownPowerMixin extends CooldownPower {
    private int activatedTime = -1;

    public ActiveCooldownPowerMixin(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender) {
        super(type, entity, cooldownDuration, hudRender);
    }

    @Override
    public void use() {
        super.use();
        activatedTime = 0;
    }

    @Override
    public void tick() {
        super.tick();

        // Wait a little for the Single Use powers to run
        if (activatedTime >= 3) {
            activatedTime = -1;
        } else if (activatedTime >= 0) {
            activatedTime++;
        }
    }

    @Override
    public boolean canUse() {
        return entity.getEntityWorld().getTime() >= lastUseTime + cooldownDuration;
    }

    @Override
    public boolean isActive() {
        return super.isActive() &&
                entity.getEntityWorld().getTime() >= lastUseTime + cooldownDuration &&
                activatedTime >= 0;
    }
}
