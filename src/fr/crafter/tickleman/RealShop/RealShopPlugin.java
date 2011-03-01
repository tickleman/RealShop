package fr.crafter.tickleman.RealShop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

import com.nijiko.coelho.iConomy.iConomy;

import fr.crafter.tickleman.RealEconomy.RealEconomy;
import fr.crafter.tickleman.RealEconomy.iConomyLink;
import fr.crafter.tickleman.RealPlugin.RealChest;
import fr.crafter.tickleman.RealPlugin.RealDataValuesFile;
import fr.crafter.tickleman.RealPlugin.RealInventory;
import fr.crafter.tickleman.RealPlugin.RealItemStack;
import fr.crafter.tickleman.RealPlugin.RealItemStackHashMap;
import fr.crafter.tickleman.RealPlugin.RealPlugin;
import fr.crafter.tickleman.RealPlugin.RealTime;

//################################################################################## RealShopPlugin
public class RealShopPlugin extends RealPlugin
{

	/** Global configuration */
	public RealShopConfig config;

	/** Shop command typed by the player (ie "tickleman1" => "/shop") */
	public final HashMap<String, String> shopCommand = new HashMap<String, String>();
	
	/** Says if the player is into a chest, and stores chest state info */
	public final HashMap<String, RealInChestState> inChestStates = new HashMap<String, RealInChestState>();

	/** Says if the player is into a chest, and stores chest state info */
	public final HashMap<String, Boolean> lockedChests = new HashMap<String, Boolean>(); 

	/** Daily log stores movements for each buy / sold item */
	public RealShopDailyLog dailyLog = null;

	/** Number of players that have opened a shop-chest */
	public int playersInChestCounter = 0;

	/** Data values files : complete list of Minecraft blocks and items */
	public RealDataValuesFile dataValuesFile;

	/** Market prices file (market.cfg) : global market price for each item */
	public RealPricesFile marketFile;

	/** Shops list and file link */
	public RealShopsFile shopsFile;

	/** Last day time, per world (ie : in reality this is the time of the NEXT day change) */
	public HashMap<String, Long> lastDayTime = new HashMap<String, Long>();

	/** Block events Listener */
	private final RealShopBlockListener blockListener = new RealShopBlockListener(this);

	/** Player events Listener */
	private final RealShopPlayerListener playerListener = new RealShopPlayerListener(this);

	/** Server events Listener */
	private final RealShopServerListener serverListener = new RealShopServerListener(this);

	/** RealEconomy */
	public final RealEconomy realEconomy = new RealEconomy(this);

	/** iConomy link for a further back-to-compatibility */
	public iConomy iConomy;

	//-------------------------------------------------------------------------------- RealShopPlugin
	public RealShopPlugin()
	{
		super("Tickleman", "RealShop", "0.32");
	}

	//------------------------------------------------------------------------------------- onDisable
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

	//-------------------------------------------------------------------------------------- onEnable
	@Override
	public void onEnable()
	{
		// events listeners 
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
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
		// Economy plugin link
		realEconomy.economyPlugin = "RealEconomy";
		if (config.economyPlugin.equals("iConomy")) {
			if (iConomyLink.init(this)) {
				realEconomy.economyPlugin = "iConomy";
			} else {
				log.severe("Uses RealEconomy instead of iConomy !");
			}
		}
		// enable
		super.onEnable();
	}

	//------------------------------------------------------------------------------------ enterChest
	public boolean enterChest(Player player, Block block)
	{
		// write in-chest state (inChest = true, player's coordinates, inventory backup)
		String playerName = player.getName();
		RealInChestState inChestState = inChestStates.get(playerName);
		if (inChestState == null) {
			inChestState = new RealInChestState();
			inChestStates.put(playerName, inChestState);
		}
		inChestState.enterTime = RealTime.worldToRealTime(player.getWorld());
		inChestState.inChest = true;
		inChestState.block = block;
		if (shopsFile.shopAt(inChestState.block).player.equals(playerName)) {
			player.sendMessage(lang.tr("Welcome into your shop"));
			inChestStates.remove(playerName);
			return true;
		} else {
			inChestState.chest = RealChest.create(block);
			String chestId = inChestState.chest.getChestId();
			if (lockedChests.get(chestId) != null) {
				player.sendMessage(lang.tr("This shop is already in use by another player"));
				inChestStates.remove(playerName);
				return false;
			} else {
				lockedChests.put(chestId, true);
				inChestState.lastX = Math.round(player.getLocation().getX());
				inChestState.lastZ = Math.round(player.getLocation().getZ());
				inChestState.itemStackHashMap = RealItemStackHashMap.create().storeInventory(
						RealInventory.create(inChestState.chest), false
				);
				// shop information
				player.sendMessage(
						lang.tr("Welcome into this shop") + ". " + lang.tr("You've got") + " "
						+ realEconomy.getBalance(player.getName()) + " " + realEconomy.getCurrency()
						+ " " + lang.tr("into your pocket")
				);
				playersInChestCounter = inChestStates.size();
				return true;
			}
		}
	}

