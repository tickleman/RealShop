package fr.crafter.tickleman.RealPlugin;

import org.bukkit.block.Block;

//####################################################################################### RealBlock
public class RealBlock
{

	//------------------------------------------------------------------------------------- fromStrId
	public static Block fromStrId(RealPlugin plugin, String str)
	{
		String[] coords = str.split(";");
		return plugin.getServer().getWorld(coords[0]).getBlockAt(
			Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3])
		);
	}

	//----------------------------------------------------------------------------------------- strId
	public static String strId(Block block)
	{
		return block.getWorld().getName() + ";" + block.getX() + ";" + block.getY() + ";" + block.getZ();
	}

}
