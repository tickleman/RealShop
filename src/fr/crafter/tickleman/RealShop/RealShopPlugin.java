package fr.crafter.tickleman.RealShop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

import fr.crafter.tickleman.RealEconomy.RealEconomy;
import fr.crafter.tickleman.RealPlugin.RealBlock;
import fr.crafter.tickleman.RealPlugin.RealChest;
import fr.crafter.tickleman.RealPlugin.RealColor;
import fr.crafter.tickleman.RealPlugin.RealDataValuesFile;
import fr.crafter.tickleman.RealPlugin.RealInventory;
import fr.crafter.tickleman.RealPlugin.RealItemStack;
import fr.crafter.tickleman.RealPlugin.RealItemStackHashMap;
import fr.crafter.tickleman.RealPlugin.RealPlugin;
import fr.crafter.tickleman.RealPlugin.RealTime;
import fr.crafter.tickleman.RealPlugin.RealTools;
import fr.crafter.tickleman.RealPlugin.RealTranslationFile;

//################################################################################## RealShopPlugin
public class RealShopPlugin extends RealPlugin
{

	/** Global configuration */
	public RealShopConfig config;

	/** Says if the player is into a chest, and stores chest state info */
	public final HashMap<String, RealInChestState> inChestStates = new HashMap<String, RealInChestState>();

	/** The last chest selected by the each player (ie "tickleman1" => "world,x,y,z" */
	public final HashMap<String, String> lastSelectedChest = new HashMap<String, String>(); 

	/** Says if the player is into a chest, and stores chest state info */
	public final HashMap<String, String> lockedChests = new HashMap<String, String>(); 

	/** Daily log stores movements for each buy / sold item */
	public RealShopDailyLog dailyLog = null;

	/** Number of players that have opened a chest-shop */
	public int playersInChestCounter = 0;

	/** Data values files : complete list of Minecraft blocks and items */
	public RealDataValuesFile dataValuesFile;

	/** Market prices file (market.txt) : global market price for each item */
	public RealPricesFile marketFile;

	/** Shops list and file link */
	public RealShopsFile shopsFile;

	/** Last day time, per world (ie : in reality this is the time of the NEXT day change) */
	public HashMap<String, Long> lastDayTime = new HashMap<String, Long>();

	/** Block events Listener */
	private final RealShopBlockListener blockListener = new RealShopBlockListener(this);

	/** Player events Listener */
	private final RealShopPlayerListener playerListener = new RealShopPlayerListener(this);

	/** Plugin events Listener */
	private final RealShopPluginListener pluginListener = new RealShopPluginListener(this);

	/** RealEconomy */
	public final RealEconomy realEconomy;

