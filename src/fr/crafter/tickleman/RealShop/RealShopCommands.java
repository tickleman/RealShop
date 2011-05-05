package fr.crafter.tickleman.RealShop;

import org.bukkit.entity.Player;

import fr.crafter.tickleman.RealPlugin.RealColor;

//################################################################################ RealShopCommands
public class RealShopCommands
{
	
	RealShopPlugin plugin;

	//------------------------------------------------------------------------------ RealShopCommands
	public RealShopCommands(RealShopPlugin plugin)
	{
		this.plugin = plugin;
	}

	//-------------------------------------------------------------------------------- marketPriceDel
	public void marketPriceDel(Player player, String typeIdDamage)
	{
		plugin.marketFile.prices.remove(typeIdDamage);
		plugin.marketFile.save();
		player.sendMessage(
			RealColor.message
			+ plugin.lang.tr("Market price deleted for +item")
			.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.message)
		);
	}

	//---------------------------------------------------------------------------- marketPriceDisplay
	public void marketPriceDisplay(Player player, String typeIdDamage)
	{
		RealPrice price = plugin.marketFile.prices.get(typeIdDamage);
		if (price == null) {
			player.sendMessage(
				RealColor.cancel
				+ plugin.lang.tr("No market price for +item")
				.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.cancel)
			);
			price = plugin.marketFile.getPrice(typeIdDamage, null);
			if (price == null) {
				player.sendMessage(
					RealColor.cancel
					+ plugin.lang.tr("Price can't be calculated from recipes for +item")
					.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.cancel)
				);
			} else {
				player.sendMessage(
					RealColor.message
					+ plugin.lang.tr("Calculated price (from recipes) for +item : buy +buy, sell +sell")
					.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.message)
					.replace("+buy", RealColor.price + price.buy + RealColor.message)
					.replace("+sell", RealColor.price + price.sell + RealColor.message)
				);
			}
		} else {
			player.sendMessage(
				RealColor.message
				+ plugin.lang.tr("Market price for +item : buy +buy, sell +sell")
				.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.message)
				.replace("+buy", RealColor.price + price.buy + RealColor.message)
				.replace("+sell", RealColor.price + price.sell + RealColor.message)
			);
		}
	}

	//-------------------------------------------------------------------------------- marketPriceSet
	public void marketPriceSet(Player player, String typeIdDamage, String buyPrice, String sellPrice)
	{
		try {
			RealPrice price = new RealPrice(
				Double.parseDouble(buyPrice), Double.parseDouble(sellPrice.equals("") ? buyPrice : sellPrice)
			);
			plugin.marketFile.prices.put(typeIdDamage, price);
			plugin.marketFile.save();
			player.sendMessage(
				RealColor.message
				+ plugin.lang.tr("Market price for +item : buy +buy, sell +sell")
				.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.message)
				.replace("+buy", RealColor.price + price.buy + RealColor.message)
				.replace("+sell", RealColor.price + price.sell + RealColor.message)
			);
		} catch (Exception e) {
			player.sendMessage(
				RealColor.cancel
				+ plugin.lang.tr("Error while setting market price for +item")
				.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.cancel)
			);
			player.sendMessage(
				RealColor.message
				+ plugin.lang.tr("Usage: +command")
				.replace("+command", RealColor.command + plugin.lang.tr("/rshop market <itemId>[:<itemDamage>] <sellPrice> <buyPrice>") + RealColor.message)
			);
		}
	}

	//-------------------------------------------------------------------------------- playerPriceDel
	public void playerPriceDel(Player player, String typeIdDamage)
	{
		RealPricesFile pricesFile = RealPricesFile.playerPricesFile(
				plugin, player.getName(), null
			);
			pricesFile.prices.remove(typeIdDamage);
			pricesFile.save();
			player.sendMessage(
				RealColor.message
				+ plugin.lang.tr("Player's price deleted for +item")
				.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.message)
			);
	}

	//---------------------------------------------------------------------------- playerPriceDisplay
	public void playerPriceDisplay(Player player, String typeIdDamage)
	{
		RealPricesFile pricesFile = RealPricesFile.playerPricesFile(
				plugin, player.getName(), null
			);
			RealPrice price = pricesFile.prices.get(typeIdDamage);
			if (price == null) {
				player.sendMessage(
					RealColor.cancel
					+ plugin.lang.tr("No player's price for +item")
					.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.cancel)
				);
				price = pricesFile.getPrice(typeIdDamage, plugin.marketFile);
				if (price == null) {
					player.sendMessage(
						RealColor.cancel
						+ plugin.lang.tr("Price can't be calculated from recipes for +item")
						.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.cancel)
					);
				} else {
					player.sendMessage(
						RealColor.message
						+ plugin.lang.tr("Calculated price (from market/recipes) for +item : buy +buy, sell +sell")
						.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.message)
						.replace("+buy", RealColor.price + price.buy + RealColor.message)
						.replace("+sell", RealColor.price + price.sell + RealColor.message)
					);
				}
			} else {
				player.sendMessage(
					RealColor.message
					+ plugin.lang.tr("Player's price for +item : buy +buy, sell +sell")
					.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.message)
					.replace("+buy", RealColor.price + price.buy + RealColor.message)
					.replace("+sell", RealColor.price + price.sell + RealColor.message)
				);
			}
	}

	//-------------------------------------------------------------------------------- playerPriceSet
	public void playerPriceSet(Player player, String typeIdDamage, String buyPrice, String sellPrice)
	{
		RealPricesFile pricesFile = RealPricesFile.playerPricesFile(
			plugin, player.getName(), null
		);
		try {
			RealPrice price = new RealPrice(
				Double.parseDouble(buyPrice), Double.parseDouble(sellPrice.equals("") ? buyPrice : sellPrice)
			);
			pricesFile.prices.put(typeIdDamage, price);
			pricesFile.save();
			player.sendMessage(
				RealColor.message
				+ plugin.lang.tr("Player's price for +item : buy +buy, sell +sell")
				.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.message)
				.replace("+buy", RealColor.price + price.buy + RealColor.message)
				.replace("+sell", RealColor.price + price.sell + RealColor.message)
			);
		} catch (Exception e) {
			player.sendMessage(
				RealColor.cancel
				+ plugin.lang.tr("Error while setting player's price for +item")
				.replace("+item", RealColor.item + plugin.dataValuesFile.getName(typeIdDamage) + RealColor.cancel)
			);
			player.sendMessage(
				RealColor.message
				+ plugin.lang.tr("Usage: +command")
				.replace("+command", RealColor.command + plugin.lang.tr("/rshop price <itemId>[:<itemDamage>] <sellPrice> <buyPrice>") + RealColor.message)
			);
		}
	}

}
