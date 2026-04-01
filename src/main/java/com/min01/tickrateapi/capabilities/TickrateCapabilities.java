package com.min01.tickrateapi.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class TickrateCapabilities
{
	public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
    	Entity entity = event.getObject();
		event.addCapability(TickrateCapabilityImpl.ID, new TickrateCapabilityImpl(entity));
	}
}