	//-------------------------------------------------------------------------------- RealShopPlugin
	public RealShopPlugin()
	{
		super("tickleman", "RealShop", "0.56");
		realEconomy = new RealEconomy(this);
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
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.INVENTORY_OPEN, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Normal, this);
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
		if (config.economyPlugin.equals("RealEconomy")) {
			log.info("Uses built-in RealEconomy (/mny commands) as economy system", true);
		}
		pluginListener.onPluginEnabled(null);
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
		RealShop shop = shopsFile.shopAt(inChestState.block);
		inChestState.chest = RealChest.create(block);
		String chestId = inChestState.chest.getChestId();
		String otherPlayerName = lockedChests.get(chestId); 
		if (otherPlayerName != null) {
			player.sendMessage(
				RealColor.cancel
				+ lang.tr("The shop +name is already in use by player +client")
				.replace("+client", RealColor.player + otherPlayerName + RealColor.cancel)
				.replace("+name", RealColor.shop + shop.name + RealColor.cancel)
				.replace("  ", " ")
			);
			inChestStates.remove(playerName);
			return false;
		} else {
			lockedChests.put(chestId, playerName);
			inChestState.lastX = Math.round(player.getLocation().getX());
			inChestState.lastZ = Math.round(player.getLocation().getZ());
			inChestState.itemStackHashMap = RealItemStackHashMap.create().storeInventory(
					RealInventory.create(inChestState.chest), false
			);
			// shop information
			if (shop.player.equals(playerName)) {
				player.sendMessage(
					RealColor.message
					+ lang.tr("Welcome into your shop +name")
					.replace("+name", RealColor.shop + shop.name + RealColor.message)
					.replace("  ", " ")
				);
			} else {
				player.sendMessage(
					RealColor.message
					+ lang.tr("Welcome into +owner's shop +name. You've got +money in your pocket")
					.replace("+money", RealColor.price + realEconomy.getBalance(player.getName(), true) + RealColor.message)
					.replace("+name", RealColor.shop + shop.name + RealColor.message)
					.replace("+owner", RealColor.player + shop.player + RealColor.message)
					.replace("  ", " ")
				);
			}
			playersInChestCounter = inChestStates.size();
			return true;
		}
	}

	//------------------------------------------------------------------------------------- exitChest
	public void exitChest(Player player, boolean playerQuits)
	{
		String playerName = player.getName();
		RealInChestState inChestState = inChestStates.get(playerName);
		if (inChestState != null) {
			if (inChestState.inChest) {
				inChestState.inChest = false;
				RealShop shop = shopsFile.shopAt(inChestState.block);
				String shopPlayerName = shop.player;
				Player shopPlayer = getServer().getPlayer(shopPlayerName);
				boolean had_message = false;
				if (!shop.player.equals(player.getName())) {
					RealPricesFile pricesFile = RealPricesFile.playerPricesFile(
						this, shopPlayerName, marketFile
					);
					// remove new chest's inventory items from old chest's inventory
					// in order to know how many of each has been buy (positive) / sold (negative)
					inChestState.itemStackHashMap.storeInventory(
						RealInventory.create(inChestState.chest), true
					);
					// prepare bill
					RealShopTransaction transaction = RealShopTransaction.create(
						this, playerName, shopPlayerName, inChestState.itemStackHashMap, pricesFile, marketFile
					).prepareBill(shopsFile.shopAt(inChestState.block));
					if (transaction.isCancelled()) {
						// transaction is fully cancelled : items go back in their original inventories
						ArrayList<RealItemStack> itemStackList = inChestState.itemStackHashMap.getContents();
						RealInventory inv = RealInventory.create(player);
						if (!inv.storeRealItemStackList(itemStackList, true, false)) {
							logStolenItems(player, shop, inv.errorLog);
						} else if (playerQuits) {
							logStolenItems(player, shop, itemStackList);
						} else {
							RealInventory
							.create(inChestState.chest)
							.storeRealItemStackList(itemStackList, false, false);
						}
						player.sendMessage(RealColor.cancel + lang.tr("Cancelled transaction"));
						had_message = true;
					} else {
						// some lines cancelled : corresponding items go back to their original inventories
						if (!transaction.cancelledLines.isEmpty()) {
							RealInventory inv = RealInventory.create(player);
							if (!inv.storeRealItemStackList(transaction.cancelledLines, true, false)) {
								logStolenItems(player, shop, inv.errorLog);
							} else if (playerQuits) {
								logStolenItems(player, shop, transaction.cancelledLines);
							} else {
								RealInventory
								.create(inChestState.chest)
								.storeRealItemStackList(transaction.cancelledLines, false, false);
							}
							// display cancelled lines information
							Iterator<RealShopTransactionLine> iterator = transaction.cancelledLines.iterator();
							while (iterator.hasNext()) {
								RealShopTransactionLine line = iterator.next();
								String strSide = (line.getAmount() < 0) ? "sale" : "purchase";
								log.info(
									("[shop +name] +owner > +client: cancelled " + strSide + " +item x+quantity (+linePrice) +comment")
									.replace("+name", shop.name)
									.replace("+owner", shop.player)
									.replace("+client", playerName)
									.replace("+item", line.getTypeIdDamage() + " (" + dataValuesFile.getName(line.getTypeIdDamage()) + ")")
									.replace("+linePrice", "" + Math.abs(line.getLinePrice()))
									.replace("+price", "" + line.getUnitPrice())
									.replace("+quantity", "" + Math.abs(line.getAmount()))
									.replace("+comment", line.comment)
									.replace("  ", " ").replace(" ]", "]").replace("[ ", "[")
								);
								player.sendMessage(
									RealColor.cancel
									+ lang.tr("Cancelled " + strSide + " +item x+quantity (+linePrice) +comment")
									.replace("+item", RealColor.item + dataValuesFile.getName(line.getTypeIdDamage()) + RealColor.cancel)
									.replace("+linePrice", RealColor.price + Math.abs(line.getLinePrice()) + RealColor.cancel)
									.replace("+price", RealColor.price + line.getUnitPrice() + RealColor.cancel)
									.replace("+quantity", RealColor.quantity + Math.abs(line.getAmount()) + RealColor.cancel)
									.replace("+comment", lang.tr(line.comment))
								);
								had_message = true;
							}
						}
						boolean transactionOk = false;
						// update player's account
						if (realEconomy.setBalance(
							playerName, realEconomy.getBalance(playerName) - transaction.getTotalPrice() 
						)) {
							// update shop player's account
							if (realEconomy.setBalance(
									shopPlayerName, realEconomy.getBalance(shopPlayerName) + transaction.getTotalPrice()
							)) {
								transactionOk = true;
							} else {
								// rollback if any error
								realEconomy.setBalance(
									playerName, realEconomy.getBalance(playerName) + transaction.getTotalPrice() 
								);
							}
						}
						if (transactionOk) { 
							// store transaction lines into daily log
							dailyLog.addTransaction(transaction);
							// display transaction lines information
							Iterator<RealShopTransactionLine> iterator = transaction.transactionLines.iterator();
							while (iterator.hasNext()) {
								RealShopTransactionLine transactionLine = iterator.next();
								String strSide, shopStrSide;
								if (transactionLine.getAmount() < 0) {
									strSide = "Sold";
									shopStrSide = "sold";
									if (shop.getFlag("infiniteSell", config.shopInfiniteSell.equals("true"))) {
										// infinite sell : remove new items from chest
										if (
											!RealInventory.create(inChestState.chest)
											.storeRealItemStack(transactionLine, false)
										) {
											log.severe(
												"Can't infiniteSell " + transactionLine.getTypeId()
												+ " " + transactionLine.getAmount()
											);
										}
									}
								} else {
									strSide = "Purchased";
									shopStrSide = "purchased";
									if (shop.getFlag("infiniteBuy", config.shopInfiniteBuy.equals("true"))) {
										// infinite buy : create items back into chest
										RealInventory
										.create(inChestState.chest)
										.storeRealItemStack(transactionLine, false);
									}
								}
								log.info(
									("[shop +name] +owner > +client: " + strSide + " +item x+quantity (+linePrice)")
									.replace("+name", shop.name)
									.replace("+owner", shop.player)
									.replace("+client", playerName)
									.replace("+item", transactionLine.getTypeIdDamage() + " (" + dataValuesFile.getName(transactionLine.getTypeIdDamage()) + ")")
									.replace("+linePrice", "" + Math.abs(transactionLine.getLinePrice()))
									.replace("+price", "" + transactionLine.getUnitPrice())
									.replace("+quantity", "" + Math.abs(transactionLine.getAmount()))
								);
								player.sendMessage(
									RealColor.text
									+ lang.tr(strSide + " +item x+quantity (+linePrice)")
									.replace("+client", RealColor.player + playerName + RealColor.text)
									.replace("+item", RealColor.item + dataValuesFile.getName(transactionLine.getTypeIdDamage()) + RealColor.text)
									.replace("+linePrice", RealColor.price + Math.abs(transactionLine.getLinePrice()) + " " + realEconomy.getCurrency() + RealColor.text)
									.replace("+name", RealColor.shop + shop.name + RealColor.text)
									.replace("+owner", RealColor.player + shop.player + RealColor.text)
									.replace("+price", RealColor.price + transactionLine.getUnitPrice() + " " + realEconomy.getCurrency() + RealColor.text)
									.replace("+quantity", RealColor.quantity + Math.abs(transactionLine.getAmount()) + RealColor.text)
									.replace("  ", " ").replace(" ]", "]").replace("[ ", "[")
								);
								if (shopPlayer != null) {
									shopPlayer.sendMessage(
										RealColor.text
										+ lang.tr("[shop +name] +client " + shopStrSide + " +item x+quantity (+linePrice)")
										.replace("+client", RealColor.player + playerName + RealColor.text)
										.replace("+item", RealColor.item + dataValuesFile.getName(transactionLine.getTypeIdDamage()) + RealColor.text)
										.replace("+linePrice", RealColor.price + Math.abs(transactionLine.getLinePrice()) + " " + realEconomy.getCurrency() + RealColor.text)
										.replace("+name", RealColor.shop + shop.name + RealColor.text)
										.replace("+owner", RealColor.player + shop.player + RealColor.text)
										.replace("+price", RealColor.price + transactionLine.getUnitPrice() + " " + realEconomy.getCurrency() + RealColor.text)
										.replace("+quantity", RealColor.quantity + Math.abs(transactionLine.getAmount()) + RealColor.text)
										.replace("  ", " ").replace(" ]", "]").replace("[ ", "[")
									);
								}
								had_message = true;
							}
						}
					}
				}
				if (!had_message) {
					player.sendMessage(
						RealColor.message
						+ lang.tr("No transaction")
						.replace("+client", RealColor.player + playerName + RealColor.message)
						.replace("+name", RealColor.shop + shop.name + RealColor.message)
						.replace("+owner", RealColor.player + shop.player + RealColor.message)
						.replace("  ", " ")
					);
				}
			}
			lockedChests.remove(inChestState.chest.getChestId());
			inChestStates.remove(playerName);
			playersInChestCounter = inChestStates.size();
		}
	}

	//------------------------------------------------------------------------------------------ help
	public void help(Player player, boolean isOp, String page)
	{
		// choose help file
		String l = language;
		if (!RealTools.fileExists(getDataFolder() + "/" + l + ".help.txt")) {
			RealTools.extractDefaultFile(this, l + ".help.txt");
			if (!RealTools.fileExists(getDataFolder() + "/" + l + ".help.txt")) {
				l = "en";
				if (!RealTools.fileExists(getDataFolder() + "/" + l + ".help.txt")) {
					RealTools.extractDefaultFile(this, l + ".help.txt");
					if (!RealTools.fileExists(getDataFolder() + "/" + l + ".help.txt")) {
						log.severe("No help file " + getDataFolder() + "/" + language + ".help.txt");
						player.sendMessage(RealColor.cancel + lang.tr("/rshop HELP is not available"));
						return;
					}
				}
			}
		}
		// display help file
		try {
			if (page.equals("")) {
				player.sendMessage(RealColor.text + lang.tr("/rshop HELP summary"));
			}
			BufferedReader reader = new BufferedReader(
				new FileReader(getDataFolder() + "/" + l + ".help.txt")
			);
			String buffer;
			boolean inside = false;
			while ((buffer = reader.readLine()) != null) {
				buffer = buffer.trim();
				if (!buffer.equals("") && (buffer.charAt(0) != '#')) {
					String[] hlp = buffer.split(":");
					hlp[0] = hlp[0].trim();
					if (hlp.length > 1) {
						hlp[1] = hlp[1].trim();
					}
					if ((buffer.charAt(0) == '[') && (buffer.charAt(buffer.length() - 1) == ']')) {
						// section header [help1|h|1 : text]
						hlp[0] = hlp[0].substring(1).trim();
						hlp[1] = hlp[1].substring(0, hlp[1].length() - 1).trim();
						if (page.equals("")) {
							// summary : display
							player.sendMessage(
								RealColor.command + "/rshop help " + hlp[0] + " "
								+ RealColor.message + hlp[1]
							);
						} else {
							// help page : check if in this section header and display title
							inside = (("|" + hlp[0] + "|").indexOf("|" + page + "|") > -1);
							if (inside) {
								player.sendMessage(
									RealColor.text
									+ "/rshop HELP " + hlp[1]
									+ " (" + hlp[0] + ")"
								);
							}
						}
					} else if (inside && buffer.charAt(0) == '/') {
						// display help page command
						player.sendMessage(RealColor.command + hlp[0] + " " + RealColor.message + hlp[1]);
					} else if (inside) {
						// display help page line
						player.sendMessage(RealColor.message + buffer);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			log.severe(e.getMessage());
			log.severe(e.getStackTrace().toString());
		}
	}

	//-------------------------------------------------------------------------------- logStolenItems
	public void logStolenItems(Player player, RealShop shop, ArrayList<? extends RealItemStack> items)
	{
		for (RealItemStack item : items) {
			log.warning(
				("[shop +name] +owner > +client: stolen +item x+quantity item duplicated !")
				.replace("+name", shop.name)
				.replace("+owner", shop.player)
				.replace("+client", player.getName())
				.replace("+item", item.getTypeIdDamage() + " (" + dataValuesFile.getName(item.getTypeIdDamage()) + ")")
				.replace("+quantity", "" + Math.abs(item.getAmount()))
				.replace("  ", " ").replace(" ]", "]").replace("[ ", "[")
			);
			player.sendMessage(
				RealColor.cancel
				+ lang.tr("Stolen +item x+quantity item duplicated !")
				.replace("+item", RealColor.item + dataValuesFile.getName(item.getTypeIdDamage()) + RealColor.cancel)
				.replace("+quantity", RealColor.quantity + Math.abs(item.getAmount()) + RealColor.cancel)
			);
		}
	}

	//------------------------------------------------------------------------------- onPlayerCommand
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (sender instanceof Player) {
			Player player = (Player)sender;
			if (playersInChestCounter > 0) {
				exitChest(player, true);
			}
			String command = cmd.getName().toLowerCase();
			for (int i = 0; i < args.length; i++) {
				args[i] = args[i];
			}
			if (
				(command.equals("rs") || command.equals("rshop"))
				&& (player.isOp() || config.shopOpOnly.equals("false"))
			) {
				boolean isOp = player.isOp();
				String playerName = player.getName();
				String lastChestKey = lastSelectedChest.get(playerName);
				String param = ((args.length > 0) ? args[0] : "");
				String param2 = ((args.length > 1) ? args[1] : "");
				String param3 = ((args.length > 2) ? args[2] : "");
				String param4 = ((args.length > 3) ? args[3] : "");
				// /rshop commands that do not need to be into a shop
				if (param.equals("help") || param.equals("h") || param.equals("?")) {
					help(player, isOp, param2);
				} else if (isOp && (param.equals("reload") || param.equals("rel"))) {
					reload(player);
				} else if (isOp && (param.equals("check") || param.equals("chk"))) {
					pluginInfos(player);
				} else if (isOp && (param.equals("market") || param.equals("m"))) {
					if (param2.equals("")) {
						pluginInfosPrices(player);
					} else if (param2.equals("del") || param2.equals("d")) {
						marketFile.prices.remove(param3);
						marketFile.save();
						player.sendMessage(
							RealColor.message
							+ lang.tr("Market price deleted for +item")
							.replace("+item", RealColor.item + dataValuesFile.getName(param3) + RealColor.message)
						);
					} else if (args.length > 2) {
						try {
							RealPrice price = new RealPrice(
								Double.parseDouble(param3), Double.parseDouble(param4.equals("") ? param3 : param4)
							);
							marketFile.prices.put(param2, price);
							marketFile.save();
							player.sendMessage(
								RealColor.message
								+ lang.tr("Market price for +item : buy +buy, sell +sell")
								.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.message)
								.replace("+buy", RealColor.price + price.buy + RealColor.message)
								.replace("+sell", RealColor.price + price.sell + RealColor.message)
							);
						} catch (Exception e) {
							player.sendMessage(
								RealColor.cancel
								+ lang.tr("Error while setting market price for +item")
								.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.cancel)
							);
							player.sendMessage(
								RealColor.message
								+ lang.tr("Usage: +command")
								.replace("+command", RealColor.command + lang.tr("/rshop market <itemId>[:<itemDamage>] <sellPrice> <buyPrice>") + RealColor.message)
							);
						}
					} else {
						RealPrice price = marketFile.prices.get(param2);
						if (price == null) {
							player.sendMessage(
								RealColor.cancel
								+ lang.tr("No market price for +item")
								.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.cancel)
							);
							price = marketFile.getPrice(param2, null);
							if (price == null) {
								player.sendMessage(
									RealColor.cancel
									+ lang.tr("Price can't be calculated from recipes for +item")
									.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.cancel)
								);
							} else {
								player.sendMessage(
									RealColor.message
									+ lang.tr("Calculated price (from recipes) for +item : buy +buy, sell +sell")
									.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.message)
									.replace("+buy", RealColor.price + price.buy + RealColor.message)
									.replace("+sell", RealColor.price + price.sell + RealColor.message)
								);
							}
						} else {
							player.sendMessage(
								RealColor.message
								+ lang.tr("Market price for +item : buy +buy, sell +sell")
								.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.message)
								.replace("+buy", RealColor.price + price.buy + RealColor.message)
								.replace("+sell", RealColor.price + price.sell + RealColor.message)
							);
						}
					}
				} else if (param.equals("price") || param.equals("p")) {
					if (param2.equals("")) {
						pluginInfosPlayerPrices(player);
					} else if (param2.equals("del") || param2.equals("d")) {
						RealPricesFile pricesFile = RealPricesFile.playerPricesFile(
							this, player.getName(), null
						);
						pricesFile.prices.remove(param3);
						pricesFile.save();
						player.sendMessage(
							RealColor.message
							+ lang.tr("Player's price deleted for +item")
							.replace("+item", RealColor.item + dataValuesFile.getName(param3) + RealColor.message)
						);
					} else if (args.length > 2) {
						RealPricesFile pricesFile = RealPricesFile.playerPricesFile(
							this, player.getName(), null
						);
						try {
							RealPrice price = new RealPrice(
								Double.parseDouble(param3), Double.parseDouble(param4.equals("") ? param3 : param4)
							);
							pricesFile.prices.put(param2, price);
							pricesFile.save();
							player.sendMessage(
								RealColor.message
								+ lang.tr("Player's price for +item : buy +buy, sell +sell")
								.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.message)
								.replace("+buy", RealColor.price + price.buy + RealColor.message)
								.replace("+sell", RealColor.price + price.sell + RealColor.message)
							);
						} catch (Exception e) {
							player.sendMessage(
								RealColor.cancel
								+ lang.tr("Error while setting player's price for +item")
								.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.cancel)
							);
							player.sendMessage(
								RealColor.message
								+ lang.tr("Usage: +command")
								.replace("+command", RealColor.command + lang.tr("/rshop price <itemId>[:<itemDamage>] <sellPrice> <buyPrice>") + RealColor.message)
							);
						}
					} else {
						RealPricesFile pricesFile = RealPricesFile.playerPricesFile(
							this, player.getName(), null
						);
						RealPrice price = pricesFile.prices.get(param2);
						if (price == null) {
							player.sendMessage(
								RealColor.cancel
								+ lang.tr("No player's price for +item")
								.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.cancel)
							);
							price = pricesFile.getPrice(param2, marketFile);
							if (price == null) {
								player.sendMessage(
									RealColor.cancel
									+ lang.tr("Price can't be calculated from recipes for +item")
									.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.cancel)
								);
							} else {
								player.sendMessage(
									RealColor.message
									+ lang.tr("Calculated price (from market/recipes) for +item : buy +buy, sell +sell")
									.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.message)
									.replace("+buy", RealColor.price + price.buy + RealColor.message)
									.replace("+sell", RealColor.price + price.sell + RealColor.message)
								);
							}
						} else {
							player.sendMessage(
								RealColor.message
								+ lang.tr("Player's price for +item : buy +buy, sell +sell")
								.replace("+item", RealColor.item + dataValuesFile.getName(param2) + RealColor.message)
								.replace("+buy", RealColor.price + price.buy + RealColor.message)
								.replace("+sell", RealColor.price + price.sell + RealColor.message)
							);
						}
					}
				} else if (isOp && param.equals("log")) {
					pluginInfosDailyLog(player);
					player.sendMessage(
						RealColor.text
						+ lang.tr("Daily log was dumped into the realshop.log file")
					);
				} else if (isOp && (param.equals("simul") || param.equals("sim"))) {
					marketFile.dailyPricesCalculation(dailyLog, true);
					player.sendMessage(
						RealColor.text
						+ lang.tr("Daily prices calculation simulation is into the realshop.log file")
					);
				} else if (isOp && (param.equals("daily") || param.equals("day"))) {
					marketFile.dailyPricesCalculation(dailyLog);
					player.sendMessage(
						RealColor.text
						+ lang.tr("Real daily prices calculation log is into the realshop.log file")
					);
				} else if (lastChestKey == null) {
					// no chest selected
					player.sendMessage(
						RealColor.cancel
						+ lang.tr("You must select a chest-shop before typing any /rshop command")
					);
				} else {
					Block block = RealBlock.fromStrId(this, lastChestKey);
					Block neighbor = RealChest.scanForNeighborChest(
						block.getWorld(), block.getX(), block.getY(), block.getZ()
					);
					RealShop shop = shopsFile.shopAt(lastChestKey);
					if (shop == null) {
						// /rshop commands on a chests that is not a shop
						if (param.equals("create") || param.equals("c")) {
							registerBlockAsShop(player, block, param2);
						} else {
							player.sendMessage(
								RealColor.cancel
								+ lang.tr("The chest you selected is not a shop")
							);
						}
					} else if (param.equals("")) {
						shopInfo(player, block);
					} else if (player.isOp() || playerName.equals(shop.player)) {
						// /rshop commands on a chest that is a shop that belongs to me
						if (param.equals("delete")) {
							registerBlockAsShop(player, block, param2);
						} else if (param.equals("open") || param.equals("o")) {
							shop.opened = true;
							player.sendMessage(
								RealColor.message
								+ lang.tr("The shop +name is now opened")
								.replace("+name", RealColor.shop + shop.name + RealColor.message)
								.replace("  ", " ")
							);
						} else if (param.equals("close") || param.equals("c")) {
							shop.opened = false;
							player.sendMessage(
									RealColor.message
									+ lang.tr("The shop +name is now closed")
									.replace("+name", RealColor.shop + shop.name + RealColor.message)
									.replace("  ", " ")
								);
						} else if (param.equals("buy") || param.equals("b")) {
							shopAddBuy(player, block, param2, false);
							if (neighbor != null) shopAddBuy(player, neighbor, param2, true); 
						} else if (param.equals("sell") || param.equals("s")) {
							shopAddSell(player, block, param2, false);
							if (neighbor != null) shopAddSell(player, neighbor, param2, true);
						} else if (param.equals("xbuy") || param.equals("xb")) {
							shopExclBuy(player, block, param2, false);
							if (neighbor != null) shopExclBuy(player, neighbor, param2, true);
						} else if (param.equals("xsell") || param.equals("xs")) {
							shopExclSell(player, block, param2, false);
							if (neighbor != null) shopExclSell(player, neighbor, param2, true);
						} else if (param.equals("give")) {
							shopGive(player, block, param2, false);
							if (neighbor != null) shopGive(player, neighbor, param2, true);
						} else if (isOp && (param.equals("infiniteBuy") || param.equals("ib"))) {
							shop.setFlag("infiniteBuy", param2);
							player.sendMessage(
								RealColor.message
								+ lang.tr("Infinite buy flag is")
								+ " " + RealColor.command
								+ lang.tr(shop.getFlag("infiniteBuy", config.shopInfiniteBuy.equals("true")) ? "on" : "off")
							);
						} else if (isOp && (param.equals("infiniteSell") || param.equals("is"))) {
							shop.setFlag("infiniteSell", param2);
							player.sendMessage(
								RealColor.message
								+ lang.tr("Infinite sell flag is")
								+ " " + RealColor.command
								+ lang.tr(shop.getFlag("infiniteSell", config.shopInfiniteSell.equals("true")) ? "on" : "off")
							);
						} else if (param.equals("marketItemsOnly") || param.equals("mi")) {
							shop.setFlag("marketItemsOnly", param2);
							player.sendMessage(
								RealColor.message
								+ lang.tr("Trade market items only flag is")
								+ " " + RealColor.command
								+ lang.tr(shop.getFlag("marketItemsOnly", config.shopMarketItemsOnly.equals("true")) ? "on" : "off")
							);
						} else if (param.equals("damagedItems") || param.equals("di")) {
							shop.setFlag("damagedItems", param2);
							player.sendMessage(
								RealColor.message
								+ lang.tr("Damaged item buy/sell flag is")
								+ " " + RealColor.command
								+ lang.tr(shop.getFlag("damagedItems", config.shopDamagedItems.equals("true")) ? "on" : "off")
							);
						}
					} else {
						// /rshop commands on a chest that is a shop that belongs to someone else
						player.sendMessage(
								RealColor.cancel
								+ lang.tr("The chest-shop you selected belongs to +owner")
								.replace("+name", RealColor.shop + shop.name + RealColor.cancel)
								.replace("+owner", RealColor.player + shop.player + RealColor.cancel)
								.replace("  ", " ")
						);
					}
				}
				return true;
			} else if (command.equals("mny")) {
				if (config.economyPlugin.equals("RealEconomy")) {
					// simple /mny commands
					String param = ((args.length > 0) ? args[0] : "");
					String playerName = player.getName();
					if (param.equals("help") || param.equals("h")) {
						// HELP
						player.sendMessage(RealColor.doc + "RealEconomy help");
						player.sendMessage(RealColor.command + "/mny" + RealColor.doc + " : tell me how many money I have in my pocket");
						player.sendMessage(RealColor.command + "/mny give <player> <amount>" + RealColor.doc + " : give money to another player");
						player.sendMessage(RealColor.command + "/mny burn <amount>" + RealColor.doc + " : burn your money");
						if (player.isOp()) {
							player.sendMessage("RealEconomy operator help");
							player.sendMessage(RealColor.command + "/mny tell <player>" + RealColor.doc + " : tell me how many money the player has");
							player.sendMessage(RealColor.command + "/mny set <player> <balance>" + RealColor.doc + " : sets the balance of a player");
							player.sendMessage(RealColor.command + "/mny inc <player> <amount>" + RealColor.doc + " : increase balance of a player");
							player.sendMessage(RealColor.command + "/mny dec <player> <amount>" + RealColor.doc + " : decrease the balance of a player");
							//player.sendMessage("/mny top [<count>] : tell the top count players");
						}
		 			} else if (param.equals("")) {
		 				// NO PARAM : BALANCE
		 				player.sendMessage(
	 						RealColor.message + "You've got "
	 						+ RealColor.price + realEconomy.getBalance(playerName) + realEconomy.getCurrency()
	 						+ RealColor.message + " in your pocket"
	 					);
		 			} else if (param.equals("give") || param.equals("g")) {
		 				// GIVE MONEY
		 				String toPlayerName = ((args.length > 1) ? args[1] : "");
						double amount;
						try {
							amount = ((args.length > 2) ? Double.parseDouble(args[2]) : 0);
						} catch (Exception e) {
							amount = 0;
						}
						if (amount > 0) {
							if (realEconomy.getBalance(playerName) >= amount) {
								// transfer money with rollback
								if (realEconomy.setBalance(
									playerName, realEconomy.getBalance(playerName) - amount
								)) {
									if (!realEconomy.setBalance(
											toPlayerName, realEconomy.getBalance(toPlayerName) + amount
									)) {
										realEconomy.setBalance(
											playerName, realEconomy.getBalance(playerName) + amount
										);
									}
								}
								player.sendMessage(
									RealColor.message + "You give " + RealColor.price + amount + realEconomy.getCurrency()
									+ RealColor.message + " to " + RealColor.player + toPlayerName
								);
								Player toPlayer = getServer().getPlayer(toPlayerName);
								if (toPlayer != null) {
									toPlayer.sendMessage(
										RealColor.player + playerName
										+ RealColor.message + " gives you "
										+ RealColor.price + amount + realEconomy.getCurrency()
									);
								}
								log.info(
									RealColor.player + playerName
									+ RealColor.message + " gives "
									+ RealColor.price + amount + realEconomy.getCurrency()
									+ RealColor.message + " to " + RealColor.player + toPlayerName
								);
							} else {
								player.sendMessage(
									RealColor.cancel + "You don't have enough "
									+ RealColor.price + realEconomy.getCurrency()
								);
							}
						}
		 			} else if (param.equals("burn") || param.equals("b")) {
		 				double amount;
		 				try {
		 					amount = ((args.length > 2) ? Double.parseDouble(args[2]) : 0);
		 				} catch (Exception e) {
		 					amount = 0;
		 				}
		 				amount = Math.min(realEconomy.getBalance(playerName), amount);
		 				if (amount > 0) {
							realEconomy.setBalance(
								playerName, realEconomy.getBalance(playerName) - amount
							);
							player.sendMessage(
								RealColor.message + "You burned "
								+ RealColor.price + amount + realEconomy.getCurrency()
							);
		 				}
		 			} else if (player.isOp()) {
		 				if (param.equals("tell") || param.equals("t")) {
		 					String toPlayerName = ((args.length > 1) ? args[1] : "");
		 					// TELL
		 					player.sendMessage(
		 						RealColor.player + toPlayerName + RealColor.message + " has got "
		 						+ RealColor.price + realEconomy.getBalance(playerName) + realEconomy.getCurrency()
		 						+ RealColor.message + " in his pocket"
		 					);
		 				} else if (param.equals("set") || param.equals("s")) {
		 					// SET
		 					String toPlayerName = ((args.length > 1) ? args[1] : "");
		 					double amount;
		 					try {
		 						amount = ((args.length > 2) ? Double.parseDouble(args[2]) : 0);
		 					} catch (Exception e) {
		 						amount = 0;
		 					}
							realEconomy.setBalance(toPlayerName, amount);
							player.sendMessage(
								RealColor.player + toPlayerName
								+ RealColor.message + " balance set to "
								+ RealColor.price + amount + realEconomy.getCurrency()
							);
							Player toPlayer = getServer().getPlayer(toPlayerName);
							if (toPlayer != null) {
								toPlayer.sendMessage(
									RealColor.player + playerName
									+ RealColor.message + " sets your balance to "
									+ RealColor.price + amount + realEconomy.getCurrency()
								);
							}
		 				} else if (param.equals("inc") || param.equals("i")) {
		 					// INC
		 					String toPlayerName = ((args.length > 1) ? args[1] : "");
		 					double amount;
		 					try {
		 						amount = ((args.length > 2) ? Double.parseDouble(args[2]) : 0);
		 					} catch (Exception e) {
		 						amount = 0;
		 					}
		 					realEconomy.setBalance(
		 						toPlayerName, realEconomy.getBalance(toPlayerName) + amount
		 					);
							player.sendMessage(
								RealColor.message + "You increase "
								+ RealColor.player + toPlayerName
								+ RealColor.message + "'s balance of " 
								+ RealColor.price + amount + realEconomy.getCurrency()
							);
							Player toPlayer = getServer().getPlayer(toPlayerName);
							if (toPlayer != null) {
								toPlayer.sendMessage(
									RealColor.player + playerName
									+ RealColor.message + " increased your balance of "
									+ RealColor.price + amount + realEconomy.getCurrency()
								);
							}
							log.info(
								RealColor.player + playerName
								+ RealColor.message + " increases the balance of "
								+ RealColor.player + toPlayerName
								+ RealColor.message + " of "
								+ RealColor.price + amount + realEconomy.getCurrency()
							);
		 				} else if (param.equals("dec") || param.equals("d")) {
		 					// DEC
		 					String toPlayerName = ((args.length > 1) ? args[1] : "");
		 					double amount;
		 					try {
		 						amount = ((args.length > 2) ? Double.parseDouble(args[2]) : 0);
		 					} catch (Exception e) {
		 						amount = 0;
		 					}
		 					amount = Math.min(realEconomy.getBalance(toPlayerName), amount);
		 					realEconomy.setBalance(
		 						toPlayerName, realEconomy.getBalance(toPlayerName) - amount
		 					);
							player.sendMessage(
								RealColor.message + "You decrease "
								+ RealColor.player + toPlayerName
								+ RealColor.message + "'s balance of "
								+ RealColor.price + amount + realEconomy.getCurrency()
							);
							Player toPlayer = getServer().getPlayer(toPlayerName);
							if (toPlayer != null) {
								toPlayer.sendMessage(
									RealColor.player + playerName
									+ RealColor.message + " decreased your balance of "
									+ RealColor.price + amount + realEconomy.getCurrency()
								);
							}
							log.info(
								RealColor.player + playerName
								+ RealColor.message + " decreases the balance of "
								+ RealColor.player + toPlayerName
								+ RealColor.message + " of "
								+ RealColor.price + amount + realEconomy.getCurrency()
							);
		 				} else if (param.equals("top")) {
		 					// TOP
		 					/*
		 					int count;
		 					try {
		 						count = ((args.length > 1) ? Integer.parseInt(args[1]) : 0);
		 					} catch (Exception e) {
		 						count = 0;
		 					}
		 					int subCount = 0;
		 					while ((count == 0) || (subCount < count)) {
		 						
		 					}
		 					*/
		 				} else {
		 					return false;
		 				}
	 				} else {
	 					return true;
	 				}
	 			} else {
	 				return true;
	 			}
			} else {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	//--------------------------------------------------------------------------- registerBlockAsShop
	public void registerBlockAsShop(Player player, Block block, String shopName)
	{
		registerBlockAsShop(player, block, shopName, 0);
	}

	//--------------------------------------------------------------------------- registerBlockAsShop
	/**
	 * mode  0 to auto register/unregister
	 * mode  1 to force register of a neighbor chest
	 * mode -1 to force unregister of a neighbor chest
	 */
	private void registerBlockAsShop(Player player, Block block, String shopName, int mode)
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
			// remove shop
			shopsFile.shops.remove(key);
			if (neighborBlock != null) {
				registerBlockAsShop(player, neighborBlock, shopName, -1);
			}
			shopsFile.save();
			message = RealColor.message + lang.tr("The shop +name has been deleted")
			.replace("+name", RealColor.shop + shop.name + RealColor.message)
			.replace("  ", " ");
		} else {
			// if shop did not exist or force creation, then add shop
			shop = new RealShop(
				block.getWorld().getName(), block.getX(), block.getY(), block.getZ(),
				playerName
			);
			shop.name = shopName;
			shop.sellOnly.put("0", true);
			shopsFile.shops.put(key, shop);
			if (neighborBlock != null) {
				registerBlockAsShop(player, neighborBlock, shopName, 1);
			}
			shopsFile.save();
			message = RealColor.message
			+ lang.tr("The shop +name has been created")
			.replace("+name", RealColor.shop + shop.name + RealColor.message)
			.replace("  ", " ");
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
			if (players.equals("")) {
				players = iterator.next();
			} else {
				players += ", " + iterator.next();
			}
		}
		player.sendMessage(RealColor.player + playersInChestCounter + " players" + RealColor.message + " in chest counter");
		player.sendMessage(RealColor.message + "inChestStates for " + RealColor.player + players);
		player.sendMessage(RealColor.message + shopsFile.shops.size() + " opened shops");
		player.sendMessage(RealColor.price + marketFile.prices.size() + " market prices");
		log.info(playersInChestCounter + " players in chest counter");
		log.info("inChestStates for " + players);
		log.info(shopsFile.shops.size() + " opened shops");
		log.info(marketFile.prices.size() + " market prices");
	}

	//----------------------------------------------------------------------- pluginInfosPlayerPrices
	public void pluginInfosPlayerPrices(Player player)
	{
		if (RealPricesFile.playerHasPricesFile(this, player.getName())) {
			String log = "";
			HashMap<String, RealPrice> prices = RealPricesFile
				.playerPricesFile(this, player.getName(), marketFile)
				.prices; 
			for (String typeIdDamage : prices.keySet()) {
				RealPrice price = prices.get(typeIdDamage);
				if (log.length() > 0) {
					log += RealColor.message + ", ";
				}
				log += RealColor.item + dataValuesFile.getName(typeIdDamage)
				+ RealColor.message + ": "
				+ RealColor.price + price.getBuy()
				+ RealColor.message + "/"
				+ RealColor.price + price.getSell();
			}
			player.sendMessage(RealColor.message + log);
		} else {
			player.sendMessage(RealColor.cancel + lang.tr("You did not set any price"));
		}
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
		for (String id : dataValuesFile.getIds()) {
			RealPrice price = marketFile.getPrice(id, null);
			if (price != null) {
				log.info(
						"- " + id + " (" + dataValuesFile.getName(id) + ") :"
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
		dailyLog.toLog(log);
	}

	//---------------------------------------------------------------------------------------- reload
	public void reload(Player player)
	{
		player.sendMessage(
			RealColor.message
			+ lang.tr("Reload RealShop configuration files")
		);
		realEconomy.config.load();
		config.load();
		realEconomy.config.language = config.language;
		realEconomy.accountsFile.load();
		dataValuesFile.load();
		marketFile.load();
		shopsFile.load();
		lang = new RealTranslationFile(this, config.language);
		lang.load();
		player.sendMessage(
			RealColor.message + "accounts, config, dataValues, economy, lang, market, shops"
		);
	}

	//------------------------------------------------------------------------------------ selectShop
	public void selectChest(Player player, Block block, boolean silent)
	{
		String playerName = player.getName();
		RealShop shop = shopsFile.shopAt(block);
		lastSelectedChest.put(playerName, RealBlock.strId(block));
		if (!silent && (shop != null) && (player.isOp() || playerName.equals(shop.player))) {
			player.sendMessage(
				RealColor.message 
				+ lang.tr("You selected +owner's shop +name")
				.replace("+client", RealColor.player + playerName + RealColor.message)
				.replace("+name", RealColor.shop + shop.name + RealColor.message)
				.replace("+owner", RealColor.player + shop.player + RealColor.message)
				.replace("  ", " ")
			);
		}
	}

	//------------------------------------------------------------------------------------ shopAddBuy
	public void shopAddBuy(Player player, Block block, String command, boolean silent)
	{
		RealShop shop = shopsFile.shopAt(block);
		shopAddExclBuySell(player, shop.buyOnly, command, "buy", silent, true);
	}

	//---------------------------------------------------------------------------- shopAddExclBuySell
	private void shopAddExclBuySell(
		Player player, HashMap<String, Boolean> addTo, String command, String what,
		boolean silent, boolean all
	) {
		command += "+";
		int index = 0;
		boolean plus = true;
		String strTypeId = "";
		while (index < command.length()) {
			char c = command.charAt(index);
			if ((c == '+') || (c == '-')) {
				if (!strTypeId.equals("")) {
					try {
						String typeIdDamage = strTypeId;
						if (plus) {
							addTo.put(typeIdDamage, true);
						} else {
							addTo.remove(typeIdDamage);
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
			} else if (((c >= '0') && (c <= '9')) || (c == ':')) {
				strTypeId += c;
			}
			index ++;
		}
		shopsFile.save();
		if (!silent) {
			String strAddTo = RealShop.hashMapToCsv(addTo).replace(",", ", ");
			if (strAddTo.length() == 0) {
				strAddTo = "(" + lang.tr(all ? "all" : "none") + ")";
			}
			player.sendMessage(
				RealColor.message
				+ lang.tr("Now clients can " + what + " +items")
				.replace("+items", RealColor.item + strAddTo + RealColor.message)
			);
		}
	}

	//----------------------------------------------------------------------------------- shopAddSell
	public void shopAddSell(Player player, Block block, String command, boolean silent)
	{
		RealShop shop = shopsFile.shopAt(block);
		shopAddExclBuySell(player, shop.sellOnly, command, "sell", silent, true);
	}

	//----------------------------------------------------------------------------------- shopExclBuy
	public void shopExclBuy(Player player, Block block, String command, boolean silent)
	{
		RealShop shop = shopsFile.shopAt(block);
		shopAddExclBuySell(player, shop.buyExclude, command, "not buy", silent, false);
	}

	//---------------------------------------------------------------------------------- shopExclSell
	public void shopExclSell(Player player, Block block, String command, boolean silent)
	{
		RealShop shop = shopsFile.shopAt(block);
		shopAddExclBuySell(player, shop.sellExclude, command, "not sell", silent, false);
	}

	//-------------------------------------------------------------------------------------- shopGive
	public void shopGive(Player player, Block block, String toPlayer, boolean silent)
	{
		RealShop shop = shopsFile.shopAt(block);
		shop.player = toPlayer;
		shopsFile.save();
		if (!silent) player.sendMessage(
			RealColor.message
			+ lang.tr("The shop +name was given to +client")
			.replace("+client", RealColor.player + toPlayer + RealColor.message)
			.replace("+name", RealColor.shop + shop.name + RealColor.message)
			.replace("+owner", RealColor.player + player.getName() + RealColor.message)
			.replace("  ", " ")
		);
	}

	//-------------------------------------------------------------------------------------- shopInfo
	public void shopInfo(Player player, Block block)
	{
		RealShop shop = shopsFile.shopAt(block);
		Block neighbor = RealChest.scanForNeighborChest(block); 
		player.sendMessage(
			RealColor.message
			+ lang.tr("+owner's shop +name : +opened")
			.replace("+owner", RealColor.player + shop.player + RealColor.message)
			.replace("+name", RealColor.shop + shop.name + RealColor.message)
			.replace("+opened", lang.tr(shop.opened ? "opened" : "closed"))
			.replace("  ", " ")
		);
		player.sendMessage(RealColor.text + RealBlock.strId(block));
		if (neighbor != null) player.sendMessage(RealColor.text + RealBlock.strId(neighbor)); 
		shopAddBuy(player, block, "", false);
		shopAddSell(player, block, "", false);
		shopExclBuy(player, block, "", false);
		shopExclSell(player, block, "", false);
		player.sendMessage(
			RealColor.message
			+(shop.getFlag("infiniteBuy", config.shopInfiniteBuy.equals("true")) ? "+" : "-") + lang.tr("infinite buy") + " "
			+ (shop.getFlag("infiniteSell", config.shopInfiniteSell.equals("true")) ? "+" : "-") + lang.tr("infinite sell") + " "
		);
		player.sendMessage(
			RealColor.message
			+ (shop.getFlag("marketItemsOnly", config.shopMarketItemsOnly.equals("true")) ? "+" : "-") + lang.tr("market items only") + " "
			+ (shop.getFlag("damagedItems", config.shopDamagedItems.equals("true")) ? "+" : "-") + lang.tr("accepts damaged items")
		);
	}

	//------------------------------------------------------------------------------- shopPricesInfos
	public void shopPricesInfos(Player player, Block block)
	{
		RealShop shop = shopsFile.shopAt(block);
		String list;
		RealPricesFile pricesFile = RealPricesFile.playerPricesFile(
			this, player.getName(), marketFile
		);
		// sell (may be a very long list)
		list = "";
		Iterator<String> sellIterator = shop.sellOnly.keySet().iterator();
		if (!sellIterator.hasNext()) {
			sellIterator = dataValuesFile.getIdsIterator();
		}
		int count = 20;
		while (sellIterator.hasNext()) {
			String typeIdDamage = sellIterator.next();
			RealPrice price = pricesFile.getPrice(typeIdDamage, marketFile);
			if (price == null) {
				price = marketFile.getPrice(typeIdDamage, null);
			}
			if ((price != null) && shop.isItemSellAllowed(typeIdDamage)) {
				if (!list.equals("")) {
					list += RealColor.message + ", ";
				}
				list += RealColor.item + dataValuesFile.getName(typeIdDamage)
					+ RealColor.message + ": " + RealColor.price + price.sell;
			}
			if (count-- == 0) {
				list += ", ...";
				break;
			}
		}
		if (list.equals("")) {
			player.sendMessage(RealColor.cancel + lang.tr("Nothing can be sold here"));
		} else {
			player.sendMessage(
				RealColor.message
				+ lang.tr("You can sell +items")
				.replace("+items", RealColor.item + list + RealColor.message)
			);
		}
		// buy (may be as long as the number of filled slots!) 
		list = "";
		RealItemStackHashMap itemStack = RealItemStackHashMap.create().storeInventory(
			RealInventory.create(RealChest.create(block)), false
		); 
		Iterator<RealItemStack> buyIterator = itemStack.getContents().iterator();
		count = 20;
		while (buyIterator.hasNext()) {
			RealItemStack item = buyIterator.next();
			String typeIdDamage = item.getTypeIdDamage();
			RealPrice price = pricesFile.getPrice(typeIdDamage, marketFile);
			if (price == null) {
				price = marketFile.getPrice(typeIdDamage, null);
			}
			if ((price != null) && shop.isItemBuyAllowed(typeIdDamage)) {
				if (!list.equals("")) {
					list += RealColor.message + ", ";
				}
				list += RealColor.item + dataValuesFile.getName(typeIdDamage)
					+ RealColor.message + ": " + RealColor.price + price.buy;
			}
			if (count-- == 0) {
				list += ", ...";
				break;
			}
		}
		if (list.equals("")) {
			player.sendMessage(RealColor.cancel + lang.tr("Nothing to buy here"));
		} else {
			player.sendMessage(
				RealColor.message
				+ lang.tr("You can buy +items")
				.replace("+items", RealColor.item + list + RealColor.message)
			);
		}
	}

}
