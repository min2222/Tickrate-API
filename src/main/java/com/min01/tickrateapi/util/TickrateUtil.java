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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = TickrateAPI.MODID, bus = Bus.FORGE)
public class TickrateUtil 
{
	public static final Method GET_ENTITY = ObfuscationReflectionHelper.findMethod(Level.class, "m_142646_");
	public static final Map<Integer, Entity> ENTITY_MAP = new HashMap<>();
	public static final Map<Integer, Entity> ENTITY_MAP2 = new HashMap<>();
	public static final Map<ResourceKey<Level>, CustomTimer> LEVEL_MAP = new HashMap<>();
	public static final List<Pair<AABB, Float>> AABB_LIST = new ArrayList<>();
	
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
    	SetTickrateCommand.register(event.getDispatcher());
    }
    
	@SubscribeEvent
	public static void onLevelLoadEvent(LevelEvent.Load event)
	{
		ResourceKey<Level> dimension = ((Level) event.getLevel()).dimension();
    	TickrateSavedData data = TickrateSavedData.get(dimension);
    	if(data != null)
    	{
    		TickrateNetwork.sendToAll(new UpdateDimensionTickratePacket(dimension, data.getTimer().tickrate));
    		data.getTickrateAreas().forEach(t ->
    		{
        		TickrateNetwork.sendToAll(new UpdateAreaTickratePacket(t.getLeft(), t.getRight()));
    		});
    	}
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
    
    public static boolean shouldChangeSubEntities(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.shouldChangeSubEntities();
    }
    
    public static boolean shouldExcludeSubEntities(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.shouldExcludeSubEntities();
    }
    
    public static List<Pair<AABB, Float>> getTickrateAreas(ResourceKey<Level> dimension)
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
    
    public static void changeSubEntities(Entity entity, boolean changeSubEntities)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.changeSubEntities(changeSubEntities);
    }
    
    public static void setBaseTickrate(Entity entity, float tickrate)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.setBaseTickrate(tickrate);
    }
	
    public static void setTickrate(Entity entity, float tickrate)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.setTickrate(tickrate);
    }
    
    public static float getTickrate(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.getTickrate();
    }
    
    public static void resetTickrate(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.resetTickrate();
    }
    
    public static CustomTimer getBaseTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.getBaseTimer();
    }
    
    public static CustomTimer getTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.getCurrentTimer();
    }
    
    public static boolean hasTimer(Entity entity)
    {
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
    	return cap.hasTimer();
    }
    
    public static float getTickRateAt(ResourceKey<Level> dimension, Vec3 pos)
    {
		for(Iterator<Pair<AABB, Float>> itr = getTickrateAreas(dimension).iterator(); itr.hasNext();)
		{
			Pair<AABB, Float> pair = itr.next();
			AABB aabb = pair.getLeft();
			if(aabb.contains(pos))
			{
				return pair.getRight();
			}
		}
		return 20.0F;
    }
    
    public static Pair<Boolean, Float> getArea(ResourceKey<Level> dimension, AABB boundingBox)
    {
		for(Iterator<Pair<AABB, Float>> itr = getTickrateAreas(dimension).iterator(); itr.hasNext();)
		{
			Pair<AABB, Float> pair = itr.next();
			AABB aabb = pair.getLeft();
			return Pair.of(aabb.intersects(boundingBox), pair.getRight());
		}
		return Pair.of(false, 20.0F);
    }
    
	@SuppressWarnings("unchecked")
	public static <T extends Entity> T getEntityByUUID(Level level, UUID uuid)
	{
		try 
		{
			LevelEntityGetter<Entity> entities = (LevelEntityGetter<Entity>) GET_ENTITY.invoke(level);
			return (T) entities.get(uuid);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
}
