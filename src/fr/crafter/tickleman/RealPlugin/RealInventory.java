package fr.crafter.tickleman.RealPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


//################################################################################### RealInventory
/*
 * This Inventory object manages :
 * - players inventory (excluding special armor slots)
 * - chest FULL inventory (for small AND big chests)
 * This enable to :
 * - remove items from the inventory (even if from big chest)
 * - add items to the inventory (even if in big chest)
 * - move items from an inventory to another one, using add and remove features
 */
public class RealInventory
{

	/**
	 * for chests only : need to update their inventory each time it is modified
	 */
	private Chest[] chests;

	/**
	 * Error log : when an item could not be removed, then it's logged here
	 */
	public ArrayList<RealItemStack> errorLog = new ArrayList<RealItemStack>();

	/**
	 * content is a collection of arrays of ItemStack objects
	 * this can handle :
	 * - small chest inventory (one inventory)
	 * - big chest inventory (two inventories)
	 * - player inventory (one inventory)
	 * - any further inventory settings may be added easily (I hope)
	 */
	public Inventory[] inventories;

	/**
	 * Backup ItemStack[] for each inventory
	 * Set with backup(), and used by restore() to restore
	 */
	private ArrayList<ItemStack[]> itemStackBackup;

	//--------------------------------------------------------------------------------- RealInventory
	/**
	 * Constructor #0 : an empty inventory, for manual fill 
	 */
	private RealInventory()
	{
		clear();
	}

	//--------------------------------------------------------------------------------- RealInventory
	/**
	 * Constructor #1 : fill inventory with all Player's inventory slots 
	 */
	private RealInventory(Player player)
	{
		loadFromPlayerInventory(player);
	}

	//--------------------------------------------------------------------------------- RealInventory
	/**
	 * Constructor #2 : fill inventory with all RealChest's inventory slots
	 */
	private RealInventory(RealChest chest)
	{
		loadFromRealChestInventory(chest);
	}

	//------------------------------------------------------------------------------------------- add
	public boolean add(int typeId, int amount)
	{
		return add(typeId, amount, (short)0);
	}

	//------------------------------------------------------------------------------------------- add
	/**
	 * Add an amount of item into the inventory
	 * Return false if not enough space in inventory
	 */
	public boolean add(int typeId, int amount, short durability)
	{
		backup();
		for (int i = 0; i < inventories.length; i++) {
			HashMap<Integer, ItemStack> remaining = inventories[i].addItem(
				new ItemStack(typeId, amount, durability)
			);
			update();
			if (remaining.isEmpty()) {
				return true;
			} else {
				amount = remaining.get(0).getAmount();
			}
		}
		restore();
		return false;
	}

	//-------------------------------------------------------------------------------------- contains
	/**
	 * Return true if the inventory contains at least amount of the given material
	 * (amount is the sum of multiple slots if needed)
	 */
	public boolean contains(int typeId, int amount)
	{
		for (int i = 0; i < inventories.length; i++) {
			HashMap<Integer, ? extends ItemStack> items = inventories[i].all(typeId);
			Iterator<? extends ItemStack> iterator = items.values().iterator();
			while (iterator.hasNext()) {
				amount -= iterator.next().getAmount();
				if (amount <= 0) {
					return true;
				}
			}
		}
		return false;
	}

	//---------------------------------------------------------------------------------------- backup
	/**
	 * Backups RealInventory to a copy of the inventory
	 */
	public ArrayList<ItemStack[]> backup()
	{
		itemStackBackup = new ArrayList<ItemStack[]>();
		for (int i = 0; i < inventories.length; i++) {
			itemStackBackup.add(inventories[i].getContents().clone());
		}
		return itemStackBackup;
	}

	//----------------------------------------------------------------------------------------- clear
	/**
	 * Clear inventory, resetting its collection of arrays of ItemStack objects
	 */
	private void clear()
	{
		chests = new Chest[2];
		inventories = new Inventory[0];
	}

