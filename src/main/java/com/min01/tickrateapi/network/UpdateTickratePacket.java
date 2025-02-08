package com.min01.tickrateapi.network;
import java.util.UUID;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilities;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UpdateTickratePacket implements CustomPacketPayload
{
	private final UUID uuid;
	private final ITickrateCapability cap;
	private final boolean reset;

	public static final CustomPacketPayload.Type<UpdateTickratePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TickrateAPI.MODID, "update_tickrate"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTickratePacket> STREAM_CODEC = CustomPacketPayload.codec(UpdateTickratePacket::encode, UpdateTickratePacket::new);

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
		cap.deserializeNBT(null, buf.readNbt());
		this.cap = cap;
		this.reset = buf.readBoolean();
	}
	public void encode(FriendlyByteBuf buf)
	{
		buf.writeUUID(this.uuid);
		buf.writeNbt(this.cap.serializeNBT(null));
		buf.writeBoolean(this.reset);
	}

	public static void handle(UpdateTickratePacket message, IPayloadContext ctx)
	{
		ctx.enqueueWork(() ->
		{
			LogicalSidedProvider.CLIENTWORLD.get(LogicalSide.CLIENT).filter(ClientLevel.class::isInstance).ifPresent(t -> 
			{
				Entity entity = TickrateUtil.getEntityByUUID(t, message.uuid);
				ITickrateCapability cap = entity.getCapability(TickrateCapabilities.TICKRATE);
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
			});
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
