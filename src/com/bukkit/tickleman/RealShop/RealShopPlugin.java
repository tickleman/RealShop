package com.bukkit.tickleman.RealShop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

import com.bukkit.tickleman.RealPlugin.RealChest;
import com.bukkit.tickleman.RealPlugin.RealDataValuesFile;
import com.bukkit.tickleman.RealPlugin.RealEconomy;
import com.bukkit.tickleman.RealPlugin.RealInventory;
import com.bukkit.tickleman.RealPlugin.RealItemStack;
import com.bukkit.tickleman.RealPlugin.RealItemStackHashMap;
import com.bukkit.tickleman.RealPlugin.RealPlugin;

public class RealShopPlugin extends RealPlugin
{

	public RealShopConfig config;

	public final HashMap<String, String> shopCommand = new HashMap<String, String>();
	public final HashMap<String, RealInChestState> inChestStates = new HashMap<String, RealInChestState>();
	public RealShopDailyLog dailyLog = null;
	public int playersInChestCounter = 0;
	public RealDataValuesFile dataValuesFile;
	public RealPricesFile marketFile;
	public RealShopsFile shopsFile;
	public HashMap<String, Long> lastDayTime = new HashMap<String, Long>();

	private final RealShopBlockListener blockListener = new RealShopBlockListener(this);
	private final RealShopPlayerListener playerListener = new RealShopPlayerListener(this);

	//-------------------------------------------------------------------------------- RealShopPlugin
	public RealShopPlugin()
	{
		super("Tickleman", "RealShop", "0.15");
	}

	//----------------------------------------------------------------------------------- onDisable
	@Override
	public void onDisable()
	{
		// linked objects reset (dailyLog is kept)
		config = null;
		dataValuesFile = null;
		marketFile = null;
		shopsFile = null;
		super.onDisable();
	}

	//------------------------------------------------------------------------------------ onEnable
	@Override
	public void onEnable()
	{
		// events listeners 
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		// read configuration file
		config = new RealShopConfig(this);
		config.load();
		// setup dailyLog (kept when disabled)
		if (dailyLog == null) {
			dailyLog = new RealShopDailyLog(this);
		}
		// read data values file
		dataValuesFile = new RealDataValuesFile(this, "dataValues");
		dataValuesFile.load();
		// read market file
		marketFile = new RealPricesFile(this, "market");
		marketFile.load();
		// read shops file
		shopsFile = new RealShopsFile(this);
		shopsFile.load();
		// enable
		super.onEnable();
		// check mandatory dependencies
		if (!RealEconomy.init(this)) {
			log.severe("needs iConomy plugin");
			pm.disablePlugin(this);
		}
	}

	//---------------------------------------------------------------------------------- enterChest
	public void enterChest(Player player, Block block)
	{
		// write in-chest state (inChest = true, player's coordinates, inventory backup)
		String playerName = player.getName();
		RealInChestState inChestState = inChestStates.get(playerName);
		if (inChestState == null) {
			inChestState = new RealInChestState();
			inChestStates.put(playerName, inChestState);
			log.info(
				"Player " + playerName + " enters in shop chest "
				+ block.getWorld() + "," + block.getX() + "," + block.getY() + "," + block.getZ() 
			);
		}
		inChestState.inChest = true;
		inChestState.chest = RealChest.create(block);
		inChestState.lastX = Math.round(player.getLocation().getX());
		inChestState.lastZ = Math.round(player.getLocation().getZ());
		inChestState.itemStackHashMap = RealItemStackHashMap.create().storeInventory(
			player.getInventory(), true
		);
		// shop information
		player.sendMessage(
			lang.tr("Welcome into this shop") + ". " + lang.tr("You've got") + " "
			+ RealEconomy.getBalance(player.getName()) + " " + RealEconomy.getCurrency()
			+ " " + lang.tr("into your pocket")
		);
		playersInChestCounter = inChestStates.size();
		log.info("Players in chest counter = " + playersInChestCounter);
	}

