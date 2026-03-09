package com.min01.tickrateapi.capabilities;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;

public class TickrateCapabilities
{
	public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
    	Entity entity = event.getObject();
    	TickrateCapabilityImpl cap = new TickrateCapabilityImpl();
    	cap.setEntity(entity);
		event.addCapability(TickrateCapabilityImpl.ID, cap);
	}
}
