package com.min01.tickrateapi.mixin;

import java.util.Map;
import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.min01.tickrateapi.api.EntityTickEvent;
import com.min01.tickrateapi.api.TickrateDimension;
import com.min01.tickrateapi.api.TickrateTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer
{
	@Shadow
	@Final
	private Map<ResourceKey<Level>, ServerLevel> levels;
	
	@Shadow
	private ProfilerFiller profiler;

	@Shadow(remap = false)
	private Map<ResourceKey<Level>, long[]> perWorldTickTimes;
	
	@Shadow
	private int tickCount;
	
	@Shadow(remap = false)
	private int worldArrayMarker;
	
	@Shadow(remap = false)
	private int worldArrayLast;

	@Shadow(remap = false)
	private ServerLevel[] worldArray;
	
	@ModifyReturnValue(method = "getWorldArray", at = @At("RETURN"), remap = false)
	private ServerLevel[] tickrateapi$getWorldArray(ServerLevel[] original)
	{
		if(this.worldArrayMarker == this.worldArrayLast && this.worldArray != null)
		{
			return this.worldArray;
		}
		this.worldArray = this.levels.values().stream().filter(t -> !TickrateUtil.hasDimensionTimer(t)).toArray(x -> new ServerLevel[x]);
		this.worldArrayLast = this.worldArrayMarker;
		return this.worldArray;
	}
	
	@WrapMethod(method = "tickServer")
	private void tickrateapi$tickServer(BooleanSupplier pHasTimeLeft, Operation<Void> original)
	{
		original.call(pHasTimeLeft);
		this.tickrateapi$tickServerLevel();
	}
	
	@Unique
	private void tickrateapi$tickServerLevel()
	{
		for(ServerLevel serverLevel : this.levels.values())
		{
			ProfilerFiller profilerfiller = serverLevel.getProfiler();
			serverLevel.entityTickList.forEach(t -> 
			{
				if(!t.isRemoved())
				{
					if(TickrateUtil.hasTimer(t))
					{
						if(serverLevel.shouldDiscardEntity(t)) 
						{
							t.discard();
						}
						else 
						{
							profilerfiller.push("checkDespawn");
							t.checkDespawn();
							profilerfiller.pop();
							if(serverLevel.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(t.chunkPosition().toLong())) 
							{
								Entity entity = t.getVehicle();
								if(entity != null)
								{
									if(!entity.isRemoved() && entity.hasPassenger(t))
									{
										return;
									}
									t.stopRiding();
								}
								profilerfiller.push("tick");
								if(!t.isRemoved() && !(t instanceof PartEntity))
								{
									TickrateTimer timer = TickrateUtil.getTimer(t);
									if(timer.tickrate <= 0)
									{
										TickrateUtil.DEFAULT_TIMER.tickServer(() -> 
										{
											MinecraftForge.EVENT_BUS.post(new EntityTickEvent(t));
										});
									}
									else
									{
										timer.tickServer(() -> 
										{
									        TickrateUtil.guardEntityTick(serverLevel::tickNonPassenger, t);
										});
									}
								}
								profilerfiller.pop();
							}
						}
					}
				}
			});
			
			if(!TickrateUtil.hasDimensionTimer(serverLevel))
			{
				continue;
			}
			
			TickrateDimension dimension = TickrateUtil.getDimensionTickrate(serverLevel);
			TickrateTimer timer = dimension.timer;
			
			timer.tickServer(() -> 
			{
	    		BooleanSupplier pHasTimeLeft = timer::haveTime;
	    		
	    		long tickStart = Util.getNanos();
	    		this.profiler.push(() -> 
	    		{
	    			return serverLevel + " " + serverLevel.dimension().location();
	    		});
	    		
	    		if(this.tickCount % 20 == 0)
	    		{
	    			this.profiler.push("timeSync");
	    			this.synchronizeTime(serverLevel);
	    			this.profiler.pop();
	    		}

	    		this.profiler.push("tick");
	    		ForgeEventFactory.onPreLevelTick(serverLevel, pHasTimeLeft);
	             
	    		try 
	    		{
	    			serverLevel.tick(pHasTimeLeft);
	    		}
	    		catch(Throwable throwable)
	    		{
	    			CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception ticking world");
	    			serverLevel.fillReportDetails(crashreport);
	    			throw new ReportedException(crashreport);
	    		}
	    		ForgeEventFactory.onPostLevelTick(serverLevel, pHasTimeLeft);
	    		
	    		this.profiler.pop();
	    		this.perWorldTickTimes.computeIfAbsent(serverLevel.dimension(), k -> new long[100])[this.tickCount % 100] = Util.getNanos() - tickStart;
			});
		}
	}
	
	@Shadow
	private void synchronizeTime(ServerLevel pLevel) 
	{
		   
	}
}
