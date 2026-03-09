package com.min01.tickrateapi.misc;

@SuppressWarnings("removal")
public class TickrateSecurityManager extends SecurityManager
{
	public Class<?>[] getContext()
	{
		return this.getClassContext();
	}
}
