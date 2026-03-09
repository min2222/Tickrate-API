package com.min01.tickrateapi.network;

import java.util.Iterator;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.network.NetworkEvent;

public class UpdateAreaTickratePacket 
{
	private final AABB aabb;
	private final float tickrate;
	
	public UpdateAreaTickratePacket(AABB aabb, float tickrate) 
	{
		this.aabb = aabb;
		this.tickrate = tickrate;
	}

	public static UpdateAreaTickratePacket read(FriendlyByteBuf buf)
	{
		return new UpdateAreaTickratePacket(new AABB(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat());
	}

	public void write(FriendlyByteBuf buf)
	{
		buf.writeDouble(this.aabb.minX);
		buf.writeDouble(this.aabb.minY);
		buf.writeDouble(this.aabb.minZ);
		buf.writeDouble(this.aabb.maxX);
		buf.writeDouble(this.aabb.maxY);
		buf.writeDouble(this.aabb.maxZ);
		buf.writeFloat(this.tickrate);
	}
	
	public static boolean handle(UpdateAreaTickratePacket message, Supplier<NetworkEvent.Context> ctx) 
	{
		ctx.get().enqueueWork(() ->
		{
			if(ctx.get().getDirection().getReceptionSide().isClient())
			{
				LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide()).filter(ClientLevel.class::isInstance).ifPresent(t -> 
				{
					if(message.tickrate == 20)
					{
						for(Iterator<Pair<AABB, Float>> itr = TickrateUtil.AABB_LIST.iterator(); itr.hasNext();)
						{
							Pair<AABB, Float> next = itr.next();
							if(next.getLeft().equals(message.aabb))
							{
								itr.remove();
							}
						}
					}
					else
					{
						TickrateUtil.AABB_LIST.add(Pair.of(message.aabb, message.tickrate));
					}
				});
			}
		});
		ctx.get().setPacketHandled(true);
		return true;
	}
}
