package com.min01.tickrateapi.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilities;
import com.min01.tickrateapi.command.SetTickrateCommand;
import com.min01.tickrateapi.command.StopTickrateCommand;
import com.min01.tickrateapi.world.TickrateSavedData;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber(modid = TickrateAPI.MODID, bus = Bus.GAME)
public class TickrateUtil 
{
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
		Entity entity = event.getEntity();
		ENTITY_MAP.put(entity.getClass().hashCode(), entity);
		ENTITY_MAP2.put(entity.getClass().getSuperclass().hashCode(), entity);
	}
	
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerLoggedInEvent event)
	{
		Level level = event.getEntity().level();
		if(isExcluded(event.getEntity()))
		{
			excludeEntity(event.getEntity(), shouldExcludeSubEntities(event.getEntity()));
		}
		for(Iterator<AABB> itr = getTimeStopAreas(level.dimension()).iterator(); itr.hasNext();)
		{
			AABB next = itr.next();
			addTimeStopArea(level.dimension(), next);
		}
	}
	
	public static boolean isDimensionTimeStopped(ResourceKey<Level> dimension)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		return data.isStopped();
    	}
		return false;
	}
	
	public static boolean isEntityTimeStopped(Entity entity)
	{
		for(Iterator<AABB> itr = TickrateUtil.getTimeStopAreas(entity.level().dimension()).iterator(); itr.hasNext();)
		{
			AABB aabb = itr.next();
			if(aabb.contains(entity.position()))
			{
				return !isExcluded(entity);
			}
		}
		return !isExcluded(entity) && isDimensionTimeStopped(entity.level().dimension());
	}
    
    public static boolean isExcluded(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE);
    	return cap.isExcluded();
    }
    
    public static boolean shouldExcludeSubEntities(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE);
    	return cap.shouldExcludeSubEntities();
    }
    
    public static List<AABB> getTimeStopAreas(ResourceKey<Level> dimension)
    {
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		return data.getTimeStopAreas();
    	}
    	return new ArrayList<>();
    }
    
    public static void removeTimeStopArea(ResourceKey<Level> dimension, AABB aabb)
    {
       	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		data.removeTimeStopArea(aabb);
    	}
    }
    
	public static void addTimeStopArea(ResourceKey<Level> dimension, AABB aabb)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		data.addTimeStopArea(aabb);
    	}
	}
	
	public static void stopTime(ResourceKey<Level> dimension)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		data.stopTime();
    	}
	}

	public static void unstopTime(ResourceKey<Level> dimension)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		data.unstopTime();
    	}
	}
	
    public static void includeEntity(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE);
    	cap.exclude(false);
    }

    public static void excludeEntity(Entity entity)
    {
    	excludeEntity(entity, true);
    }

    public static void excludeEntity(Entity entity, boolean excludeSubEntities)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE);
    	cap.exclude(true);
    	cap.excludeSubEntities(excludeSubEntities);
    }
	
    public static void setTickrate(Entity entity, float tickrate)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE);
    	cap.setTimer(new CustomTimer(tickrate, 0));
    }
    
    public static void resetTickrate(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE);
    	cap.resetTickrate();
    }
    
    public static CustomTimer getTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE);
    	return cap.getTimer();
    }
    
    public static boolean hasTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE);
    	return cap.hasTimer();
    }
    
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getEntityByUUID(Level level, UUID uuid)
	{
		Method m = ObfuscationReflectionHelper.findMethod(Level.class, "m_142646_");
		try 
		{
			LevelEntityGetter<Entity> entities = (LevelEntityGetter<Entity>) m.invoke(level);
			return (T) entities.get(uuid);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
}
