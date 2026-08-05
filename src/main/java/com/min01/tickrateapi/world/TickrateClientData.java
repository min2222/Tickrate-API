package com.min01.tickrateapi.world;

import com.min01.tickrateapi.api.TickrateArea;
import com.min01.tickrateapi.api.TickrateDimension;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class TickrateClientData 
{
	public static final ObjectArrayList<TickrateArea> AREAS = new ObjectArrayList<>();
	public static final Object2ObjectOpenHashMap<ResourceKey<Level>, TickrateDimension> DIMENSIONS = new Object2ObjectOpenHashMap<>();
	
	public static void addArea(TickrateArea area)
	{
		removeArea(area);
    	AREAS.add(area);
	}
	
	public static void removeArea(TickrateArea area)
	{
		AREAS.removeIf(t -> t.aabb.equals(area.aabb));
	}
	
	public static void setDimensionTickrate(TickrateDimension dimension)
	{
		ResourceKey<Level> key = dimension.dimension;
		float tickrate = dimension.getTickrate();
		int priority = dimension.getPriority();
		if(tickrate == 20)
		{
			removeDimensionTickrate(key);
		}
		else
		{
	    	TickrateDimension dim = getDimensionTickrate(key);
			if(dim == null)
			{
				DIMENSIONS.put(key, dimension);
			}
			else
			{
		    	dim.setTickrate(tickrate);
		    	dim.setPriority(priority);
			}
		}
	}
	
	public static TickrateDimension getDimensionTickrate(ResourceKey<Level> dimension)
	{
		if(DIMENSIONS.containsKey(dimension))
		{
			return DIMENSIONS.get(dimension);
		}
		return null;
	}
	
	public static void removeDimensionTickrate(ResourceKey<Level> dimension)
	{
		DIMENSIONS.values().removeIf(t -> t.dimension.equals(dimension));
	}
	
	public static void clear()
	{
		AREAS.clear();
		DIMENSIONS.clear();
	}
}