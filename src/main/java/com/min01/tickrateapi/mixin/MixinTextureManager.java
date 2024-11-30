package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;

@Mixin(TextureManager.class)
public class MixinTextureManager
{
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		Minecraft mc = Minecraft.getInstance();
		if(mc.level == null)
			return;
		if(TickrateUtil.isDimensionTimeStopped(mc.level.dimension()))
		{
			ci.cancel();
		}
	}
}
