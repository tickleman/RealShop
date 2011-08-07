package fr.crafter.tickleman.RealShop;

import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import fr.crafter.tickleman.RealEconomy.BOSEconomyLink;
import fr.crafter.tickleman.RealEconomy.iConomyLink;
import fr.crafter.tickleman.RealPermissions.PermissionsLink;

import org.bukkit.plugin.Plugin;

//########################################################################## RealShopPluginListener
public class RealShopPluginListener extends ServerListener
{

	RealShopPlugin plugin;

	//------------------------------------------------------------------------ RealShopPluginListener
	public RealShopPluginListener(RealShopPlugin plugin)
	{
		this.plugin = plugin;
	}

	// ------------------------------------------------------------------------------ OnPluginDisable
	@Override
	public void onPluginDisable(PluginDisableEvent event)
	{
		if (
			(event.getPlugin().getDescription().getName() == "Spout")
			|| (event.getPlugin().getDescription().getName() == "RealShop")
		) {
			plugin.inventoryListener = null;
		}
	}

	//-------------------------------------------------------------------------------- onPluginEnable
	@Override
	public void onPluginEnable(PluginEnableEvent event)
	{
		// iConomy
		if (plugin.config.economyPlugin.equals("iConomy") && !iConomyLink.initialized) {
			Plugin iConomy = plugin.getServer().getPluginManager().getPlugin("iConomy");
			if (iConomy != null) {
				if (iConomy.isEnabled()) {
					if (iConomyLink.init(plugin)) {
						plugin.log.info("Uses iConomy plugin (/money commands) as economy system", true);
						plugin.realEconomy.economyPlugin = "iConomy";
					} else {
						plugin.log.severe("Uses RealEconomy instead of iConomy !");
					}
				}
			}
		}
		// BOSEconomy
		if (plugin.config.economyPlugin.equals("BOSEconomy") && !BOSEconomyLink.initialized) {
			Plugin bosEconomy = plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
			if (bosEconomy != null) {
				if (bosEconomy.isEnabled()) {
					if (BOSEconomyLink.init(plugin)) {
						plugin.log.info("Uses BOSEconomy plugin (/econ commands) as economy system", true);
						plugin.realEconomy.economyPlugin = "BOSEconomy";
					} else {
						plugin.log.severe("Uses RealEconomy instead of BOSEconomy !");
					}
				}
			}
		}
		// Permissions
		if (plugin.config.permissionsPlugin.equals("Permissions") && !PermissionsLink.initialized) {
			Plugin permissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
			if (permissions != null) {
				if (permissions.isEnabled()) {
					if (PermissionsLink.init(plugin)) {
						plugin.log.info("Uses Permissions plugin as permissions plugin", true);
						plugin.realPermissions.permissionsPlugin = "Permissions";
					} else {
						plugin.log.severe("Use built-in op/user permissions instead of Permissions !");
					}
				}
			}
		}
		// Spout
		if (
			(plugin.inventoryListener == null)
			&& (plugin.getServer().getPluginManager().getPlugin("Spout") != null)
			&& plugin.getServer().getPluginManager().getPlugin("Spout").isEnabled()
		) {
			plugin.inventoryListener = new RealShopInventoryListener(plugin);
			plugin.getServer().getPluginManager().registerEvent(
				Event.Type.CUSTOM_EVENT, plugin.inventoryListener, Event.Priority.Normal, plugin
			);
			plugin.log.info("Uses Spout to close shops immediately when living the chest", true);
		}
	}

}
