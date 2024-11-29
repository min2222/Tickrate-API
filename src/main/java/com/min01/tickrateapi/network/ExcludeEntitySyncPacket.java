package com.min01.tickrateapi.network;

import java.util.UUID;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ExcludeEntitySyncPacket implements CustomPacketPayload
{
	private final UUID uuid;
	private final boolean isExclude;
	private final boolean excludeSubEntities;

	public static final CustomPacketPayload.Type<ExcludeEntitySyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TickrateAPI.MODID, "exclude_entity_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ExcludeEntitySyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ExcludeEntitySyncPacket::encode, ExcludeEntitySyncPacket::new);

	public ExcludeEntitySyncPacket(UUID uuid, boolean isExclude, boolean excludeSubEntities)
	{
		this.uuid = uuid;
		this.isExclude = isExclude;
		this.excludeSubEntities = excludeSubEntities;
	}

	public ExcludeEntitySyncPacket(FriendlyByteBuf buf)
	{
		this.uuid = buf.readUUID();
		this.isExclude = buf.readBoolean();
		this.excludeSubEntities = buf.readBoolean();
	}

	public void encode(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.uuid);
		buf.writeBoolean(this.isExclude);
		buf.writeBoolean(this.excludeSubEntities);
	}

	public static void handle(ExcludeEntitySyncPacket message, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
		{
			if(message.isExclude)
			{
				if(!TickrateUtil.EXCLUDED_ENTITIES.containsKey(message.uuid))
				{
					TickrateUtil.EXCLUDED_ENTITIES.put(message.uuid, message.excludeSubEntities);
				}
			}
			else
			{
				if(TickrateUtil.EXCLUDED_ENTITIES.containsKey(message.uuid))
				{
					TickrateUtil.EXCLUDED_ENTITIES.remove(message.uuid);
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
