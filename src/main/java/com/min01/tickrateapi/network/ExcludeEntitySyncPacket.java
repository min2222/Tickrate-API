package com.min01.tickrateapi.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class ExcludeEntitySyncPacket 
{
	private final UUID uuid;
	private final boolean isExclude;
	private final boolean excludeSubEntities;
	
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
	
	public static class Handler 
	{
		public static boolean onMessage(ExcludeEntitySyncPacket message, Supplier<NetworkEvent.Context> ctx) 
		{
			ctx.get().enqueueWork(() ->
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
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
