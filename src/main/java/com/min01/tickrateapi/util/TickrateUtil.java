package com.min01.tickrateapi.util;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.api.EntityTickEvent;
import com.min01.tickrateapi.api.TickrateArea;
import com.min01.tickrateapi.api.TickrateData;
import com.min01.tickrateapi.api.TickrateDimension;
import com.min01.tickrateapi.api.TickrateTimer;
import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.command.TickrateCommand;
import com.min01.tickrateapi.network.AddTickrateAreaPacket;
import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.UpdateDimensionTickratePacket;
import com.min01.tickrateapi.world.TickrateClientData;
import com.min01.tickrateapi.world.TickrateSavedData;
import com.mojang.logging.LogUtils;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.server.timings.TimeTracker;

@Mod.EventBusSubscriber(modid = TickrateAPI.MODID, bus = Bus.FORGE)
public class TickrateUtil 
{
	public static final TickrateTimer DEFAULT_TIMER = TickrateTimer.createDefault();
	
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
    	TickrateCommand.register(event.getDispatcher());
    }
    
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerLoggedInEvent event)
	{
		Player player = event.getEntity();
		Level level = player.level;
		if(!level.isClientSide)
		{
			List<TickrateArea> areas = getTickrateAreas(level);
			areas.forEach(t ->
			{
		    	TickrateNetwork.sendToAll(new AddTickrateAreaPacket(t));
			});
			TickrateDimension dimension = getDimensionTickrate(level);
	    	TickrateNetwork.sendToAll(new UpdateDimensionTickratePacket(dimension));
		}
	}
	
	@SubscribeEvent
	public static void onEntityTick(EntityTickEvent event)
	{
		Entity entity = event.getEntity();
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.tick(entity);
	}
	
    public static void setBaseTickrate(Entity entity, float tickrate)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.setBaseTickrate(tickrate);
    }
    
    public static void setTickrate(Entity entity, float tickrate)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.setTickrate(tickrate);
    }
    
    public static void setPriority(Entity entity, int priority)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.setPriority(priority);
    }
    
    public static void resetTickrate(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.resetTickrate();
    }
    
    public static int getPriority(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.getPriority();
    }
    
    public static boolean hasTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.hasTimer();
    }
    
    public static float getBaseTickrate(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.getBaseTickrate();
    }
    
    public static float getTickrate(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.getTickrate();
    }
    
    public static TickrateTimer getBaseTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.getBaseTimer();
    }
    
    public static TickrateTimer getTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.getTimer();
    }
    
	public static void addTickrateArea(Level level, TickrateArea area)
	{
    	if(level instanceof ServerLevel serverLevel)
    	{
	    	TickrateSavedData data = TickrateSavedData.get(serverLevel);
	    	data.addArea(area);
		}
	}
	
	public static void removeTickrateArea(Level level, TickrateArea area)
	{
    	if(level instanceof ServerLevel serverLevel)
    	{
	    	TickrateSavedData data = TickrateSavedData.get(serverLevel);
	    	data.removeArea(area);
		}
	}
    
    public static List<TickrateArea> getTickrateAreas(Level level)
    {
    	if(level instanceof ServerLevel serverLevel)
    	{
        	TickrateSavedData data = TickrateSavedData.get(serverLevel);
        	return data.getAreas();
    	}
    	return TickrateClientData.AREAS;
    }
	
	public static void setDimensionTickrate(Level level, float tickrate, int priority)
	{
    	if(level instanceof ServerLevel serverLevel)
    	{
	    	TickrateSavedData data = TickrateSavedData.get(serverLevel);
	    	TickrateDimension dimension = new TickrateDimension(level.dimension(), TickrateTimer.createWithTickrate(tickrate), priority);
	    	data.setDimensionTickrate(dimension);
		}
	}
	
    public static TickrateDimension getDimensionTickrate(Level level)
    {
    	if(level instanceof ServerLevel serverLevel)
    	{
        	TickrateSavedData data = TickrateSavedData.get(serverLevel);
        	return data.dimension;
    	}
    	return TickrateClientData.getDimensionTickrate(level.dimension());
    }
	
	public static boolean hasDimensionTimer(Level level)
	{
    	TickrateDimension dimension = getDimensionTickrate(level);
    	return dimension != null && dimension.getTickrate() != 20.0F;
	}
	
	public static TickrateData findHighestPriorityTickrate(Entity entity)
	{
		Level level = entity.level;
		TickrateData data = null;
		
		int priority = Integer.MIN_VALUE;

		if(hasDimensionTimer(level)) 
		{
			data = getDimensionTickrate(level);
		}

		List<TickrateArea> areas = getTickrateAreas(level);
		for(TickrateArea area : areas)
		{
		    if(area.dimension.equals(level.dimension()) && area.aabb.intersects(entity.getBoundingBox()))
		    {
		        if(area.getPriority() >= priority) 
		        {
		        	data = area;
		        }
		    }
		}
		return data;
	}
    
	public static void getClientLevel(Consumer<Level> consumer)
	{
		LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT).filter(ClientLevel.class::isInstance).ifPresent(level -> 
		{
			consumer.accept(level);
		});
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getEntityByUUID(Level level, UUID uuid)
	{
		return (T) level.getEntities().get(uuid);
	}
	
	//copied from Level
	public static <T extends Entity> void guardEntityTick(Consumer<T> pConsumerEntity, T pEntity)
	{
		try
		{
			TimeTracker.ENTITY_UPDATE.trackStart(pEntity);
			pConsumerEntity.accept(pEntity);
		} 
		catch(Throwable throwable)
		{
			CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking entity");
			CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being ticked");
			pEntity.fillCrashReportCategory(crashreportcategory);
			if(ForgeConfig.SERVER.removeErroringEntities.get())
			{
				LogUtils.getLogger().error("{}", crashreport.getFriendlyReport());
				pEntity.discard();
			} 
			else
			{
				throw new ReportedException(crashreport);
			}
		} 
		finally
		{
			TimeTracker.ENTITY_UPDATE.trackEnd(pEntity);
		}
	}
}
