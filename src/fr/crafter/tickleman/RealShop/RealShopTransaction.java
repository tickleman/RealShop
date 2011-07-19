package fr.crafter.tickleman.RealShop;

import java.util.ArrayList;
import java.util.Iterator;

import fr.crafter.tickleman.RealPlugin.RealItemStack;
import fr.crafter.tickleman.RealPlugin.RealItemStackHashMap;

//############################################################################# RealShopTransaction
public class RealShopTransaction
{

	private boolean cancelAll = false;

	public ArrayList<RealShopTransactionLine> cancelledLines = null;

	private RealItemStackHashMap itemStackHashMap;

	private RealPricesFile marketFile;

	private String playerName;

	private RealShopPlugin plugin;

	private RealPricesFile pricesFile;

	private String shopPlayerName;

	private double totalPrice = (double)0;

	public ArrayList<RealShopTransactionLine> transactionLines = null;

	//--------------------------------------------------------------------------- RealShopTransaction
	private RealShopTransaction(
		RealShopPlugin plugin,
		String playerName,
		String shopPlayerName,
		RealItemStackHashMap itemStackHashMap,
		RealPricesFile pricesFile,
		RealPricesFile marketFile
	) {
		this.plugin = plugin;
		this.playerName = playerName;
		this.shopPlayerName = shopPlayerName;
		this.itemStackHashMap = itemStackHashMap;
		this.pricesFile = pricesFile;
		this.marketFile = marketFile;
	}

	//---------------------------------------------------------------------------------------- create
	public static RealShopTransaction create(
		RealShopPlugin plugin,
		String playerName,
		String shopPlayerName,
		RealItemStackHashMap itemStackHashMap,
		RealPricesFile pricesFile,
		RealPricesFile marketFile
	) {
		return new RealShopTransaction(
			plugin, playerName, shopPlayerName, itemStackHashMap, pricesFile, marketFile
		);
	}

	//--------------------------------------------------------------------------------- getTotalPrice
	public double getTotalPrice()
	{
		return totalPrice;
	}

	//----------------------------------------------------------------------------------- isCancelled
	public boolean isCancelled()
	{
		return cancelAll;
	}

	//----------------------------------------------------------------------------------- prepareBill
	public RealShopTransaction prepareBill(RealShop shop)
	{
		// Initialization
		cancelAll = false;
		cancelledLines = new ArrayList<RealShopTransactionLine>();
		transactionLines = new ArrayList<RealShopTransactionLine>();
		double totalPrice = (double)0;
		// create lines and cancelled lines
		Iterator<RealItemStack> iterator = itemStackHashMap.getContents().iterator();
		while (iterator.hasNext()) {
			RealItemStack itemStack = iterator.next();
			int amount = itemStack.getAmount();
			String typeIdDurability = itemStack.getTypeIdDurability();
			RealPrice price = pricesFile.getPrice(typeIdDurability, marketFile);
			if (price == null) {
				price = marketFile.getPrice(typeIdDurability, null);
			}
			RealShopTransactionLine transactionLine = new RealShopTransactionLine(itemStack, price);
			if (
				(price == null)
				|| ((amount > 0) && !shop.isItemBuyAllowed(typeIdDurability))
				|| ((amount < 0) && !shop.isItemSellAllowed(typeIdDurability))
				|| (!shop.getFlag("damagedItems", plugin.config.shopDamagedItems.equals("true")) && (itemStack.getDamage() != 0))
				|| (shop.getFlag("marketItemsOnly", plugin.config.shopMarketItemsOnly.equals("true")) && !plugin.marketFile.prices.containsKey(typeIdDurability))
			) {
				if (price == null) {
					transactionLine.comment = "no price";
				} else if ((amount > 0) && !shop.isItemBuyAllowed(typeIdDurability)) {
					transactionLine.comment = "buy not allowed";
				} else if ((amount < 0) && !shop.isItemSellAllowed(typeIdDurability)) {
					transactionLine.comment = "sell not allowed";
				} else if (!shop.getFlag("damagedItems", plugin.config.shopDamagedItems.equals("true")) && (itemStack.getDamage() != 0)) {
					transactionLine.comment = "damaged item";
				} else if (shop.getFlag("marketItemsOnly", plugin.config.shopMarketItemsOnly.equals("true")) && !plugin.marketFile.prices.containsKey(typeIdDurability)) {
					transactionLine.comment = "not in market";
				}
				cancelledLines.add(transactionLine);
			} else {
				transactionLines.add(transactionLine);
				totalPrice += transactionLine.getLinePrice();
			}
		}
		// if total amount exceeds available player amount, then cancel all
		this.totalPrice = Math.ceil(totalPrice * 100.0) / 100.0;
		if (
			this.totalPrice > plugin.realEconomy.getBalance(playerName)
			|| (-this.totalPrice) > plugin.realEconomy.getBalance(shopPlayerName)
		) {
			cancelAll = true;
		}
		return this;
	}

	//-------------------------------------------------------------------------------------- toString
	public String toString()
	{
		String result = "";
		{
			// header
			result += "<" + playerName + "> transaction details :\n";
		}
		String prefix = cancelAll ? "-CANCEL- " : "-VALID- ";
		{
			// cancelled lines
			Iterator<RealShopTransactionLine> iterator = cancelledLines.iterator();
			while (iterator.hasNext()) {
				RealItemStack itemStack = iterator.next();  
				result += "-CANCEL- " + plugin.dataValuesFile.getName(itemStack.getTypeIdDurability())
					+ " x" + itemStack.getAmount() + " :"
					+ " cancelled line"
					+ "\n";
			}
		}
		{
			// transaction lines
			Iterator<RealShopTransactionLine> iterator = transactionLines.iterator();
			while (iterator.hasNext()) {
				RealShopTransactionLine transactionLine = iterator.next();
				String strGain, strSide;
				if (transactionLine.getAmount() < 0) {
					strSide = "sale";
					strGain = "profit";
				} else {
					strSide = "purchase";
					strGain = "expense";
				}
				result += prefix + plugin.dataValuesFile.getName(transactionLine.getTypeIdDurability()) + ": "
					+ strSide
					+ " x" + Math.abs(transactionLine.getAmount())
					+ " price " + plugin.realEconomy.format(transactionLine.getUnitPrice())
					+ " " + strGain + " "
					+ plugin.realEconomy.format(Math.abs(transactionLine.getLinePrice()))
					+ "\n";
			}
		}
		{
			// footer
			result += prefix + "total price = " + totalPrice + "\n";
			if (cancelAll) {
				result += "CANCELALL !\n";
			}
		}
		return result;
	}

}