	//----------------------------------------------------------------------------------- exitChest
	public void exitChest(Player player)
	{
		String playerName = player.getName();
		RealInChestState inChestState = inChestStates.get(playerName);
		if (inChestState != null) {
			log.info("Player " + playerName + " exits from shop chest ");
			if (inChestState.inChest) {
				inChestState.inChest = false;
				// reload prices
				marketFile.load();
				// remove new player's inventory items from old player's inventory
				// in order to know how many of each has been buy (positive) / sold (negative)
				inChestState.itemStackHashMap.storeInventory(player.getInventory(), false);
				// prepare bill
				RealShopTransaction transaction = RealShopTransaction.create(
					this, playerName, inChestState.itemStackHashMap, marketFile
				).prepareBill();
				log.info(transaction.toString());
				if (transaction.isCanceled()) {
					// transaction is fully canceled : items go back in their original inventories
					ArrayList<RealItemStack> itemStackList = inChestState.itemStackHashMap.getContents();
					RealInventory
						.create(inChestState.chest)
						.storeRealItemStackList(itemStackList, false);
					RealInventory
						.create(player)
						.storeRealItemStackList(itemStackList, true);
					player.sendMessage(lang.tr("Cancelled transaction"));
				} else {
					// some lines canceled : corresponding items go back to their original inventories
					if (!transaction.canceledLines.isEmpty()) {
						RealInventory
							.create(inChestState.chest)
							.storeRealItemStackList(transaction.canceledLines, false);
						RealInventory
							.create(player)
							.storeRealItemStackList(transaction.canceledLines, true);
						// display canceled lines information
						Iterator<RealItemStack> iterator = transaction.canceledLines.iterator();
						while (iterator.hasNext()) {
							player.sendMessage(
								dataValuesFile.getName(iterator.next().getTypeId())
								+ " : " + lang.tr("cancelled line")
							);
						}
					}
					// update player's account
					RealEconomy.setBalance(
						playerName, RealEconomy.getBalance(playerName) - transaction.getTotalPrice() 
					);
					// store transaction lines into daily log
					dailyLog.addTransaction(transaction);
					// display transaction lines information
					Iterator<RealShopTransactionLine> iterator = transaction.transactionLines.iterator();
					while (iterator.hasNext()) {
						RealShopTransactionLine transactionLine = iterator.next();
						String strGain, strSide;
						if (transactionLine.getAmount() < 0) {
							strSide = lang.tr("sale");
							strGain = lang.tr("profit");
						} else {
							strSide = lang.tr("purchase");
							strGain = lang.tr("expense");
						}
						player.sendMessage(
							"- " + dataValuesFile.getName(transactionLine.getTypeId()) + ": "
							+ strSide
							+ " x" + Math.abs(transactionLine.getAmount())
							+ " " + lang.tr("price")
							+ " " + transactionLine.getUnitPrice() + RealEconomy.getCurrency()
							+ " " + strGain + " "
							+ Math.abs(transactionLine.getLinePrice()) + RealEconomy.getCurrency()
						);
					}
					// display transaction total
					String strSide = transaction.getTotalPrice() < 0 ? lang.tr("earned") :lang.tr("spent");
					player.sendMessage(
						lang.tr("Transaction total") + " : " + lang.tr("you have") + " " + strSide + " "
						+ Math.abs(transaction.getTotalPrice()) + RealEconomy.getCurrency()
					);
				}
			}
			inChestStates.remove(playerName);
			playersInChestCounter = inChestStates.size();
			log.info("Players in chest counter = " + playersInChestCounter);
		}
	}

	//------------------------------------------------------------------------- registerBlockAsShop
	public void registerBlockAsShop(Player player, Block block)
	{
		registerBlockAsShop(player, block, 0);
	}

	//------------------------------------------------------------------------- registerBlockAsShop
	private void registerBlockAsShop(Player player, Block block, int mode)
	{
		String message = null;
		// neighbor block will be registered too
		Block neighborBlock;
		if (mode == 0) {
			neighborBlock = RealChest.scanForNeighborChest(
					block.getWorld(), block.getX(), block.getY(), block.getZ()
			);
		} else {
			neighborBlock = null;
		}
		// get playerName and shop key (location)
		String playerName = player.getName();
		String key = block.getWorld().getName() + ";"
			+ block.getX() + ";" + block.getY() + ";" + block.getZ();
		// check if shop already exists
		RealShop shop = shopsFile.shops.get(key);
		if ((mode == -1) || ((mode == 0) && (shop != null))) {
			// if shop already exists or force removal : check if player has same name or op
			if ((mode == -1) || (shop.player == playerName) || player.isOp()) {
				// remove shop
				shopsFile.shops.remove(key);
				if (neighborBlock != null) {
					registerBlockAsShop(player, neighborBlock, -1);
				}
				shopsFile.save();
				message = lang.tr("This chest is not a shop anymore");
			} else {
				message = lang.tr("This chest belongs to") + " " + shop.player;
			}
		} else {
			// if shop did not exist or force creation, then add shop
			shopsFile.shops.put(key, new RealShop(
				block.getWorld().getName(), block.getX(), block.getY(), block.getZ(),
				playerName
			));
			if (neighborBlock != null) {
				registerBlockAsShop(player, neighborBlock, 1);
			}
			shopsFile.save();
			message = lang.tr("This chest is now a shop");
		}
		if ((mode == 0) && (message != null)) {
			player.sendMessage(message);
		}
	}

	//-------------------------------------------------------------------------------- pluginsInfos
	/**
	 * Displays informations about RealShop
	 * Operators will get it using "/shop check" command
	 */
	public void pluginInfos(Player player)
	{
		Iterator<String> iterator = inChestStates.keySet().iterator();
		String players = "";
		while (iterator.hasNext()) {
			if (players == "") {
				players = iterator.next();
			} else {
				players += ", " + iterator.next();
			}
		}
		player.sendMessage(playersInChestCounter + " players in chest counter");
		player.sendMessage("inChestStates for " + players);
		player.sendMessage(shopsFile.shops.size() + " opened shops");
		player.sendMessage(marketFile.prices.size() + " market prices");
		log.info(playersInChestCounter + " players in chest counter");
		log.info("inChestStates for " + players);
		log.info(shopsFile.shops.size() + " opened shops");
		log.info(marketFile.prices.size() + " market prices");
	}

	//--------------------------------------------------------------------------- pluginInfosPrices
	/**
	 * Displays the whole market prices from RealShop
	 * (includes calculated prices)
	 * Operators will get it using "/shop prices" command
	 */
	public void pluginInfosPrices(Player player)
	{
		log.info("Market prices list :");
		int[] ids = dataValuesFile.getIds();
		for (int i = 0; i < ids.length; i++) {
			RealPrice price = marketFile.getPrice(ids[i]);
			if (price != null) {
				log.info(
						"SHOP PRICES : " + ids[i] + " (" + dataValuesFile.getName(ids[i]) + ") :"
						+ " buy " + price.getBuy() + " sell " + price.getSell()
				);
			}
		}
	}

	//------------------------------------------------------------------------- pluginInfosDailyLog
	/**
	 * Displays current daily moves log status
	 * (includes calculated prices)
	 * Operators will get it using "/shop prices" command
	 */
	public void pluginInfosDailyLog(Player player)
	{
		log.info("Daily log status is : " + dailyLog.toString());
	}

}
