package fr.crafter.tickleman.RealShop;

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

	//-------------------------------------------------------------------------------- onPluginEnable
	@Override
	public void onPluginEnable(PluginEnableEvent event)
	{
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
	}

}
