package com.min01.tickrateapi.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class TickrateSavedData extends SavedData
{
	public static final String NAME = "tickrate_data";
	
	private boolean isStopped;
	private final List<AABB> areas = new ArrayList<>();
	
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
    	data.isStopped = nbt.getBoolean("isStopped");
    	ListTag areas = nbt.getList("Areas", 10);
		for(int i = 0; i < areas.size(); ++i)
		{
			CompoundTag tag = areas.getCompound(i);
			data.addTimeStopArea(new AABB(tag.getDouble("MinX"), tag.getDouble("MinY"), tag.getDouble("MinZ"), tag.getDouble("MaxX"), tag.getDouble("MaxY"), tag.getDouble("MaxZ")));
		}
        return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries)
	{
		ListTag areas = new ListTag();
		this.areas.forEach(t -> 
		{
			CompoundTag tag = new CompoundTag();
			tag.putDouble("MinX", t.minX);
			tag.putDouble("MinY", t.minY);
			tag.putDouble("MinZ", t.minZ);
			tag.putDouble("MaxX", t.maxX);
			tag.putDouble("MaxY", t.maxY);
			tag.putDouble("MaxZ", t.maxZ);
			areas.add(tag);
		});
		nbt.putBoolean("isStopped", this.isStopped);
		nbt.put("Areas", areas);
		return nbt;
	}
	
	public List<AABB> getTimeStopAreas()
	{
		return this.areas;
	}
	
	public void stopTime()
	{
		this.isStopped = true;
		this.setDirty();
	}
	
	public void unstopTime()
	{
		this.isStopped = false;
		this.setDirty();
	}
	
	public void removeTimeStopArea(AABB aabb)
	{
		if(this.areas.contains(aabb))
		{
			for(Iterator<AABB> itr = this.areas.iterator(); itr.hasNext();)
			{
				AABB next = itr.next();
				if(next == aabb)
				{
					itr.remove();
				}
			}
			this.setDirty();
		}
	}
	
	public void addTimeStopArea(AABB aabb)
	{
		if(!this.areas.contains(aabb))
		{
			this.areas.add(aabb);
			this.setDirty();
		}
	}
	
	public boolean isStopped()
	{
		return this.isStopped;
	}
}
