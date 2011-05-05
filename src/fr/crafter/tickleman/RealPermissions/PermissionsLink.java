package fr.crafter.tickleman.RealPermissions;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import fr.crafter.tickleman.RealPlugin.RealPlugin;

//########################################################################### class PermissionsLink
public class PermissionsLink
{

	public static boolean initialized = false;

	private static RealPlugin plugin;

	public static PermissionHandler permissionsHandler;

	//--------------------------------------------------------------------------------- hasPermission
	public static boolean hasPermission(Player player, String permission)
	{
		return permissionsHandler.has(player, plugin.name.toLowerCase() + "." + permission);
	}

	//------------------------------------------------------------------------------------------ init
	public static boolean init(RealPlugin plugin)
	{
		PermissionsLink.plugin = plugin;
		Plugin test = plugin.getServer().getPluginManager().getPlugin("Permissions");
		boolean ok = (test != null);
		if (ok) {
			permissionsHandler = ((Permissions)test).getHandler();
			plugin.log.info("load dependency : Permissions ok", true);
			initialized = true;
		} else {
			plugin.log.severe("load dependency : Permissions could not be linked", true);
		}
		return ok;
	}

}
