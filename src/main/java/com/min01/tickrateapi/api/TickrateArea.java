package com.min01.tickrateapi.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class TickrateArea extends TickrateData
{
	public final AABB aabb;
	
	public TickrateArea(ResourceKey<Level> dimension, TickrateTimer timer, AABB aabb, int priority) 
	{
		super(dimension, timer, priority);
		this.aabb = aabb;
	}
	
	public static TickrateArea read(CompoundTag tag)
	{
		AABB aabb = new AABB(tag.getDouble("MinX"), tag.getDouble("MinY"), tag.getDouble("MinZ"), tag.getDouble("MaxX"), tag.getDouble("MaxY"), tag.getDouble("MaxZ"));
		ResourceLocation location = ResourceLocation.parse(tag.getString("Dimension"));
		ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);
		return new TickrateArea(dimension, TickrateTimer.createWithTickrate(tag.getFloat("Tickrate")), aabb, tag.getInt("Priority"));
	}
	
	public void write(CompoundTag tag)
	{
		tag.putString("Dimension", this.dimension.location().toString());
		tag.putFloat("Tickrate", this.getTickrate());
		
		tag.putDouble("MinX", this.aabb.minX);
		tag.putDouble("MinY", this.aabb.minY);
		tag.putDouble("MinZ", this.aabb.minZ);
		tag.putDouble("MaxX", this.aabb.maxX);
		tag.putDouble("MaxY", this.aabb.maxY);
		tag.putDouble("MaxZ", this.aabb.maxZ);

		tag.putInt("Priority", this.priority);
	}
	
	public static TickrateArea read(FriendlyByteBuf buf)
	{
		ResourceLocation location = ResourceLocation.parse(buf.readUtf());
		ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);
		TickrateTimer timer = TickrateTimer.createWithTickrate(buf.readFloat());
		AABB aabb = new AABB(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
		return new TickrateArea(dimension, timer, aabb, buf.readInt());
	}
	
	public void write(FriendlyByteBuf buf)
	{
		buf.writeUtf(this.dimension.location().toString());
		buf.writeFloat(this.getTickrate());
		
		buf.writeDouble(this.aabb.minX);
		buf.writeDouble(this.aabb.minY);
		buf.writeDouble(this.aabb.minZ);
		buf.writeDouble(this.aabb.maxX);
		buf.writeDouble(this.aabb.maxY);
		buf.writeDouble(this.aabb.maxZ);
		
		buf.writeInt(this.priority);
	}
}
