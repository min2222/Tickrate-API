package com.min01.tickrateapi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.command.SetEntityTickrateCommand;
import com.min01.tickrateapi.network.ExcludeEntitySyncPacket;
import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.TimeStopSyncPacket;
import com.min01.tickrateapi.network.TimerSyncPacket;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = TickrateAPI.MODID, bus = Bus.FORGE)
public class TickrateUtil 
{
	private static final Map<UUID, CustomTimer> TIMER_MAP = new HashMap<>();
	private static final Map<UUID, CustomTimer> CLIENT_TIMER_MAP = new HashMap<>();

	public static final String TICKRATE = "Tickrate";
	public static final String EXCLUDED = "Excluded";
	public static final Map<Integer, UUID> ENTITY_MAP = new HashMap<>();
	public static final Map<Integer, UUID> ENTITY_MAP2 = new HashMap<>();
	
	public static final List<ResourceKey<Level>> DIMENSIONS = new ArrayList<>();
	public static final Map<UUID, Boolean> EXCLUDED_ENTITIES = new HashMap<>();
	
	public static final CustomTimer STOP = new CustomTimer(0.0F, 0);
	
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
    	SetEntityTickrateCommand.register(event.getDispatcher());
    }
    
	@SubscribeEvent
	public static void onEntityLeaveLevel(EntityLeaveLevelEvent event)
	{
		TickrateUtil.resetTickrate(event.getEntity());
	}
	
	//test purpose;
	@SubscribeEvent
	public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event)
	{
		if(event.getItemStack().getItem() == Items.NETHERITE_SWORD)
		{
			Player player = event.getEntity();
			boolean isStop = isDimensionTimeStopped(player.level.dimension());
			boolean isExcluded = isExcluded(player);
			stopOrUnstopTime(!isStop, player.level);
			excludeOrIncludeEntity(!isExcluded, player);
		}
	}
	
	@SubscribeEvent
	public static void onEntityJoin(EntityJoinLevelEvent event)
	{
		ENTITY_MAP.put(event.getEntity().getClass().hashCode(), event.getEntity().getUUID());
		for(Class<?> clazz : event.getEntity().getClass().getDeclaredClasses())
		{
			ENTITY_MAP2.put(clazz.hashCode(), event.getEntity().getUUID());
		}
	}
	
	public static boolean isDimensionTimeStopped(ResourceKey<Level> dimension)
	{
		return DIMENSIONS.contains(dimension);
	}
	
	public static boolean isEntityTimeStopped(Entity entity)
	{
		return !isExcluded(entity) && isDimensionTimeStopped(entity.level.dimension());
	}
    
    public static boolean isExcluded(Entity entity)
    {
    	return EXCLUDED_ENTITIES.containsKey(entity.getUUID());
    }
    
    public static boolean shouldExcludeSubEntities(Entity entity)
    {
    	return EXCLUDED_ENTITIES.get(entity.getUUID());
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
		if(!DIMENSIONS.contains(dimension))
		{
			DIMENSIONS.add(dimension);
		}
		TickrateNetwork.sendToAll(new TimeStopSyncPacket(dimension, true));
	}

	//must call in only server side
	public static void unstopTime(ResourceKey<Level> dimension)
	{
		if(DIMENSIONS.contains(dimension))
		{
			DIMENSIONS.remove(dimension);
		}
		TickrateNetwork.sendToAll(new TimeStopSyncPacket(dimension, false));
	}

	//must call in only server side
	public static void excludeOrIncludeEntity(boolean flag, Entity entity)
	{
		if(!entity.level.isClientSide)
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
		if(EXCLUDED_ENTITIES.containsKey(entity.getUUID()))
		{
			EXCLUDED_ENTITIES.remove(entity.getUUID());
		}
		TickrateNetwork.sendToAll(new ExcludeEntitySyncPacket(entity.getUUID(), false, false));
    }

	//must call in only server side
    public static void excludeEntity(Entity entity)
    {
    	excludeEntity(entity, true);
    }

	//must call in only server side
    public static void excludeEntity(Entity entity, boolean excludeSubEntities)
    {
		if(!EXCLUDED_ENTITIES.containsKey(entity.getUUID()))
		{
			EXCLUDED_ENTITIES.put(entity.getUUID(), excludeSubEntities);
		}
		TickrateNetwork.sendToAll(new ExcludeEntitySyncPacket(entity.getUUID(), true, excludeSubEntities));
    }
	
    public static void setTickrate(Entity entity, float tickrate)
    {
    	if(!entity.level.isClientSide)
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
    	if(!entity.level.isClientSide)
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
