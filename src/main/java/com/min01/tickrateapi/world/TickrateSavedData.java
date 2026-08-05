package com.min01.tickrateapi.world;

import java.util.ArrayList;
import java.util.List;

import com.min01.tickrateapi.api.TickrateArea;
import com.min01.tickrateapi.api.TickrateDimension;
import com.min01.tickrateapi.api.TickrateTimer;
import com.min01.tickrateapi.network.AddTickrateAreaPacket;
import com.min01.tickrateapi.network.RemoveTickrateAreaPacket;
import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.UpdateDimensionTickratePacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class TickrateSavedData extends SavedData
{
	public static final String NAME = "tickrate_data";
	
	public final List<TickrateArea> areas = new ArrayList<>();
	public TickrateDimension dimension = new TickrateDimension(Level.OVERWORLD, TickrateTimer.createDefault(), 1000);
	
    public static TickrateSavedData get(ServerLevel serverLevel)
    {
        DimensionDataStorage storage = serverLevel.getDataStorage();
        TickrateSavedData data = storage.computeIfAbsent(TickrateSavedData::load, TickrateSavedData::new, NAME);
        return data;
    }
    
    public static TickrateSavedData load(CompoundTag nbt) 
    {
    	TickrateSavedData data = new TickrateSavedData();
    	ListTag areas = nbt.getList("TickrateAreas", 10);
		for(int i = 0; i < areas.size(); ++i)
		{
			CompoundTag tag = areas.getCompound(i);
			data.addArea(TickrateArea.read(tag));
		}
		data.setDimensionTickrate(TickrateDimension.read(nbt));
        return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag areas = new ListTag();
		this.areas.forEach(t -> 
		{
			CompoundTag tag = new CompoundTag();
			t.write(tag);
			areas.add(tag);
		});
		nbt.put("TickrateAreas", areas);
		this.dimension.write(nbt);
		return nbt;
	}
	
	public void setDimensionTickrate(TickrateDimension dimension)
	{
		this.dimension = dimension;
		this.setDirty();
    	TickrateNetwork.sendToAll(new UpdateDimensionTickratePacket(this.dimension));
	}
	
	public TickrateDimension getDimensionTickrate()
	{
		return this.dimension;
	}
	
	public void addArea(TickrateArea area)
	{
	    this.areas.removeIf(t -> t.aabb.equals(area.aabb));
        this.areas.add(area);
		this.setDirty();
    	TickrateNetwork.sendToAll(new AddTickrateAreaPacket(area));
	}
	
	public void removeArea(TickrateArea area)
	{
		this.areas.removeIf(t -> t.aabb.equals(area.aabb));
		this.setDirty();
    	TickrateNetwork.sendToAll(new RemoveTickrateAreaPacket(area));
	}
	
	public List<TickrateArea> getAreas()
	{
		return this.areas;
	}
}