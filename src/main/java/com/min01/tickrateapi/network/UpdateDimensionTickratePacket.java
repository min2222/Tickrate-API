package com.min01.tickrateapi.network;

import java.util.function.Supplier;

import com.min01.tickrateapi.api.TickrateDimension;
import com.min01.tickrateapi.util.TickrateUtil;
import com.min01.tickrateapi.world.TickrateClientData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class UpdateDimensionTickratePacket 
{
	private final TickrateDimension dimension;
	
	public UpdateDimensionTickratePacket(TickrateDimension dimension) 
	{
		this.dimension = dimension;
	}

	public static UpdateDimensionTickratePacket read(FriendlyByteBuf buf)
	{
		return new UpdateDimensionTickratePacket(TickrateDimension.read(buf));
	}

	public void write(FriendlyByteBuf buf)
	{
		this.dimension.write(buf);
	}
	
	public static boolean handle(UpdateDimensionTickratePacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isClient())
			{
				TickrateUtil.getClientLevel(t -> 
				{
					TickrateClientData.setDimensionTickrate(message.dimension);
				});
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}