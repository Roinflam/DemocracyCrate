package pers.tany.democracycrate.listenevent;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pers.tany.democracycrate.Main;
import pers.tany.democracycrate.command.Commands;
import pers.tany.democracycrate.gui.ShowInterface;
import pers.tany.democracycrate.utils.CrateUtil;
import pers.tany.democracycrate.utils.HologramUtil;
import pers.tany.democracycrate.utils.ItemUtil;
import pers.tany.yukinoaapi.interfacepart.inventory.IInventory;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IList;
import pers.tany.yukinoaapi.interfacepart.other.IRandom;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.player.IPlayer;
import pers.tany.yukinoaapi.realizationpart.player.Ask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Events implements Listener {
    private final Set<String> draw = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent evt) {
        Player player = evt.getPlayer();
        String name = player.getName();
        Block block = evt.getBlock();
        Location location = block.getLocation();
        String crateName = CrateUtil.getLocationCrate(location);
        if (crateName != null) {
            if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                evt.setCancelled(true);
            } else if (!player.isSneaking()) {
                evt.setCancelled(true);
            } else {
                CrateUtil.delLocation(location);
                HologramUtil.delHologram(location);
                player.sendMessage(IString.color(Main.message.getString("DelBind").replace("[crateName]", crateName)));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerInteract(PlayerInteractEvent evt) {
//        if (evt.getHand() != null && evt.getHand().equals(EquipmentSlot.OFF_HAND)) {
//            return;
//        }
        Player player = evt.getPlayer();
        String name = player.getName();
        if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK) || evt.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Block block = evt.getClickedBlock();
            Location location = block.getLocation();
            if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (Commands.bindMap.containsKey(evt.getPlayer().getName())) {
                    if (evt.isCancelled()) {
                        return;
                    }
                    if (Main.config.getStringList("BindWhiteType").size() > 0) {
                        if (!Main.config.getStringList("BindWhiteType").contains(block.getType().toString())) {
                            player.sendMessage(IString.color(Main.message.getString("NoBindType")));
                            return;
                        }
                    }
                    if (Main.config.getStringList("BlackBind").contains(location.getWorld().getName())) {
                        player.sendMessage(IString.color(Main.message.getString("NoBind")));
                    } else {
                        if (CrateUtil.getBindCrateNumber(Commands.bindMap.get(name)) >= Main.config.getInt("MaxBind")) {
                            player.sendMessage(IString.color(Main.message.getString("MaxBind").replace("[number]", Main.config.getInt("MaxBind") + "")));
                        } else {
                            CrateUtil.addLocation(location, Commands.bindMap.get(name));
                            HologramUtil.addHologram(location);
                            player.sendMessage(IString.color(Main.message.getString("Bind").replace("[crateName]", Commands.bindMap.get(name))));
                        }
                    }
                    evt.setCancelled(true);
                    Commands.bindMap.remove(name);
                    return;
                }
            }
            String crateName = CrateUtil.getLocationCrate(location);
            if (crateName != null && !player.isSneaking()) {
                evt.setCancelled(true);
                if (evt.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    player.closeInventory();
                    IInventory.openInventory(new ShowInterface(crateName, player, 1), player);
                } else if (evt.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (!CrateUtil.isEnable(crateName)) {
                        player.sendMessage(IString.color(Main.message.getString("NotEnable")));
                        return;
                    }
                    if (draw.contains(player.getName())) {
                        return;
                    }
                    draw.add(player.getName());
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            player.sendMessage(IString.color(Main.message.getString("DrawNumber")));
                            while (true) {
                                Ask ask = new Ask(player, 10);
                                if (ask.getReason().equals(Ask.Reason.answer)) {
                                    String answer = ask.getAnswer();
                                    draw.remove(player.getName());
                                    try {
                                        int number = Integer.parseInt(answer);
                                        if (number <= 0) {
                                            throw new NumberFormatException();
                                        }
                                        if (number > Main.config.getInt("MaxDrawNumber")) {
                                            player.sendMessage(IString.color(Main.message.getString("MaxDrawNumber").replace("[max]", Main.config.getInt("MaxDrawNumber") + "")));
                                            return;
                                        }
                                        int itemAmount = 0;
                                        for (String itemInfo : CrateUtil.getItemList(crateName)) {
                                            ItemStack itemStack = ItemUtil.getItemStack(itemInfo.split(":")[0]);
                                            itemAmount += itemStack.getAmount();
                                        }
                                        if (itemAmount < number) {
                                            if (itemAmount == 0) {
                                                player.sendMessage(IString.color(Main.message.getString("EmptyCrate")));
                                                return;
                                            } else if (!Main.config.getBoolean("ReturnItem")) {
                                                player.sendMessage(IString.color(Main.message.getString("InsufficientItem").replace("[number]", itemAmount + "")));
                                                return;
                                            }
                                        }
                                        int spendMoney = CrateUtil.getMoney(crateName) * number;
                                        if (Main.economy.getBalance(player.getName()) < spendMoney) {
                                            player.sendMessage(IString.color(Main.message.getString("LackMoney").replace("[spend]", spendMoney + "").replace("[money]", Main.economy.getBalance(player.getName()) + "")));
                                            return;
                                        }
                                        String owner = CrateUtil.getOwner(crateName);
                                        Main.economy.withdrawPlayer(name, spendMoney);
                                        if (Bukkit.getPlayerExact(owner) != null) {
                                            if (!Bukkit.getPlayerExact(owner).hasPermission("dc.tax")) {
                                                spendMoney = (int) (spendMoney - spendMoney * Main.config.getDouble("Tax"));
                                            }
                                        }
                                        Main.economy.depositPlayer(owner, spendMoney);
                                        if (spendMoney > 0) {
                                            Main.data.set("TotalMoney." + owner, Main.data.getInt("TotalMoney." + owner) + spendMoney);
                                            if (Bukkit.getPlayerExact(owner) != null) {
                                                Bukkit.getPlayerExact(owner).sendMessage(IString.color(Main.message.getString("GiveMoney").replace("[player]", player.getName()).replace("[crateName]", crateName).replace("[number]", number + "").replace("[money]", spendMoney + "")));
                                            }
                                        }
                                        boolean win = false;
                                        String itemName = "";
                                        for (int i = 0; i < number; i++) {
                                            List<String> itemList = IList.upsetList(CrateUtil.getItemList(crateName));
                                            if (itemList.size() <= 0) {
                                                player.sendMessage(IString.color(Main.message.getString("ReturnItem").replace("[number]", (number - i) + "")));
                                                break;
                                            }
                                            for (int index = 0; index < itemList.size(); index++) {
                                                String itemInfo = itemList.get(index);
                                                ItemStack itemStack = ItemUtil.getItemStack(itemInfo.split(":")[0]);
                                                String probability = itemInfo.split(":")[1];
                                                if (IRandom.percentageChance(probability)) {
                                                    win = true;
                                                    ItemStack giveItemStack = itemStack.clone();
                                                    giveItemStack.setAmount(1);
                                                    new BukkitRunnable() {

                                                        @Override
                                                        public void run() {
                                                            IPlayer.giveItem(player, giveItemStack);
                                                        }

                                                    }.runTask(Main.plugin);
                                                    if (Double.parseDouble(probability.replace("%", "")) <= Double.parseDouble(Main.config.getString("RaryProbability").replace("%", ""))) {
                                                        Bukkit.broadcastMessage(IString.color(Main.message.getString("RareDrawItem").replace("[player]", player.getName()).replace("[owner]", owner).replace("[crate]", crateName).replace("[item]", IItem.getName(giveItemStack)).replace("[random]", probability)));
                                                    } else {
                                                        itemName += IItem.getName(giveItemStack) + " ";
                                                    }
                                                    int amount = itemStack.getAmount();
                                                    itemStack.setAmount(amount - 1);
                                                    if (amount <= 1) {
                                                        itemList.remove(index);
                                                    } else {
                                                        itemList.set(index, ItemUtil.getItemData(itemStack) + ":" + probability);
                                                    }
                                                    break;
                                                }
                                            }
                                            CrateUtil.setItemList(crateName, itemList);
                                        }
                                        if (!itemName.equals("") && win) {
                                            player.sendMessage(IString.color(Main.message.getString("DrawItem").replace("[number]", number + "").replace("[item]", itemName)));
                                        } else if (!win) {
                                            player.sendMessage(IString.color(Main.message.getString("NotWin")));
                                        }
                                        return;
                                    } catch (NumberFormatException ignored) {

                                    }
                                } else {
                                    if (ask.getReason().equals(Ask.Reason.when)) {
                                        player.sendMessage(IString.color(Main.message.getString("Timeout")));
                                    }
                                    draw.remove(player.getName());
                                    return;
                                }
                                player.sendMessage(IString.color(Main.message.getString("DrawNumber")));
                            }
                        }

                    }.runTaskAsynchronously(Main.plugin);

                }
            }
        }
    }
}
