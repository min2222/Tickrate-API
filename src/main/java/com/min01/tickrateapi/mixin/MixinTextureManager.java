package com.min01.tickrateapi.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.config.TimerConfig;
import com.min01.tickrateapi.util.ITime;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.Tickable;

@Mixin(TextureManager.class)
public class MixinTextureManager 
{
	@Shadow
	@Final
	private Set<Tickable> tickableTextures;
	
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		Minecraft mc = Minecraft.getInstance();
		if(TickrateUtil.hasDimensionTimer(mc.level.dimension()) && TickrateUtil.isExcluded(mc.player))
		{
			ci.cancel();
			int j = ((ITime)mc.level).getTime();
			for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, j); ++k)
			{
				for(Tickable tickable : this.tickableTextures)
				{
					tickable.tick();
				}
			}
		}
	}
}
