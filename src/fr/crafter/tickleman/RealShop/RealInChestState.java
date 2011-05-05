package fr.crafter.tickleman.RealShop;

import org.bukkit.block.Block;

import fr.crafter.tickleman.RealPlugin.RealChest;
import fr.crafter.tickleman.RealPlugin.RealItemStackHashMap;

//#################################################################################### InChestState
public class RealInChestState
{

	public Block block;
	
	public RealChest chest;

	public long enterTime;
	
	public boolean inChest = false;
	
	public RealItemStackHashMap itemStackHashMap;

	public long lastX = 0;

	public long lastZ = 0;

}
