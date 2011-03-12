package fr.crafter.tickleman.RealShop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

//######################################################################################## RealShop
public class RealShop
{

	/** Shop basics : position and player */
	public String world;
	public Integer posX;
	public Integer posY;
	public Integer posZ;
	public String player;
	public String name;

	/** Players will be able to buy only these items into this shop */
	public HashMap<String, Boolean> buyOnly = new HashMap<String, Boolean>();

	/** Players will be able to sell only these items into this shop */
	public HashMap<String, Boolean> sellOnly = new HashMap<String, Boolean>();

	/** Players will not be able to sell these items into this shop */
	public HashMap<String, Boolean> buyExclude = new HashMap<String, Boolean>();

	/** Players will not be able to buy these items into this shop */
	public HashMap<String, Boolean> sellExclude = new HashMap<String, Boolean>();

	/** Flags : infiniteBuy, infiniteSell, noDamagedItems, marketItemsOnly, belongToServer */
	public ArrayList<String> flags = new ArrayList<String>();

	//-------------------------------------------------------------------------------------- RealShop
	public RealShop(String world, Integer posX, Integer posY, Integer posZ, String player)
	{
		this.world = world;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.player = player;
		this.name = "";
	}

	//---------------------------------------------------------------------------------- csvToHashMap
	/**
	 * Changes a buffer "1,5,9,19" into a HashMap indexed list
	 */
	public static HashMap<String, Boolean> csvToHashMap(String buffer)
	{
		HashMap<String, Boolean> hashMap = new HashMap<String, Boolean>();
		String[] typeIds = buffer.split(",");
		for (int i = 0; i < typeIds.length; i++) {
			if (typeIds[i].trim().length() > 0) {
				try {
				hashMap.put(typeIds[i], true);
				} catch (Exception e) {
				}
			}
		}
		return hashMap;
	}

	//---------------------------------------------------------------------------------- hashMapToCsv
	/**
	 * Changes a HashMap indexed list to a buffer "1,5,9,19" 
	 */
	public static String HashMapToCsv(HashMap<String, Boolean> hashMap)
	{
		String csv = "";
		Iterator<String> iterator = hashMap.keySet().iterator();
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
	public boolean isItemBuyAllowed(String typeIdDamage)
	{
		return (
			((buyOnly.size() == 0) || (buyOnly.get(typeIdDamage) != null))
			&& (buyExclude.get(typeIdDamage) == null)
		);
	}

	//----------------------------------------------------------------------------- isItemSellAllowed
	/**
	 * Returns true if the player can sell an item into this shop
	 */
	public boolean isItemSellAllowed(String typeIdDamage)
	{
		return (
			((sellOnly.size() == 0) || (sellOnly.get(typeIdDamage) != null))
			&& (sellExclude.get(typeIdDamage) == null)
		);
	}

}
