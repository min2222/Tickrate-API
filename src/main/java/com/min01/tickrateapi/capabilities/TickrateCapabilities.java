package com.min01.tickrateapi.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class TickrateCapabilities
{
	public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		event.addCapability(TickrateCapabilityImpl.ID, new TickrateCapabilityImpl());
	}
}
