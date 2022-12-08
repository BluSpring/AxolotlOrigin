package xyz.bluspring.axolotlorigin.powers

import io.github.apace100.apoli.power.Power
import io.github.apace100.apoli.power.PowerType
import io.github.apace100.apoli.power.PowerTypeReference
import net.minecraft.util.Identifier

object AxolotlPowerTypes {
    val ancientAttack: PowerType<*> = PowerTypeReference<Power>(Identifier("haiken", "ancient_attack"))
    val ancientSense: PowerType<*> = PowerTypeReference<Power>(Identifier("haiken", "ancient_sense"))
    val antiFreeze: PowerType<*> = PowerTypeReference<Power>(Identifier("haiken", "antifreeze"))
    val axolotlBucket: PowerType<*> = PowerTypeReference<Power>(Identifier("haiken", "axolotl_bucket"))
    //val bluePower: PowerType<*> = PowerTypeReference<Power>(Identifier("haiken", "blue_power"))

    val fakeDeath: PowerType<*> = PowerTypeReference<Power>(Identifier("haiken", "fake_death"))
    val fakeDeathActivator: PowerType<*> = PowerTypeReference<Power>(Identifier("haiken", "fake_death_activator"))

    val waterLocked: PowerType<*> = PowerTypeReference<Power>(Identifier("haiken", "water_locked"))
}