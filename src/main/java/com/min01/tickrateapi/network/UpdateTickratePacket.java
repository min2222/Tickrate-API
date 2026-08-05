package com.min01.tickrateapi.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class UpdateTickratePacket 
{
	private final UUID uuid;
	private final int priority;
	private final float baseTickrate;
	private final float currentTickrate;
	
	public UpdateTickratePacket(UUID uuid, int priority, float baseTickrate, float currentTickrate) 
	{
		this.uuid = uuid;
		this.priority = priority;
		this.baseTickrate = baseTickrate;
		this.currentTickrate = baseTickrate;
	}

	public static UpdateTickratePacket read(FriendlyByteBuf buf)
	{
		return new UpdateTickratePacket(buf.readUUID(), buf.readInt(), buf.readFloat(), buf.readFloat());
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.uuid);
		buf.writeInt(this.priority);
		buf.writeFloat(this.baseTickrate);
		buf.writeFloat(this.currentTickrate);
	}
	
	public static boolean handle(UpdateTickratePacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isClient())
			{
				TickrateUtil.getClientLevel(t -> 
				{
					Entity entity = TickrateUtil.getEntityByUUID(t, message.uuid);
					ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
					cap.sync(message.priority, message.baseTickrate, message.currentTickrate);
				});
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}