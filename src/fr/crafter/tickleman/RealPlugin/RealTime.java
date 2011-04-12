package fr.crafter.tickleman.RealPlugin;

import org.bukkit.World;

public class RealTime
{

	//--------------------------------------------------------------------------------- worldToMcTime
	/**
	 * Return the time in number of seconds from the beginning of the world
	 * Notice that the play begins at 06:00AM of the day 0 of the year 0
	 */
	public static long worldToMcTime(World world)
	{
		return Math.round((double)(world.getFullTime() - 6000) * 3.6);
	}

	//------------------------------------------------------------------------------- worldToRealTime
	/**
	 * Return the time in number of seconds from the beginning of the world
	 * number of seconds of our real world (make the 24 hours -> 20 minutes translation)
	 */
	public static long worldToRealTime(World world)
	{
		return Math.round((double)(world.getFullTime() - 6000) / (double)20);
	}

}
