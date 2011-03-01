package fr.crafter.tickleman.RealShop;

import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;

import com.nijiko.coelho.iConomy.iConomy;

//########################################################################## RealShopServerListener
public class RealShopServerListener extends ServerListener
{

	private RealShopPlugin plugin;

	// ------------------------------------------------------------------------
	// RealShopServerListener
	public RealShopServerListener(RealShopPlugin plugin)
	{
		this.plugin = plugin;
	}

	// -------------------------------------------------------------------------------
	// onPluginEnabled
	@Override
	public void onPluginEnabled(PluginEvent event)
	{
		if (
			(plugin.config.economyPlugin == "iConomy")
			&& event.getPlugin().getDescription().getName().equals("iConomy")
		) {
			plugin.iConomy = (iConomy)event.getPlugin();
		}
	}

}
