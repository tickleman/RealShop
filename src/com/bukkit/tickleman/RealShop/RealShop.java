package com.bukkit.tickleman.RealShop;

import java.util.HashMap;
import java.util.Iterator;

//######################################################################################## RealShop
public class RealShop
{

	/** Shop basics */
	public String world;
	public Integer posX;
	public Integer posY;
	public Integer posZ;
	public String player;

	/** Players will be able to buy only these items into this shop */
	public HashMap<Integer, Boolean> buyOnly = new HashMap<Integer, Boolean>();

	/** Players will be able to sell only these items into this shop */
	public HashMap<Integer, Boolean> sellOnly = new HashMap<Integer, Boolean>();

	/** Players will not be able to sell these items into this shop */
	public HashMap<Integer, Boolean> buyExclude = new HashMap<Integer, Boolean>();

	/** Players will not be able to buy these items into this shop */
	public HashMap<Integer, Boolean> sellExclude = new HashMap<Integer, Boolean>();

	//-------------------------------------------------------------------------------------- RealShop
	public RealShop(String world, Integer posX, Integer posY, Integer posZ, String player)
	{
		this.world = world;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.player = player;
	}

	//---------------------------------------------------------------------------------- csvToHashMap
	/**
	 * Changes a buffer "1,5,9,19" into a HashMap indexed list
	 */
	public static HashMap<Integer, Boolean> csvToHashMap(String buffer)
	{
		HashMap<Integer, Boolean> hashMap = new HashMap<Integer, Boolean>();
		String[] typeIds = buffer.split(",");
		for (int i = 0; i < typeIds.length; i++) {
			hashMap.put(Integer.parseInt(typeIds[i]), true);
		}
		return hashMap;
	}

	//---------------------------------------------------------------------------------- hashMapToCsv
	/**
	 * Changes a HashMap indexed list to a buffer "1,5,9,19" 
	 */
	public static String HashMapToCsv(HashMap<Integer, Boolean> hashMap)
	{
		String csv = "";
		Iterator<Integer> iterator = hashMap.keySet().iterator();
		if (iterator.hasNext()) {
			csv = iterator.next().toString();
		}
		while (iterator.hasNext()) {
			csv += "," + iterator.next().toString();
		}
		return csv;
	}

	//------------------------------------------------------------------------------ isItemBuyAllowed
	/**
	 * Returns true if the player can buy an item into this shop
	 */
	public boolean isItemBuyAllowed(int typeId)
	{
		return (
			((buyOnly.size() == 0) || (buyOnly.get(typeId) != null))
			&& (buyExclude.get(typeId) == null)
		);
	}

	//----------------------------------------------------------------------------- isItemSellAllowed
	/**
	 * Returns true if the player can sell an item into this shop
	 */
	public boolean isItemSellAllowed(int typeId)
	{
		return (
			((sellOnly.size() == 0) || (sellOnly.get(typeId) != null))
			&& (sellExclude.get(typeId) == null)
		);
	}

}
