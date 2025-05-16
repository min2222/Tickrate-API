package com.min01.tickrateapi.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.util.CustomTimer;
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
		if(TickrateUtil.hasDimensionTimer(mc.level.dimension()))
		{
			CustomTimer timer = TickrateUtil.getDimensionTimer(mc.level.dimension());
			if(timer.tickrate == 0.0F)
			{
				ci.cancel();
			}
		}
	}
}
