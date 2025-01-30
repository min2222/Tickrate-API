package com.min01.tickrateapi.mixin;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.tickrateapi.config.TimerConfig;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends Level
{
	protected MixinServerLevel(WritableLevelData p_220352_, ResourceKey<Level> p_220353_, RegistryAccess p_270200_, Holder<DimensionType> p_220354_, Supplier<ProfilerFiller> p_220355_, boolean p_220356_, boolean p_220357_, long p_220358_, int p_220359_)
	{
		super(p_220352_, p_220353_, p_270200_, p_220354_, p_220355_, p_220356_, p_220357_, p_220358_, p_220359_);
	}
	
	@Inject(at = @At("HEAD"), method = "addFreshEntity")
	private void addFreshEntity(Entity p_8837_, CallbackInfoReturnable<Boolean> ci)
	{
		MySecurityManager manager = new MySecurityManager();
		Class<?>[] ctx = manager.getContext();
		for(Class<?> clazz : ctx)
		{
			if(TickrateUtil.ENTITY_MAP.containsKey(clazz.hashCode()))
			{
				Entity entity = TickrateUtil.ENTITY_MAP.get(clazz.hashCode());
				if(entity != null)
				{
					if(TickrateUtil.hasTimer(entity))
					{
						if(TickrateUtil.getTimer(entity).shouldChangeSubEntities)
						{
							TickrateUtil.setTickrate(p_8837_, TickrateUtil.getTimer(entity).tickrate);
						}
					}
					if(TickrateUtil.isExcluded(entity) && TickrateUtil.shouldExcludeSubEntities(entity))
					{
						TickrateUtil.excludeEntity(p_8837_);
					}
				}
			}
			else if(TickrateUtil.ENTITY_MAP2.containsKey(clazz.hashCode()))
			{
				Entity entity = TickrateUtil.ENTITY_MAP2.get(clazz.hashCode());
				if(entity != null)
				{
					if(TickrateUtil.hasTimer(entity))
					{
						if(TickrateUtil.getTimer(entity).shouldChangeSubEntities)
						{
							TickrateUtil.setTickrate(p_8837_, TickrateUtil.getTimer(entity).tickrate);
						}
					}
					if(TickrateUtil.isExcluded(entity) && TickrateUtil.shouldExcludeSubEntities(entity))
					{
						TickrateUtil.excludeEntity(p_8837_);
					}
				}
			}
		}
	}

	@SuppressWarnings("removal")
	private static class MySecurityManager extends SecurityManager
	{
		public Class<?>[] getContext()
		{
			return this.getClassContext();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "tick", cancellable = true)
	private void tick(BooleanSupplier p_8794_, CallbackInfo ci) 
	{
		if(TickrateUtil.isDimensionTimeStopped(this.dimension()))
		{
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "tickNonPassenger", cancellable = true)
	private void tickNonPassenger(Entity p_8648_, CallbackInfo ci) 
	{
		if(TickrateUtil.isEntityTimeStopped(p_8648_))
		{
			ci.cancel();
		}
		if(p_8648_ instanceof Player)
			return;
		if(TickrateUtil.hasTimer(p_8648_))
		{
			ci.cancel();
			int j = TickrateUtil.getTimer(p_8648_).advanceTime(Util.getMillis());
			for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, j); ++k)
			{
				this.tickEntities(p_8648_);
			}
		}
	}

	@Unique
	private void tickEntities(Entity p_8648_)
	{
		p_8648_.setOldPosAndRot();
		ProfilerFiller profilerfiller = this.getProfiler();
		p_8648_.tickCount++;
		this.getProfiler().push(() -> BuiltInRegistries.ENTITY_TYPE.getKey(p_8648_.getType()).toString());
		profilerfiller.incrementCounter("tickNonPassenger");
		if(!net.neoforged.neoforge.event.EventHooks.fireEntityTickPre(p_8648_).isCanceled())
		{
			p_8648_.tick();
			net.neoforged.neoforge.event.EventHooks.fireEntityTickPost(p_8648_);
		}
		this.getProfiler().pop();
		for(Entity entity : p_8648_.getPassengers())
		{
			this.tickPassenger(p_8648_, entity);
		}
	}
	
	@Shadow
	private void tickPassenger(Entity p_104642_, Entity p_104643_) 
	{
		
	}
}
