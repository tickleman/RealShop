package fr.crafter.tickleman.RealPermissions;

import org.bukkit.entity.Player;

import fr.crafter.tickleman.RealPlugin.RealPlugin;

//################################################################################# RealPermissions
public class RealPermissions
{

	public String permissionsPlugin;

	private RealPlugin plugin;

	//----------------------------------------------------------------------------------- RealEconomy
	public RealPermissions(RealPlugin plugin)
	{
		this.plugin = plugin;
		this.permissionsPlugin = "none";
	}

	//--------------------------------------------------------------------------------- hasPermission
	public boolean hasPermission(Player player, String permission)
	{
		if (permissionsPlugin.equals("Permissions")) {
			return PermissionsLink.hasPermission(player, permission);
		}
		return plugin.hasPermission(player, permission);
	}

}