	//---------------------------------------------------------------------------------------- create
	/**
	 * Static constructor #1
	 * - return the RealInventory linked to a player's inventory 
	 *   (inventory contains full player's inventory, including armor slots) 
	 */
	public static RealInventory create(Player player)
	{
		return new RealInventory(player);
	}

	//---------------------------------------------------------------------------------------- create
	/**
	 * Static constructor #2
	 * - return the RealInventory linked to a chest's inventory
	 *   (works with both small and big chests) 
	 */
	public static RealInventory create(RealChest chest)
	{
		return new RealInventory(chest);
	}

	//-------------------------------------------------------------------------------------- copyFrom
	/**
	 * Copy a RealInventory content from another one
	 * result ItemStack objects are newly created (independent, thus)
	 * but all the link to the original inventories are kept
	 * source and destination should have exactly the same size (or this will crash)
	 */
	/*
	private void copyFrom(RealInventory source)
	{
		for (int i = 0; i < source.inventories.length; i++) {
			inventories[i].setContents(source.inventories[i].getContents().clone());
		}
	}
	*/

	//----------------------------------------------------------------------- loadFromPlayerInventory
	/**
	 * Clear current inventory (if set) and link RealInventory to a player's inventory
	 * (excluding armor slots)
	 */
	private void loadFromPlayerInventory(Player player)
	{
		clear();
		inventories = new Inventory[1];
		inventories[0] = player.getInventory();
	}

	//-------------------------------------------------------------------- loadFromRealChestInventory
	/**
	 * Clear current inventory (if set) and link RealInventory to a chest's inventory
	 * (including the neighbor chest inventory if big chest)
	 */
	private void loadFromRealChestInventory(RealChest chest)
	{
		clear();
		chests[0] = chest.getMainChest();
		chests[1] = chest.getNeighborChest();
		inventories = new Inventory[(chests[1] == null) ? 1 : 2];
		inventories[0] = chests[0].getInventory();
		if (chests[1] != null) {
			inventories[1] = chests[1].getInventory();
		}
	}

	//--------------------------------------------------------------------------------------- restore
	/**
	 * Restore inventory from last backup
	 */
	public void restore()
	{
		restore(itemStackBackup);
	}

	//--------------------------------------------------------------------------------------- restore
	/**
	 * Restore inventory from last backup
	 */
	public void restore(ArrayList<ItemStack[]> itemStackBackup)
	{
		for (int i = 0; i < inventories.length; i++) {
			inventories[i].setContents(itemStackBackup.get(i));
		}
		update();
	}

	//-------------------------------------------------------------------------------------- moveFrom
	public boolean moveFrom(RealInventory source, int typeId, int amount, short durability)
	{
		ArrayList<ItemStack[]> itemStackBackup = source.backup();
		if (source.remove(typeId, amount, durability)) {
			if (add(typeId, amount, durability)) {
				return true;
			}
			source.restore(itemStackBackup);
		}
		return false;
	}

	//-------------------------------------------------------------------------------------- moveFrom
	public boolean moveFrom(RealInventory source, ItemStack itemStack)
	{
		return moveFrom(
			source, itemStack.getTypeId(), itemStack.getAmount(), itemStack.getDurability()
		);
	}


	//---------------------------------------------------------------------------------------- remove
	/**
	 * Remove an amount of item from the inventory
	 * Return false if the inventory does not contain the given amount (then nothing is removed)
	 */
	public boolean remove(int typeId, int amount)
	{
		return remove(typeId, amount, null);
	}

