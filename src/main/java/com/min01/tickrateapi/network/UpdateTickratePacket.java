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
	private final ITickrateCapability cap;
	
	public UpdateTickratePacket(UUID uuid, ITickrateCapability cap) 
	{
		this.uuid = uuid;
		this.cap = cap;
	}

	public static UpdateTickratePacket read(FriendlyByteBuf buf)
	{
		UUID uuid = buf.readUUID();
		ITickrateCapability cap = new TickrateCapabilityImpl();
		cap.deserializeNBT(buf.readNbt());
		return new UpdateTickratePacket(uuid, cap);
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.uuid);
		buf.writeNbt(this.cap.serializeNBT());
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
					cap.sync(message.cap.isExcluded(), message.cap.shouldChangeSubEntities(), message.cap.shouldExcludeSubEntities(), message.cap.getBaseTimer().tickrate, message.cap.getTickrate());
				});
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
