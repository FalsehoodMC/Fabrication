package com.unascribed.fabrication.mixin._general.atlas_tracking;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Sprite.class)
@EligibleIf(envMatches= Env.CLIENT)
public interface SpriteAccessor {
    @Invoker("getFrameCount")
    int getFrameCount();
}
