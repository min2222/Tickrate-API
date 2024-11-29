package com.min01.tickrateapi.network;

import java.util.function.Supplier;

import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class TimeStopSyncPacket 
{
	private final ResourceKey<Level> dimension;
	private final boolean isStop;
	
	public TimeStopSyncPacket(ResourceKey<Level> dimension, boolean isStop) 
	{
		this.dimension = dimension;
		this.isStop = isStop;
	}

	public TimeStopSyncPacket(FriendlyByteBuf buf)
	{
		this.dimension = buf.readResourceKey(Registry.DIMENSION_REGISTRY);
		this.isStop = buf.readBoolean();
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeResourceKey(this.dimension);
		buf.writeBoolean(this.isStop);
	}
	
	public static class Handler 
	{
		public static boolean onMessage(TimeStopSyncPacket message, Supplier<NetworkEvent.Context> ctx) 
		{
			ctx.get().enqueueWork(() ->
			{
				if(message.isStop)
				{
					if(!TickrateUtil.DIMENSIONS.contains(message.dimension))
					{
						TickrateUtil.DIMENSIONS.add(message.dimension);
					}
				}
				else
				{
					if(TickrateUtil.DIMENSIONS.contains(message.dimension))
					{
						TickrateUtil.DIMENSIONS.remove(message.dimension);
					}
				}
			});
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
