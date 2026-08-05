package com.min01.tickrateapi.network;

import java.util.function.Supplier;

import com.min01.tickrateapi.api.TickrateArea;
import com.min01.tickrateapi.util.TickrateUtil;
import com.min01.tickrateapi.world.TickrateClientData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class AddTickrateAreaPacket 
{
	private final TickrateArea area;
	
	public AddTickrateAreaPacket(TickrateArea area) 
	{
		this.area = area;
	}

	public static AddTickrateAreaPacket read(FriendlyByteBuf buf)
	{
		return new AddTickrateAreaPacket(TickrateArea.read(buf));
	}

	public void write(FriendlyByteBuf buf)
	{
		this.area.write(buf);
	}
	
	public static boolean handle(AddTickrateAreaPacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isClient())
			{
				TickrateUtil.getClientLevel(t -> 
				{
					TickrateClientData.addArea(message.area);
				});
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}