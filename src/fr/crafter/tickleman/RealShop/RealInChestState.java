package fr.crafter.tickleman.RealShop;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import fr.crafter.tickleman.RealPlugin.RealChest;
import fr.crafter.tickleman.RealPlugin.RealItemStackHashMap;

//#################################################################################### InChestState
public class RealInChestState
{

	/**
	 * the block object of the clicked chest
	 */
	public Block block;

	/**
	 * the chest object
	 */
	public RealChest chest;

	/**
	 * system time when the player entered the shop
	 */
	public long enterTime;
	
	/**
	 * true if a player is into the chest, else false
	 */
	public boolean inChest = false;

	/**
	 * Backup of the chest's inventory
	 */
	public ArrayList<ItemStack[]> chestInventoryBackup = null;

	/**
	 * Stores the content of the chest at the beginning of the transaction
	 */
	public RealItemStackHashMap chestItemStackHashMap;

	/**
	 * Backup of the player's inventory 
	 */
	public ArrayList<ItemStack[]> playerInventoryBackup = null;

	/**
	 * last x position of the player
	 */
	public long lastX = 0;

	/**
	 * last z position of the player
	 */
	public long lastZ = 0;

}
