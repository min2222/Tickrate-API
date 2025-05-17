package com.min01.tickrateapi.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilities;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.command.SetTickrateCommand;
import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.UpdateAreaTickratePacket;
import com.min01.tickrateapi.network.UpdateDimensionTickratePacket;
import com.min01.tickrateapi.world.TickrateSavedData;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = TickrateAPI.MODID, bus = Bus.FORGE)
public class TickrateUtil 
{
	public static final Map<Integer, Entity> ENTITY_MAP = new HashMap<>();
	public static final Map<Integer, Entity> ENTITY_MAP2 = new HashMap<>();
	public static final Map<ResourceKey<Level>, CustomTimer> LEVEL_MAP = new HashMap<>();
	public static final List<Pair<AABB, CustomTimer>> AABB_LIST = new ArrayList<>();
	public static final CustomTimer TIMER = new CustomTimer(20.0F, 0L);
	
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
    	SetTickrateCommand.register(event.getDispatcher());
    }
	
	@SubscribeEvent
	public static void onEntityJoinLevel(EntityJoinLevelEvent event)
	{
		Entity entity = event.getEntity();
		ENTITY_MAP.put(entity.getClass().hashCode(), entity);
		ENTITY_MAP2.put(entity.getClass().getSuperclass().hashCode(), entity);
	}
	
	public static boolean hasDimensionTimer(ResourceKey<Level> dimension)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		return data.getTimer().tickrate != 20.0F;
    	}
		return LEVEL_MAP.containsKey(dimension) && LEVEL_MAP.get(dimension).tickrate != 20.0F;
	}
	
	public static CustomTimer getDimensionTimer(ResourceKey<Level> dimension)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		return data.getTimer();
    	}
		return LEVEL_MAP.get(dimension);
	}
    
    public static boolean isExcluded(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.isExcluded();
    }
    
    public static boolean shouldExcludeSubEntities(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.shouldExcludeSubEntities();
    }
    
    public static List<Pair<AABB, CustomTimer>> getTickrateAreas(ResourceKey<Level> dimension)
    {
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		return data.getTickrateAreas();
    	}
    	return AABB_LIST;
    }
    
	public static void addTickrateArea(ResourceKey<Level> dimension, AABB aabb, float tickrate)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		data.addTickrateArea(aabb, tickrate);
    		TickrateNetwork.sendToAll(new UpdateAreaTickratePacket(aabb, tickrate));
    	}
	}
	
	public static void setLevelTickrate(ResourceKey<Level> dimension, float tickrate)
	{
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		data.setTickrate(tickrate);
    		TickrateNetwork.sendToAll(new UpdateDimensionTickratePacket(dimension, tickrate));
    	}
	}
	
    public static void includeEntity(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.exclude(false);
    }

    public static void excludeEntity(Entity entity)
    {
    	excludeEntity(entity, true);
    }

    public static void excludeEntity(Entity entity, boolean excludeSubEntities)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.exclude(true);
    	cap.excludeSubEntities(excludeSubEntities);
    }
	
    public static void setTickrate(Entity entity, float tickrate)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.setTimer(new CustomTimer(tickrate, 0));
    }
    
    public static void resetTickrate(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.resetTickrate();
    }
    
    public static CustomTimer getTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	if(inArea(entity.level.dimension(), entity.getBoundingBox()))
    	{
    		return getTimerInArea(entity.level.dimension(), entity.getBoundingBox());
    	}
    	return cap.getTimer();
    }
    
    public static boolean hasTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.hasTimer() || inArea(entity.level.dimension(), entity.getBoundingBox());
    }
    
    public static CustomTimer getTimerInArea(ResourceKey<Level> dimension, AABB boundingBox)
    {
		for(Iterator<Pair<AABB, CustomTimer>> itr = getTickrateAreas(dimension).iterator(); itr.hasNext();)
		{
			Pair<AABB, CustomTimer> pair = itr.next();
			AABB aabb = pair.getLeft();
			CustomTimer timer = pair.getRight();
			if(aabb.intersects(boundingBox))
			{
				return timer;
			}
		}
		return TickrateUtil.TIMER;
    }
    
    public static boolean inArea(ResourceKey<Level> dimension, AABB boundingBox)
    {
		for(Iterator<Pair<AABB, CustomTimer>> itr = getTickrateAreas(dimension).iterator(); itr.hasNext();)
		{
			AABB aabb = itr.next().getLeft();
			return aabb.intersects(boundingBox);
		}
		return false;
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
