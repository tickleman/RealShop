package com.bukkit.tickleman.RealShop;

import com.bukkit.tickleman.RealPlugin.RealChest;
import com.bukkit.tickleman.RealPlugin.RealItemStackHashMap;

//#################################################################################### InChestState
public class RealInChestState
{

	public boolean inChest = false;
	
	public RealChest chest;
	public RealItemStackHashMap itemStackHashMap;

	public long lastX = 0;
	public long lastZ = 0;

}
