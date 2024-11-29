package com.min01.tickrateapi.network;
import java.util.UUID;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TimerSyncPacket implements CustomPacketPayload
{
	private final UUID uuid;
	private final float tickRate;
	private final boolean reset;

	public static final CustomPacketPayload.Type<TimerSyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TickrateAPI.MODID, "timer_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TimerSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(TimerSyncPacket::encode, TimerSyncPacket::new);

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

	public static void handle(TimerSyncPacket message, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
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
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
