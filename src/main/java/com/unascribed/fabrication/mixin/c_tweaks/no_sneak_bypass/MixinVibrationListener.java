package com.unascribed.fabrication.mixin.c_tweaks.no_sneak_bypass;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FakeMixinHack;
import net.minecraft.world.event.listener.VibrationListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VibrationListener.Callback.class)
@EligibleIf(configAvailable="*.no_sneak_bypass")
@FakeMixinHack("com.unascribed.fabrication.logic.NoSneakBypassVibrations")
public interface MixinVibrationListener {
}
