package com.min01.tickrateapi.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.network.NetworkEvent;

public class UpdateTickratePacket 
{
	private final UUID uuid;
	private final boolean excluded;
	private final boolean excludeSubEntities;
	private final boolean shouldChangeSubEntities;
	private final float baseTickrate;
	private final float currentTickrate;
	
	public UpdateTickratePacket(UUID uuid, boolean excluded, boolean excludeSubEntities, boolean changeSubEntities, float baseTickrate, float currentTickrate) 
	{
		this.uuid = uuid;
		this.excluded = excluded;
		this.excludeSubEntities = excludeSubEntities;
		this.shouldChangeSubEntities = changeSubEntities;
		this.baseTickrate = baseTickrate;
		this.currentTickrate = baseTickrate;
	}

	public static UpdateTickratePacket read(FriendlyByteBuf buf)
	{
		return new UpdateTickratePacket(buf.readUUID(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readFloat());
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.uuid);
		buf.writeBoolean(this.excluded);
		buf.writeBoolean(this.excludeSubEntities);
		buf.writeBoolean(this.shouldChangeSubEntities);
		buf.writeFloat(this.baseTickrate);
		buf.writeFloat(this.currentTickrate);
	}
	
	public static boolean handle(UpdateTickratePacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isClient())
			{
				LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide()).filter(ClientLevel.class::isInstance).ifPresent(t -> 
				{
					Entity entity = TickrateUtil.getEntityByUUID(t, message.uuid);
					ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
					cap.exclude(message.excluded);
					cap.excludeSubEntities(message.excludeSubEntities);
					cap.changeSubEntities(message.shouldChangeSubEntities);
					cap.setBaseTickrate(message.baseTickrate);
					cap.setTickrate(message.currentTickrate);
				});
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
