package com.bukkit.tickleman.RealShop;

import org.bukkit.block.Block;

import com.bukkit.tickleman.RealPlugin.RealChest;
import com.bukkit.tickleman.RealPlugin.RealItemStackHashMap;

//#################################################################################### InChestState
public class RealInChestState
{

	public boolean inChest = false;
	
	public Block block;
	public RealChest chest;
	public RealItemStackHashMap itemStackHashMap;

	public long lastX = 0;
	public long lastZ = 0;

	public long enterTime;

}
