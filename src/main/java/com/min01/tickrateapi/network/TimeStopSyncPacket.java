package com.min01.tickrateapi.network;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TimeStopSyncPacket implements CustomPacketPayload
{
	private final ResourceKey<Level> dimension;
	private final boolean isStop;

	public static final CustomPacketPayload.Type<TimeStopSyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TickrateAPI.MODID, "time_stop_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TimeStopSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(TimeStopSyncPacket::encode, TimeStopSyncPacket::new);

	public TimeStopSyncPacket(ResourceKey<Level> dimension, boolean isStop)
	{
		this.dimension = dimension;
		this.isStop = isStop;
	}

	public TimeStopSyncPacket(FriendlyByteBuf buf)
	{
		this.dimension = buf.readResourceKey(Registries.DIMENSION);
		this.isStop = buf.readBoolean();
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeResourceKey(this.dimension);
		buf.writeBoolean(this.isStop);
	}

	public static void handle(TimeStopSyncPacket message, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
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
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
