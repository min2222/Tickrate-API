package com.min01.tickrateapi.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class TimerSyncPacket 
{
	private final UUID uuid;
	private final float tickRate;
	private final boolean reset;
	
	public TimerSyncPacket(UUID uuid, float tickRate, boolean reset) 
	{
		this.uuid = uuid;
		this.tickRate = tickRate;
		this.reset = reset;
	}

	public TimerSyncPacket(FriendlyByteBuf buf)
	{
		this.uuid = buf.readUUID();
		this.tickRate = buf.readFloat();
		this.reset = buf.readBoolean();
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.uuid);
		buf.writeFloat(this.tickRate);
		buf.writeBoolean(this.reset);
	}
	
	public static class Handler 
	{
		public static boolean onMessage(TimerSyncPacket message, Supplier<NetworkEvent.Context> ctx) 
		{
			ctx.get().enqueueWork(() ->
			{
				if(!message.reset)
				{
					if(!TickrateUtil.hasClientTimer(message.uuid))
					{
						TickrateUtil.setClientTimer(message.uuid, message.tickRate);
					}
					else
					{
						CustomTimer timer = TickrateUtil.getClientTimer(message.uuid);
						if(timer.tickrate != message.tickRate)
						{
							TickrateUtil.setClientTimer(message.uuid, message.tickRate);
						}
					}
				}
				else
				{
					if(TickrateUtil.hasClientTimer(message.uuid))
					{
						TickrateUtil.removeClientTimer(message.uuid);
					}
				}
			});
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
