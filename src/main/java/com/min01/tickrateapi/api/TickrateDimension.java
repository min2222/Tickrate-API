package com.min01.tickrateapi.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class TickrateDimension extends TickrateData
{
	public TickrateDimension(ResourceKey<Level> dimension, TickrateTimer timer, int priority) 
	{
		super(dimension, timer, priority);
	}
	
	public static TickrateDimension read(CompoundTag tag)
	{
		ResourceLocation location = ResourceLocation.parse(tag.getString("Dimension"));
		ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);
		return new TickrateDimension(dimension, TickrateTimer.createWithTickrate(tag.getFloat("Tickrate")), tag.getInt("Priority"));
	}
	
	public void write(CompoundTag tag)
	{
		tag.putString("Dimension", this.dimension.location().toString());
		tag.putFloat("Tickrate", this.getTickrate());
		tag.putInt("Priority", this.priority);
	}
	
	public static TickrateDimension read(FriendlyByteBuf buf)
	{
		ResourceLocation location = ResourceLocation.parse(buf.readUtf());
		ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);
		float tickrate = buf.readFloat();
		return new TickrateDimension(dimension, TickrateTimer.createWithTickrate(tickrate), buf.readInt());
	}
	
	public void write(FriendlyByteBuf buf)
	{
		buf.writeUtf(this.dimension.location().toString());
		buf.writeFloat(this.getTickrate());
		buf.writeInt(this.priority);
	}
}
