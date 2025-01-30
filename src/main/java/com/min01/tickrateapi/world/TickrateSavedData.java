package com.min01.tickrateapi.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class TickrateSavedData extends SavedData
{
	public static final String NAME = "tickrate_data";
	
	private final List<ResourceKey<Level>> dimensions = new ArrayList<>();
	private final Map<UUID, Boolean> excludedEntities = new HashMap<>();
	
    public static SavedData.Factory<TickrateSavedData> factory()
    {
        return new SavedData.Factory<>(() -> 
        {
        	return new TickrateSavedData();
        }, TickrateSavedData::load, DataFixTypes.LEVEL);
    }
    
    public static TickrateSavedData get(ResourceKey<Level> dimension)
    {
    	MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    	if(server != null)
    	{
        	ServerLevel serverLevel = server.getLevel(dimension);
            if(serverLevel != null) 
            {
                DimensionDataStorage storage = serverLevel.getDataStorage();
                TickrateSavedData data = storage.computeIfAbsent(TickrateSavedData.factory(), NAME);
                return data;
            }
    	}
        return null;
    }

    public static TickrateSavedData load(CompoundTag nbt, HolderLookup.Provider registries) 
    {
    	TickrateSavedData data = new TickrateSavedData();
		ListTag list = nbt.getList("Dimensions", 10);
		for(int i = 0; i < list.size(); ++i)
		{
			CompoundTag tag = list.getCompound(i);
			String name = tag.getString("Dimension");
			ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(name));
			data.dimensions.add(key);
		}
		ListTag list1 = nbt.getList("ExcludedEntities", 10);
		for(int i = 0; i < list1.size(); ++i)
		{
			CompoundTag tag = list1.getCompound(i);
			UUID uuid = tag.getUUID("EntityUUID");
			boolean excludeSubEntities = tag.getBoolean("ExcludeSubEntities");
			data.excludedEntities.putIfAbsent(uuid, excludeSubEntities);
		}
        return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries)
	{
		ListTag list = new ListTag();
		ListTag list2 = new ListTag();
		this.dimensions.forEach(t -> 
		{
			CompoundTag tag = new CompoundTag();
			tag.putString("Dimension", t.location().toString());
			list.add(tag);
		});
		for(Entry<UUID, Boolean> entry : this.excludedEntities.entrySet())
		{
			UUID uuid = entry.getKey();
			boolean excludeSubEntities = entry.getValue();
			CompoundTag tag = new CompoundTag();
			tag.putUUID("EntityUUID", uuid);
			tag.putBoolean("ExcludeSubEntities", excludeSubEntities);
			list2.add(tag);
		}
		nbt.put("Dimensions", list);
		nbt.put("ExcludedEntities", list2);
		return nbt;
	}
	
	public List<ResourceKey<Level>> getDimensions()
	{
		return this.dimensions;
	}
	
	public Map<UUID, Boolean> getExcludedEntities()
	{
		return this.excludedEntities;
	}
	
	public void stopDimension(ResourceKey<Level> dimension, boolean stop)
	{
		if(stop)
		{
			if(!this.dimensions.contains(dimension))
			{
				this.dimensions.add(dimension);
			}
		}
		else
		{
			if(this.dimensions.contains(dimension))
			{
				this.dimensions.remove(dimension);
			}
		}
		this.setDirty();
	}
	
	public void excludeEntities(Entity entity, boolean exclude, boolean excludeSubEntities)
	{
		if(exclude)
		{
    		if(!this.excludedEntities.containsKey(entity.getUUID()))
    		{
    			this.excludedEntities.put(entity.getUUID(), excludeSubEntities);
    		}
		}
		else
		{
    		if(this.excludedEntities.containsKey(entity.getUUID()))
    		{
    			this.excludedEntities.remove(entity.getUUID());
    		}
		}
		this.setDirty();
	}
}
