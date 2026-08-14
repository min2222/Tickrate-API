package com.min01.tickrateapi.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.tickrateapi.api.EntityTickEvent;
import com.min01.tickrateapi.api.TickrateTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
	@Nullable
	@Shadow
	public LocalPlayer player;
	
	@Nullable
	@Shadow
	public ClientLevel level;
	
	@Shadow
	private volatile boolean pause;

	@Shadow
	private float pausePartialTick;
	
	@SuppressWarnings("resource")
	@WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"))
	private void tickrateapi$renderGame(GameRenderer instance, float f1, long crashreport, boolean crashreportcategory, Operation<Void> original)
	{
		Minecraft mc = (Minecraft) (Object) this;
		original.call(instance, mc.getPartialTick(), crashreport, crashreportcategory);
	}
	
	@WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 1))
	private void tickrateapi$tickEntities(ProfilerFiller instance, Operation<Void> original)
	{
		if(this.level != null && !this.pause)
		{
			this.level.tickingEntities.forEach(entity -> 
			{
				if(!entity.isRemoved()) 
				{
					if(!(entity instanceof Player) && !entity.isPassenger() && TickrateUtil.hasTimer(entity))
					{
						TickrateTimer timer = TickrateUtil.getTimer(entity);
						if(timer.tickrate <= 0)
						{
							int j = TickrateUtil.DEFAULT_TIMER.advanceTime(Util.getMillis());
							for(int k = 0; k < Math.min(10, j); ++k)
							{
								MinecraftForge.EVENT_BUS.post(new EntityTickEvent(entity));
							}
						}
						else
						{
							int j = timer.advanceTime(Util.getMillis());
							for(int k = 0; k < Math.min(10, j); ++k)
							{
								TickrateUtil.guardEntityTick(this.level::tickNonPassenger, entity);
							}
						}
					}
				}
			});
		}
		original.call(instance);
	}
	
	@WrapOperation(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I"))
	private int tickrateapi$advanceTime(Timer instance, long pGameTime, Operation<Integer> original)
	{
		if(this.player != null && TickrateUtil.hasTimer(this.player))
		{
			TickrateTimer timer = TickrateUtil.getTimer(this.player);
			return timer.advanceTime(pGameTime);
		}
		return original.call(instance, pGameTime);
	}
	
	@ModifyReturnValue(method = "getFrameTime", at = @At("RETURN"))
	private float tickrateapi$getFrameTime(float original)
	{
		if(this.player != null && TickrateUtil.hasTimer(this.player))
		{
			TickrateTimer timer = TickrateUtil.getTimer(this.player);
			return timer.partialTick;
		}
		return original;
	}
	
	@ModifyReturnValue(method = "getPartialTick", at = @At("RETURN"), remap = false)
	private float tickrateapi$getPartialTick(float original)
	{
		if(this.player != null && TickrateUtil.hasTimer(this.player))
		{
			TickrateTimer timer = TickrateUtil.getTimer(this.player);
			return this.pause ? this.pausePartialTick : timer.partialTick;
		}
		return original;
	}
}