	//------------------------------------------------------------------------------------- exitChest
	public void exitChest(Player player)
	{
		String playerName = player.getName();
		RealInChestState inChestState = inChestStates.get(playerName);
		if (inChestState != null) {
			if (inChestState.inChest) {
				inChestState.inChest = false;
				String shopPlayerName = shopsFile.shopAt(inChestState.block).player;
				Player shopPlayer = getServer().getPlayer(shopPlayerName);
				// reload prices
				marketFile.load();
				// remove new chest's inventory items from old chest's inventory
				// in order to know how many of each has been buy (positive) / sold (negative)
				inChestState.itemStackHashMap.storeInventory(
					RealInventory.create(inChestState.chest), true
				);
				// prepare bill
				RealShopTransaction transaction = RealShopTransaction.create(
					this, playerName, shopPlayerName, inChestState.itemStackHashMap, marketFile
				).prepareBill(shopsFile.shopAt(inChestState.block));
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
					if (realEconomy.setBalance(
						playerName, realEconomy.getBalance(playerName) - transaction.getTotalPrice() 
					)) {
						// update shop player's account
						if (!realEconomy.setBalance(
								shopPlayerName, realEconomy.getBalance(shopPlayerName) + transaction.getTotalPrice()
						)) {
							// rollback if any error
							realEconomy.setBalance(
								playerName, realEconomy.getBalance(playerName) + transaction.getTotalPrice() 
							);
						}
					}
					// store transaction lines into daily log
					dailyLog.addTransaction(transaction);
					// display transaction lines information
					Iterator<RealShopTransactionLine> iterator = transaction.transactionLines.iterator();
					while (iterator.hasNext()) {
						RealShopTransactionLine transactionLine = iterator.next();
						String strGain, strSide, shopStrGain;
						if (transactionLine.getAmount() < 0) {
							strSide = lang.tr("sale");
							strGain = lang.tr("profit");
							shopStrGain = lang.tr("expense");
						} else {
							strSide = lang.tr("purchase");
							strGain = lang.tr("expense");
							shopStrGain = lang.tr("purchase");
						}
						player.sendMessage(
							"- " + dataValuesFile.getName(transactionLine.getTypeId()) + ": "
							+ strSide
							+ " x" + Math.abs(transactionLine.getAmount())
							+ " " + lang.tr("price")
							+ " " + transactionLine.getUnitPrice() + realEconomy.getCurrency()
							+ " " + strGain + " "
							+ Math.abs(transactionLine.getLinePrice()) + realEconomy.getCurrency()
						);
						if (shopPlayer != null) {
							shopPlayer.sendMessage(
								"SHOP " + playerName
								+ " " + dataValuesFile.getName(transactionLine.getTypeId()) + ": "
								+ strSide
								+ " x" + Math.abs(transactionLine.getAmount())
								+ " " + lang.tr("price")
								+ " " + transactionLine.getUnitPrice() + realEconomy.getCurrency()
								+ " " + shopStrGain + " "
								+ Math.abs(transactionLine.getLinePrice()) + realEconomy.getCurrency()
							);
						}
					}
					// display transaction total
					String strSide = transaction.getTotalPrice() < 0 ? lang.tr("earned") :lang.tr("spent");
					player.sendMessage(
						lang.tr("Transaction total") + " : " + lang.tr("you have") + " " + strSide + " "
						+ Math.abs(transaction.getTotalPrice()) + realEconomy.getCurrency()
					);
					/*
					// It is not useful. Less lines !
					String shopStrSide = transaction.getTotalPrice() > 0 ? lang.tr("earned") :lang.tr("spent");
					if (shopPlayer != null) {
						shopPlayer.sendMessage(
							"SHOP " + playerName + " "
							+ lang.tr("Transaction total") + " : " + lang.tr("you have") + " " + shopStrSide + " "
							+ Math.abs(transaction.getTotalPrice()) + RealEconomy.getCurrency()
						);
					}
					*/
				}
			}
			lockedChests.remove(inChestState.chest.getChestId());
			inChestStates.remove(playerName);
			playersInChestCounter = inChestStates.size();
		}
	}

	//--------------------------------------------------------------------------- registerBlockAsShop
	public void registerBlockAsShop(Player player, Block block)
	{
		registerBlockAsShop(player, block, 0);
	}

	//--------------------------------------------------------------------------- registerBlockAsShop
	/**
	 * mode  0 to auto register/unregister
	 * mode  1 to force register of a neighbor chest
	 * mode -1 to force unregister of a neighbor chest
	 */
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
			if ((mode == -1) || (shop.player.equals(playerName)) || player.isOp()) {
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

	//---------------------------------------------------------------------------------- pluginsInfos
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

	//----------------------------------------------------------------------------- pluginInfosPrices
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

	//--------------------------------------------------------------------------- pluginInfosDailyLog
	/**
	 * Displays current daily moves log status
	 * (includes calculated prices)
	 * Operators will get it using "/shop prices" command
	 */
	public void pluginInfosDailyLog(Player player)
	{
		log.info("Daily log status is : " + dailyLog.toString());
	}

	//------------------------------------------------------------------------------------ shopAddBuy
	public void shopAddBuy(Player player, Block block, String command)
	{
		RealShop shop = shopsFile.shopAt(block);
		if (player.getName().equals(shop.player)) {
			shopAddExclBuySell(player, shop.buyOnly, command, "buy");
		} else {
			player.sendMessage(lang.tr("This chest belongs to") + " " + shop.player);
		}
	}

	//---------------------------------------------------------------------------- shopAddExclBuySell
	private void shopAddExclBuySell(
		Player player, HashMap<Integer, Boolean> addTo, String command, String what
	) {
		command += "+";
		int index = command.lastIndexOf(' ') + 1;
		boolean plus = true;
		String strTypeId = "";
		while (index < command.length()) {
			char c = command.charAt(index);
			if ((c == '+') || (c == '-')) {
				if (!strTypeId.equals("")) {
					try {
						int typeId = Integer.parseInt(strTypeId);
						if (plus) {
							addTo.put(typeId, true);
						} else {
							addTo.remove(typeId);
						}
					} catch (Exception e) {
					}
				}
				strTypeId = "";
				if (c == '+') {
					plus = true;
				} else if (c == '-') {
					plus = false;
				}
			} else if ((c >= '0') && (c <= '9')) {
				strTypeId += c;
			}
			index ++;
		}
		shopsFile.save();
		player.sendMessage(
			lang.tr("Now players can " + what) + " " + RealShop.HashMapToCsv(addTo).replaceAll(",", ", ")
		);
	}

	//----------------------------------------------------------------------------------- shopAddSell
	public void shopAddSell(Player player, Block block, String command)
	{
		RealShop shop = shopsFile.shopAt(block);
		if (player.getName().equals(shop.player)) {
			shopAddExclBuySell(player, shop.sellOnly, command, "sell");
		} else {
			player.sendMessage(lang.tr("This chest belongs to") + " " + shop.player);
		}
	}

	//----------------------------------------------------------------------------------- shopExclBuy
	public void shopExclBuy(Player player, Block block, String command)
	{
		RealShop shop = shopsFile.shopAt(block);
		if (player.getName().equals(shop.player)) {
			shopAddExclBuySell(player, shop.buyExclude, command, "not buy");
		} else {
			player.sendMessage(lang.tr("This chest belongs to") + " " + shop.player);
		}
	}

	//---------------------------------------------------------------------------------- shopExclSell
	public void shopExclSell(Player player, Block block, String command)
	{
		RealShop shop = shopsFile.shopAt(block);
		if (player.getName().equals(shop.player)) {
			shopAddExclBuySell(player, shop.sellExclude, command, "not sell");
		} else {
			player.sendMessage(lang.tr("This chest belongs to") + " " + shop.player);
		}
	}

	//------------------------------------------------------------------------------- shopPricesInfos
	public void shopPricesInfos(Player player, Block block)
	{
		RealShop shop = shopsFile.shopAt(block);
		String list;
		// sell (may be a very long list)
		list = "";
		Iterator<Integer> sellIterator = shop.sellOnly.keySet().iterator();
		if (!sellIterator.hasNext()) {
			sellIterator = dataValuesFile.getIdsIterator();
		}
		while (sellIterator.hasNext()) {
			int typeId = sellIterator.next();
			RealPrice price = marketFile.getPrice(typeId);
			if ((price != null) && shop.isItemSellAllowed(typeId)) {
				if (!list.equals("")) {
					list += ", ";
				}
				list += dataValuesFile.getName(typeId) + ": " + price.sell;
			}
		}
		if (list.equals("")) {
			player.sendMessage(lang.tr("Nothing can be sold here"));
		} else {
			player.sendMessage(lang.tr("You can sell") + " " + list);
		}
		// buy (may be as long as the number of filled slots!) 
		list = "";
		RealItemStackHashMap itemStack = RealItemStackHashMap.create().storeInventory(
			RealInventory.create(RealChest.create(block)), false
		); 
		Iterator<RealItemStack> buyIterator = itemStack.getContents().iterator();
		while (buyIterator.hasNext()) {
			RealItemStack item = buyIterator.next();
			int typeId = item.getTypeId();
			RealPrice price = marketFile.getPrice(typeId);
			if ((price != null) && shop.isItemBuyAllowed(typeId)) {
				if (!list.equals("")) {
					list += ", ";
				}
				list += dataValuesFile.getName(typeId) + ": " + price.buy;
			}
		}
		if (list.equals("")) {
			player.sendMessage(lang.tr("Nothing to buy here"));
		} else {
			player.sendMessage(lang.tr("You can buy") + " " + list);
		}
	}

}
