package fr.crafter.tickleman.RealShop;

import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;

public class RealShopInventoryListener extends InventoryListener
{

	private final RealShopPlugin plugin;

	//--------------------------------------------------------------------- RealShopInventoryListener
	public RealShopInventoryListener(RealShopPlugin plugin)
	{
		super();
		this.plugin = plugin;
	}

	//------------------------------------------------------------------------------ onInventoryClose
	@Override
  public void onInventoryClose(InventoryCloseEvent event)
  {
		if ((event.getPlayer() != null) && (plugin.playersInChestCounter > 0)) {
			plugin.exitChest(event.getPlayer(), true);
		}
  }

}
