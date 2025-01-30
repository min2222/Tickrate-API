package com.min01.tickrateapi.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.command.SetTickrateCommand;
import com.min01.tickrateapi.command.StopTickrateCommand;
import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.TimerSyncPacket;
import com.min01.tickrateapi.world.TickrateSavedData;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

@EventBusSubscriber(modid = TickrateAPI.MODID, bus = Bus.GAME)
public class TickrateUtil 
{
	private static final Map<UUID, CustomTimer> TIMER_MAP = new HashMap<>();
	private static final Map<UUID, CustomTimer> CLIENT_TIMER_MAP = new HashMap<>();

	public static final String TICKRATE = "Tickrate";
	public static final String EXCLUDED = "Excluded";
	public static final Map<Integer, Entity> ENTITY_MAP = new HashMap<>();
	public static final Map<Integer, Entity> ENTITY_MAP2 = new HashMap<>();
	
	public static final CustomTimer STOP = new CustomTimer(0.0F, 0);
	
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
    	SetTickrateCommand.register(event.getDispatcher());
    	StopTickrateCommand.register(event.getDispatcher());
    }
    
	@SubscribeEvent
	public static void onEntityLeaveLevel(EntityLeaveLevelEvent event)
	{
		TickrateUtil.resetTickrate(event.getEntity());
	}
	
	@SubscribeEvent
	public static void onEntityJoinLevel(EntityJoinLevelEvent event)
	{
		Level level = event.getLevel();
		Entity entity = event.getEntity();
		ENTITY_MAP.put(entity.getClass().hashCode(), entity);
		ENTITY_MAP2.put(entity.getClass().getSuperclass().hashCode(), entity);
		if(!level.isClientSide)
		{
    		stopOrUnstopTime(isDimensionTimeStopped(level.dimension()), level);
    		excludeOrIncludeEntity(isExcluded(entity), entity);
		}
	}
	
	public static boolean isDimensionTimeStopped(ResourceKey<Level> dimension)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		return data.getDimensions().contains(dimension);
    	}
		return false;
	}
	
	public static boolean isEntityTimeStopped(Entity entity)
	{
		return !isExcluded(entity) && isDimensionTimeStopped(entity.level().dimension());
	}
    
    public static boolean isExcluded(Entity entity)
    {
    	TickrateSavedData data = TickrateSavedData.get(entity.level().dimension());
    	if(data != null)
    	{
    		return data.getExcludedEntities().containsKey(entity.getUUID());
    	}
    	return false;
    }
    
    public static boolean shouldExcludeSubEntities(Entity entity)
    {
    	TickrateSavedData data = TickrateSavedData.get(entity.level().dimension());
    	if(data != null)
    	{
    		return data.getExcludedEntities().get(entity.getUUID());
    	}
    	return false;
    }
	
	public static void stopOrUnstopTime(boolean flag, Level level)
	{
		if(!level.isClientSide)
		{
			stopOrUnstopTime(flag, level.dimension());
		}
	}

	//must call in only server side
	public static void stopOrUnstopTime(boolean flag, ResourceKey<Level> dimension)
	{
		if(flag)
		{
			stopTime(dimension);
		}
		else
		{
			unstopTime(dimension);
		}
	}

	//must call in only server side
	public static void stopTime(ResourceKey<Level> dimension)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		data.stopDimension(dimension, true);
    	}
	}

	//must call in only server side
	public static void unstopTime(ResourceKey<Level> dimension)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		data.stopDimension(dimension, false);
    	}
	}

	//must call in only server side
	public static void excludeOrIncludeEntity(boolean flag, Entity entity)
	{
		if(!entity.level().isClientSide)
		{
			if(flag)
			{
				excludeEntity(entity);
			}
			else
			{
				includeEntity(entity);
			}
		}
	}

	//must call in only server side
    public static void includeEntity(Entity entity)
    {
    	TickrateSavedData data = TickrateSavedData.get(entity.level().dimension());
    	if(data != null)
    	{
    		data.excludeEntities(entity, false, false);
    	}
    }

	//must call in only server side
    public static void excludeEntity(Entity entity)
    {
    	excludeEntity(entity, true);
    }

	//must call in only server side
    public static void excludeEntity(Entity entity, boolean excludeSubEntities)
    {
    	TickrateSavedData data = TickrateSavedData.get(entity.level().dimension());
    	if(data != null)
    	{
    		data.excludeEntities(entity, true, excludeSubEntities);
    	}
    }
	
    public static void setTickrate(Entity entity, float tickrate)
    {
    	if(!entity.level().isClientSide)
    	{
    		TickrateNetwork.sendToAll(new TimerSyncPacket(entity.getUUID(), tickrate, false));
			if(!hasTimer(entity))
			{
				setTimer(entity, tickrate);
			}
			else
			{
				CustomTimer timer = getTimer(entity);
				if(timer.tickrate != tickrate)
				{
					setTimer(entity, tickrate);
				}
			}
    	}
    }
    
    public static void resetTickrate(Entity entity)
    {
    	if(!entity.level().isClientSide)
    	{
    		TickrateNetwork.sendToAll(new TimerSyncPacket(entity.getUUID(), 0, true));
    		if(hasTimer(entity))
    		{
    			removeTimer(entity);
    		}
    	}
    }
    
    public static void removeTimer(Entity entity)
    {
    	removeTimer(entity.getUUID());
    }
    
    public static void removeTimer(UUID uuid)
    {
		TIMER_MAP.remove(uuid);
    }
    
    public static void removeClientTimer(Entity entity)
    {
    	removeClientTimer(entity.getUUID());
    }
    
    public static void removeClientTimer(UUID uuid)
    {
		CLIENT_TIMER_MAP.remove(uuid);
    }
    
    public static void setTimer(Entity entity, float tickrate)
    {
    	setTimer(entity.getUUID(), tickrate);
    }
    
    public static void setTimer(UUID uuid, float tickrate)
    {
		TIMER_MAP.put(uuid, new CustomTimer(tickrate, 0));
    }
    
    public static void setClientTimer(Entity entity, float tickrate)
    {
    	setClientTimer(entity.getUUID(), tickrate);
    }
    
    public static void setClientTimer(UUID uuid, float tickrate)
    {
    	CLIENT_TIMER_MAP.put(uuid, new CustomTimer(tickrate, 0));
    }
    
    public static CustomTimer getClientTimer(Entity entity)
    {
    	return getClientTimer(entity.getUUID());
    }
    
    public static CustomTimer getClientTimer(UUID uuid)
    {
    	return CLIENT_TIMER_MAP.get(uuid);
    }
    
    public static CustomTimer getTimer(Entity entity)
    {
    	return getTimer(entity.getUUID());
    }
    
    public static CustomTimer getTimer(UUID uuid)
    {
    	return TIMER_MAP.get(uuid);
    }
    
    public static boolean hasClientTimer(Entity entity)
    {
    	return hasClientTimer(entity.getUUID());
    }
    
    public static boolean hasClientTimer(UUID uuid)
    {
    	return CLIENT_TIMER_MAP.containsKey(uuid);
    }
    
    public static boolean hasTimer(Entity entity)
    {
    	return hasTimer(entity.getUUID());
    }
    
    public static boolean hasTimer(UUID uuid)
    {
    	return TIMER_MAP.containsKey(uuid);
    }
}
