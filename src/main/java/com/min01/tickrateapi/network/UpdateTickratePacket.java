package com.min01.tickrateapi.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilities;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class UpdateTickratePacket 
{
	private final UUID uuid;
	private final ITickrateCapability cap;
	private final boolean reset;
	
	public UpdateTickratePacket(UUID uuid, ITickrateCapability cap, boolean reset) 
	{
		this.uuid = uuid;
		this.cap = cap;
		this.reset = reset;
	}

	public UpdateTickratePacket(FriendlyByteBuf buf)
	{
		this.uuid = buf.readUUID();
		ITickrateCapability cap = new TickrateCapabilityImpl();
		cap.deserializeNBT(buf.readNbt());
		this.cap = cap;
		this.reset = buf.readBoolean();
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.uuid);
		buf.writeNbt(this.cap.serializeNBT());
		buf.writeBoolean(this.reset);
	}
	
	public static class Handler 
	{
		public static boolean onMessage(UpdateTickratePacket message, Supplier<NetworkEvent.Context> ctx) 
		{
			ctx.get().enqueueWork(() ->
			{
				if(ctx.get().getDirection().getReceptionSide().isClient())
				{
					Minecraft mc = Minecraft.getInstance();
					Entity entity = TickrateUtil.getEntityByUUID(mc.level, message.uuid);
					ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE).orElse(new TickrateCapabilityImpl());
					if(message.reset)
					{
						cap.resetTickrate();
					}
					else
					{
						cap.setTimer(message.cap.getTimer());
						cap.exclude(message.cap.isExcluded());
						cap.excludeSubEntities(message.cap.shouldExcludeSubEntities());
					}
				}
			});
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