	//---------------------------------------------------------------------------------------- remove
	/**
	 * Remove an amount of item from the inventory
	 * Return false if the inventory does not contain the given amount (then nothing is removed)
	 */
	public boolean remove(int typeId, int amount, Short durability)
	{
		// backup inventories
		backup();
		int backupAmount = amount;
		// scan inventories
		for (int i = 0; i < inventories.length; i++) {
			Inventory inventory = inventories[i];
			ItemStack[] itemStackList = inventory.getContents();
			for (int j = 0; j < itemStackList.length; j++) {
				ItemStack itemStack = itemStackList[j];
				if (
					(itemStack != null)
					&& (itemStack.getTypeId() == typeId)
					&& ((durability == null) || (durability.equals(itemStack.getDurability())))
				) {
					int itemAmount = itemStack.getAmount();
					if (amount < itemAmount) {
						// remove all remaining quantity from slot
						itemStack.setAmount(itemAmount - amount);
						update();
						return true;
					} else {
						// empty slot
						inventory.clear(j);
						update();
						amount -= itemAmount;
						if (amount == 0) {
							return true;
						}
					}
				}
			}
		}
		// if all quantity has not been moved, then rollback
		restore();
		errorLog.add(new RealItemStack(typeId, backupAmount, durability));
		return false;
	}

	//---------------------------------------------------------------------------- storeRealItemStack
	/**
	 * Store ItemStack items descriptions into inventory
	 * - positive quantities additions, negative quantities removal
	 * - if reverse is set to true, negative quantities addition, positive quantities removal
	 * - if any add / remove error, then cancel the whole operation and return false
	 * - return true if everything is OK
	 */
	public boolean storeRealItemStack(RealItemStack itemStack, boolean reverse)
	{
		boolean ok = true;
		int amount = (reverse ? -itemStack.getAmount() : itemStack.getAmount());
		if (amount < 0) {
			ok = remove(itemStack.getTypeId(), -amount, itemStack.getDurability());
		} else {
			ok = add(itemStack.getTypeId(), amount, itemStack.getDurability());
		}
		return ok;
	}

	//-------------------------------------------------------------------------- storeRealItemStackList
	/**
	 * Store ItemStack items descriptions into inventory
	 * - positive quantities additions, negative quantities removal
	 * - if reverse is set to true, negative quantities addition, positive quantities removal
	 * - if any add / remove error, then cancel the whole operation and return false
	 * - return true if everything is OK
	 */
	public boolean storeRealItemStackList(
		ArrayList<? extends RealItemStack> itemStackList, boolean reverse, boolean restoreIfFail
	) {
		boolean ok = true;
		ArrayList<ItemStack[]> itemStackBackup = backup();
		Iterator<? extends RealItemStack> iterator = itemStackList.iterator();
		while (iterator.hasNext()) {
			RealItemStack itemStack = iterator.next();
			int amount = (reverse ? -itemStack.getAmount() : itemStack.getAmount());
			if (amount < 0) {
				ok = ok && remove(itemStack.getTypeId(), -amount, itemStack.getDurability());
			} else {
				ok = ok && add(itemStack.getTypeId(), amount, itemStack.getDurability());
			}
		}
		if (!ok) {
			if (restoreIfFail)  {
				restore(itemStackBackup);
				errorLog = new ArrayList<RealItemStack>();
			}
			return false;
		}
		return true;
	}

	//-------------------------------------------------------------------------------------- toString
	/**
	 * This translate the inventories content into string
	 * Use it for debugging
	 */
	@Override
	public String toString()
	{
		String string = "##### RealInventory object :\n";
		for (int i = 0; i < inventories.length; i++) {
			string += "- INVENTORY i\n";
			ItemStack[] itemStackList = inventories[i].getContents();
			for (int j = 0; j < itemStackList.length; j++) {
				ItemStack itemStack = itemStackList[j];
				string += "- " + j
				+ ": typeId=" + itemStack.getTypeId()
				+ ", amount=" + itemStack.getAmount()
				+ ", durability=" + itemStack.getDurability()
				+ "\n";
			}
		}
		string += "##### RealInventory object end";
		return string;
	}

	//---------------------------------------------------------------------------------------- update
	/**
	 * Update chests states (if this is a chest inventory)
	 * Called after each modification into the inventory
	 */
	private void update()
	{
		if (chests[0] != null) {
			chests[0].update();
		}
		if (chests[1] != null) {
			chests[1].update();
		}
	}

}
