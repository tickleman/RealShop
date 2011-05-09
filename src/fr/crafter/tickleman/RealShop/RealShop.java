package fr.crafter.tickleman.RealShop;

import java.util.HashMap;
import java.util.Iterator;

import fr.crafter.tickleman.RealPlugin.RealItemStack;

//######################################################################################## RealShop
public class RealShop
{

	/** Players will not be able to sell these items into this shop */
	public HashMap<String, Boolean> buyExclude = new HashMap<String, Boolean>();

	/** Players will be able to buy only these items into this shop */
	public HashMap<String, Boolean> buyOnly = new HashMap<String, Boolean>();

	/** Flags : infiniteBuy, infiniteSell, noDamagedItems, marketItemsOnly, belongToServer */
	public HashMap<String, Boolean> flags = new HashMap<String, Boolean>();

	/** Shop's optional name */
	public String name;
	
	/** Shop is opened if true */
	public boolean opened = true;

	/** Shop's position */
	public Integer posX;
	public Integer posY;
	public Integer posZ;

	/** Shop's owner name */
	public String player;

	/** Players will not be able to buy these items into this shop */
	public HashMap<String, Boolean> sellExclude = new HashMap<String, Boolean>();

	/** Players will be able to sell only these items into this shop */
	public HashMap<String, Boolean> sellOnly = new HashMap<String, Boolean>();

	/** Position is in this world name */
	public String world;
	
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

	//--------------------------------------------------------------------------------------- getFlag
	public boolean getFlag(String flag, boolean def)
	{
		Boolean result = flags.get(flag);
		if (result == null) {
			return def;
		} else {
			return result;
		}
	}

	//---------------------------------------------------------------------------------- hashMapToCsv
	/**
	 * Changes a HashMap indexed list to a buffer "1,5,9,19" 
	 */
	public static String hashMapToCsv(HashMap<String, Boolean> hashMap)
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
	public boolean isItemBuyAllowed(String typeIdDurability)
	{
		String typeIdDamage = RealItemStack.typeIdDurabilityWithoutDamage(typeIdDurability);
		return (
			(buyOnly.isEmpty() || (buyOnly.get(typeIdDamage) != null))
			&& (buyExclude.get(typeIdDamage) == null)
		);
	}

	//----------------------------------------------------------------------------- isItemSellAllowed
	/**
	 * Returns true if the player can sell an item into this shop
	 */
	public boolean isItemSellAllowed(String typeIdDurability)
	{
		String typeIdDamage = RealItemStack.typeIdDurabilityWithoutDamage(typeIdDurability);
		return (
			(sellOnly.isEmpty() || (sellOnly.get(typeIdDamage) != null))
			&& (sellExclude.get(typeIdDamage) == null)
		);
	}

	//--------------------------------------------------------------------------------------- setFlag
	public void setFlag(String flag, boolean value)
	{
		if (value) {
			flags.put(flag, value);
		} else {
			flags.remove(flag);
		}
	}

	//--------------------------------------------------------------------------------------- setFlag
	public void setFlag(String flag, String value)
	{
		setFlag(flag,
			value.toLowerCase().equals("on")
			|| value.toLowerCase().equals("yes")
			|| value.toLowerCase().equals("true")
			|| value.equals("1")
		);
	}

}
