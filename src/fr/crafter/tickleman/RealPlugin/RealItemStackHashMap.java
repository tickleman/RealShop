package fr.crafter.tickleman.RealPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

//############################################################################ RealItemStackHashMap
public class RealItemStackHashMap
{

	/**
	 * HashMap describing an ItemStack collection
	 * This links typeId + durability search index to a stored quantity
	 */
	public HashMap<Integer, HashMap<Short, Integer>> content;

	//------------------------------------------------------------------------ RealItemStackHashMap
	private RealItemStackHashMap()
	{
		content = new HashMap<Integer, HashMap<Short, Integer>>();
	}

	//---------------------------------------------------------------------------------------- create
	public static RealItemStackHashMap create()
	{
		return new RealItemStackHashMap();
	}

	//----------------------------------------------------------------------------------- getContents
	public ArrayList<RealItemStack> getContents()
	{
		ArrayList<RealItemStack> result = new ArrayList<RealItemStack>();
		Iterator<Integer> typeIdsIterator = content.keySet().iterator();
		while (typeIdsIterator.hasNext()) {
			int typeId = typeIdsIterator.next();
			HashMap<Short, Integer> typeIdContent = content.get(typeId);
			Iterator<Short> durabilitiesIterator = typeIdContent.keySet().iterator();
			while (durabilitiesIterator.hasNext()) {
				short durability = durabilitiesIterator.next();
				int amount = typeIdContent.get(durability);
				result.add(new RealItemStack(typeId, amount, durability));
			}
		}
		return result;
	}

	//-------------------------------------------------------------------------------- storeInventory
	public RealItemStackHashMap storeInventory(Inventory inventory, boolean removal)
	{
		ItemStack[] inventoryItems = inventory.getContents();
		for (int i = 0; i < inventoryItems.length; i++) {
			storeItem(inventoryItems[i], removal);
		}
		return this;
	}

	//-------------------------------------------------------------------------------- storeInventory
	public RealItemStackHashMap storeInventory(RealInventory realInventory, boolean removal)
	{
		for (int i = 0; i < realInventory.inventories.length; i++) {
			storeInventory(realInventory.inventories[i], removal);
		}
		return this;
	}

	//----------------------------------------------------------------------------------- storeItem
	private void storeItem(ItemStack item, boolean removal)
	{
		if (item != null) {
			Integer itemAmount = item.getAmount();
			if (itemAmount != 0) {
				Integer typeId = new Integer(item.getTypeId());
				HashMap<Short, Integer> typeIdContent = content.get(typeId);
				if (typeIdContent == null) {
					content.put(typeId, typeIdContent = new HashMap<Short, Integer>());
				}
				Short durability = item.getDurability();
				Integer amount = typeIdContent.get(durability);
				if (amount == null) {
					amount = 0;
				}
				amount = (removal ? (amount - itemAmount) : (amount + itemAmount));
				if (amount != 0) {
					typeIdContent.put(durability, amount);
				} else {
					typeIdContent.remove(durability);
					if (typeIdContent.isEmpty()) {
						content.remove(typeId);
					}
				}
			}
		}
	}

}
