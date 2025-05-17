package com.min01.tickrateapi.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.event.ForgeEventFactory;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
	@Shadow
	private volatile boolean pause;

	@Shadow
	private float pausePartialTick;
	
	@Nullable
	@Shadow
	public ClientLevel level;
	
	@Nullable
	@Shadow
	public LocalPlayer player;
	
	@Final
	@Shadow
	private Timer timer;

	@Inject(at = @At("HEAD"), method = "getFrameTime", cancellable = true)
	private void getFrameTime(CallbackInfoReturnable<Float> cir) 
	{
		if(this.player != null && this.level != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				cir.setReturnValue(TickrateUtil.getTimer(this.player).partialTick);
			}
			else if(TickrateUtil.hasDimensionTimer(this.level.dimension()))
			{
				cir.setReturnValue(TickrateUtil.getDimensionTimer(this.level.dimension()).partialTick);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getDeltaFrameTime", cancellable = true)
	private void getDeltaFrameTime(CallbackInfoReturnable<Float> cir) 
	{
		if(this.player != null && this.level != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				cir.setReturnValue(TickrateUtil.getTimer(this.player).tickDelta);
			}
			else if(TickrateUtil.hasDimensionTimer(this.level.dimension()))
			{
				cir.setReturnValue(TickrateUtil.getDimensionTimer(this.level.dimension()).tickDelta);
			}
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I"), method = "runTick")
	private int advanceTime(Timer instance, long p_92526_)
	{
		if(this.player != null && this.level != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				return TickrateUtil.getTimer(this.player).advanceTime(p_92526_);
			}
			else if(TickrateUtil.hasDimensionTimer(this.level.dimension()))
			{
				return TickrateUtil.getDimensionTimer(this.level.dimension()).advanceTime(p_92526_);
			}
			else
			{
				return instance.advanceTime(p_92526_);
			}
		}
		else
		{
			return instance.advanceTime(p_92526_);
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"), method = "runTick")
	private void render(GameRenderer instance, float f1, long crashreport, boolean crashreportcategory)
	{
		if(this.player != null && this.level != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				instance.render(this.pause ? this.pausePartialTick : TickrateUtil.getTimer(this.player).partialTick, crashreport, crashreportcategory);
			}
			else if(TickrateUtil.hasDimensionTimer(this.level.dimension()))
			{
				instance.render(TickrateUtil.getDimensionTimer(this.level.dimension()).partialTick, crashreport, crashreportcategory);
			}
			else
			{
				instance.render(f1, crashreport, crashreportcategory);
			}
		}
		else
		{
			instance.render(f1, crashreport, crashreportcategory);
		}
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onRenderTickStart(F)V"), method = "runTick", remap = false)
	private void onRenderTickStart(float timer)
	{
		if(this.player != null && this.level != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				ForgeEventFactory.onRenderTickStart(this.pause ? this.pausePartialTick : TickrateUtil.getTimer(this.player).partialTick);
			}
			else if(TickrateUtil.hasDimensionTimer(this.level.dimension()))
			{
				ForgeEventFactory.onRenderTickStart(TickrateUtil.getDimensionTimer(this.level.dimension()).partialTick);
			}
			else
			{
				ForgeEventFactory.onRenderTickStart(timer);
			}
		}
		else
		{
			ForgeEventFactory.onRenderTickStart(timer);
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onRenderTickEnd(F)V"), method = "runTick", remap = false)
	private void onRenderTickEnd(float timer)
	{
		if(this.player != null && this.level != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				ForgeEventFactory.onRenderTickEnd(this.pause ? this.pausePartialTick : TickrateUtil.getTimer(this.player).partialTick);
			}
			else if(TickrateUtil.hasDimensionTimer(this.level.dimension()))
			{
				ForgeEventFactory.onRenderTickEnd(TickrateUtil.getDimensionTimer(this.level.dimension()).partialTick);
			}
			else
			{
				ForgeEventFactory.onRenderTickEnd(timer);
			}
		}
		else
		{
			ForgeEventFactory.onRenderTickEnd(timer);
		}
	}
}
