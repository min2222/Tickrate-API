package com.min01.tickrateapi.network;

import java.util.function.Supplier;

import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.network.NetworkEvent;

public class UpdateDimensionTickratePacket 
{
	private final ResourceKey<Level> dimension;
	private final float tickrate;
	
	public UpdateDimensionTickratePacket(ResourceKey<Level> dimension, float tickrate) 
	{
		this.dimension = dimension;
		this.tickrate = tickrate;
	}

	public static UpdateDimensionTickratePacket read(FriendlyByteBuf buf)
	{
		return new UpdateDimensionTickratePacket(buf.readResourceKey(Registries.DIMENSION), buf.readFloat());
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeResourceKey(this.dimension);
		buf.writeFloat(this.tickrate);
	}
	
	public static boolean handle(UpdateDimensionTickratePacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isClient())
			{
				LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide()).filter(ClientLevel.class::isInstance).ifPresent(t -> 
				{
					if(message.tickrate == 20)
					{
						if(TickrateUtil.LEVEL_MAP.containsKey(message.dimension))
						{
							TickrateUtil.LEVEL_MAP.remove(message.dimension);
						}
					}
					else
					{
						TickrateUtil.LEVEL_MAP.put(message.dimension, new CustomTimer(message.tickrate, 0L));
					}
				});
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
