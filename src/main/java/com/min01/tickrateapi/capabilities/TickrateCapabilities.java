package com.min01.tickrateapi.capabilities;

import net.neoforged.neoforge.capabilities.EntityCapability;

public class TickrateCapabilities
{
	public static final EntityCapability<ITickrateCapability, Void> TICKRATE = EntityCapability.createVoid(ITickrateCapability.ID, ITickrateCapability.class);
}
