package pers.tany.democracycrate.command;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import pers.tany.democracycrate.Main;
import pers.tany.democracycrate.gui.AddInterface;
import pers.tany.democracycrate.gui.GuaranteedAddInterface;
import pers.tany.democracycrate.gui.ReplenishmentInterface;
import pers.tany.democracycrate.utils.CrateUtil;
import pers.tany.democracycrate.utils.HologramUtil;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.interfacepart.inventory.IInventory;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.player.IPlayer;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Commands implements CommandExecutor {
    public static HashMap<String, String> bindMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.isOp()) {
                    sender.sendMessage("§c你没有权限使用此指令！");
                    return true;
                }
                Main.config = YamlConfiguration.loadConfiguration(new File(Main.plugin.getDataFolder(), "config.yml"));
                Main.data = YamlConfiguration.loadConfiguration(new File(Main.plugin.getDataFolder(), "data.yml"));
                Main.message = YamlConfiguration.loadConfiguration(new File(Main.plugin.getDataFolder(), "message.yml"));
                Main.log = YamlConfiguration.loadConfiguration(new File(Main.plugin.getDataFolder(), "log.yml"));
                HologramUtil.update();
                sender.sendMessage("§a重载成功");
                return true;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    sender.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
//                if (!CrateUtil.getOwner(crateName).equals(sender.getName()) && !sender.isOp()) {
//                    sender.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
//                    return true;
//                }
                List<String> resultLog = Main.log.getStringList("ResultLog." + crateName);
                if (resultLog.size() <= 0) {
                    sender.sendMessage(IString.color(Main.message.getString("NotFoundLog")));
                    return true;
                }
                for (int i = resultLog.size() - 1; i >= 0; i--) {
                    sender.sendMessage("§f" + resultLog.get(i));
                }
                return true;
            }
        }
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("§a/dc check 抽奖箱名  §2查看指定抽奖箱的抽奖情况");
            sender.sendMessage("§a/dc reload  §2重载配置文件");
            return true;
        }
        Player player = (Player) sender;
        String name = player.getName();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("show")) {
                boolean hasCrate = false;
                for (String crateName : Main.data.getConfigurationSection("Crate").getKeys(false)) {
                    if (Main.data.getString("Crate." + crateName + ".Owner").equals(name)) {
                        player.sendMessage(IString.color(Main.message.getString("ShowCrate").replace("[crateName]", crateName)));
                        hasCrate = true;
                    }
                }
                if (!hasCrate) {
                    player.sendMessage(IString.color(Main.message.getString("NoCrate")));
                }
                player.sendMessage(IString.color(Main.message.getString("TotalMoneyCrate").replace("[money]", Main.data.getInt("TotalMoney." + name) + "")));
                if (Main.data.getConfigurationSection("Guaranteed." + player.getName()) != null) {
                    for (String crateName : Main.data.getConfigurationSection("Guaranteed." + player.getName()).getKeys(false)) {
                        int guaranteed = CrateUtil.getGuaranteed(crateName);
                        int number = CrateUtil.getLotteryNumber(crateName, player.getName());
                        String owner = CrateUtil.getOwner(crateName);
                        player.sendMessage(IString.color(Main.message.getString("LotteryNumber").replace("[owner]", owner).replace("[number]", number + "").replace("[crateName]", crateName).replace("[guaranteed]", guaranteed + "").replace("[surplus]", guaranteed - number + "")));
                    }
                }
                return true;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("gadd")) {
                if (IItem.isEmptyHand(player)) {
                    player.sendMessage(IString.color(Main.message.getString("EmptyHand")));
                    return true;
                }
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                ItemStack itemStack = player.getItemInHand();
                if (Main.config.getStringList("BlackType").contains(itemStack.getType().toString())) {
                    player.sendMessage(IString.color(Main.message.getString("NoAdd")));
                    return true;
                }
                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
                    for (String lore : itemStack.getItemMeta().getLore()) {
                        for (String l : Main.config.getStringList("BlackLore")) {
                            if (lore.contains(l)) {
                                player.sendMessage(IString.color(Main.message.getString("NoAdd")));
                                return true;
                            }
                        }
                    }
                }
                CrateUtil.addGuaranteedItem(crateName, ISerializer.serializerItemStack(itemStack));
                player.setItemInHand(null);
                player.sendMessage(IString.color(Main.message.getString("SettingSuccess")));
                return true;
            }
            if (args[0].equalsIgnoreCase("gadds")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                IInventory.openInventory(new GuaranteedAddInterface(crateName, player), player);
                return true;
            }
            if (args[0].equalsIgnoreCase("create")) {
                int maxCreateNumber = Main.config.getInt("MaxCreateNumber");
                for (PermissionAttachmentInfo permissionAttachmentInfo : player.getEffectivePermissions()) {
                    String permission = permissionAttachmentInfo.getPermission();
                    if (permission.startsWith("dc.maxcreate.")) {
                        try {
                            maxCreateNumber = Math.max(Integer.parseInt(permission.split("\\.")[2]), maxCreateNumber);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (CrateUtil.getPlayerCrateNumber(name) >= maxCreateNumber) {
                    player.sendMessage(IString.color(Main.message.getString("CreateLimit").replace("[max]", maxCreateNumber + "")));
                    return true;
                }
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("CreateFail").replace("[crateName]", crateName)));
                    return true;
                }
                CrateUtil.createCrate(crateName, name);
                player.sendMessage(IString.color(Main.message.getString("CreateSuccess").replace("[crateName]", crateName)));
                return true;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                for (String itemInfo : CrateUtil.getItemList(crateName)) {
                    try {
                        ItemStack itemStack = ISerializer.deserializeItemStack(itemInfo.split(":")[0]);
                        IPlayer.giveItem(player, itemStack);
                    } catch (Exception e) {
                        ItemStack itemStack = ISerializer.deserializeItemStack(itemInfo);
                        IPlayer.giveItem(player, itemStack);
                    }
                }
                for (String itemInfo : CrateUtil.getGuaranteedItemList(crateName)) {
                    ItemStack itemStack = ISerializer.deserializeItemStack(itemInfo);
                    IPlayer.giveItem(player, itemStack);
                }
                CrateUtil.removeCrate(crateName);
                for (String location : new ArrayList<>(Main.data.getConfigurationSection("Location").getKeys(false))) {
                    if (Main.data.getString("Location." + location).equals(crateName)) {
                        Main.data.set("Location." + location, null);
                    }
                }
                IConfig.saveConfig(Main.plugin, Main.data, "", "data");
                player.sendMessage(IString.color(Main.message.getString("SettingSuccess")));
                HologramUtil.update();
                return true;
            }
            if (args[0].equalsIgnoreCase("bind")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                if (bindMap.containsKey(name)) {
                    player.sendMessage(IString.color(Main.message.getString("Binding")));
                    return true;
                }
                bindMap.put(name, crateName);
                player.sendMessage(IString.color(Main.message.getString("Binding")));
                return true;
            }
            if (args[0].equalsIgnoreCase("enable")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                CrateUtil.setEnable(crateName, !CrateUtil.isEnable(crateName));
                player.sendMessage(IString.color(Main.message.getString("Enable").replace("[crateName]", crateName).replace("[enable]", CrateUtil.isEnable(crateName) ? "已启用" : "未启用")));
                return true;
            }
            if (args[0].equalsIgnoreCase("sg")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                if (IItem.isEmptyHand(player)) {
                    player.sendMessage(IString.color(Main.message.getString("EmptyHand")));
                    return true;
                }
                boolean success = false;
                ItemStack addItemStack = player.getItemInHand();
                List<String> itemList = CrateUtil.getItemList(crateName);
                for (int index = 0; index < itemList.size(); index++) {
                    String itemInfo = itemList.get(index);
                    ItemStack itemStack = ISerializer.deserializeItemStack(itemInfo.split(":")[0]);
                    if (itemStack.isSimilar(addItemStack)) {
                        if (itemStack.getAmount() >= itemStack.getMaxStackSize()) {
                            continue;
                        }
                        success = true;
                        if (itemStack.getAmount() + addItemStack.getAmount() > itemStack.getMaxStackSize()) {
                            addItemStack.setAmount(itemStack.getAmount() + addItemStack.getAmount() - itemStack.getMaxStackSize());
                            itemStack.setAmount(itemStack.getMaxStackSize());
                        } else {
                            itemStack.setAmount(itemStack.getAmount() + addItemStack.getAmount());
                            player.setItemInHand(null);
                        }
                        String probability = itemInfo.split(":")[1];
                        itemList.set(index, ISerializer.serializerItemStack(itemStack) + ":" + probability);
                        break;
                    }
                }
                CrateUtil.setItemList(crateName, itemList);
                if (success) {
                    player.sendMessage(IString.color(Main.message.getString("SupplementaryGoodsSuccess")));
                } else {
                    player.sendMessage(IString.color(Main.message.getString("SupplementaryGoodsFail")));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("sgs")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                IInventory.openInventory(new ReplenishmentInterface(crateName, player), player);
                return true;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("g")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                int number = 0;
                try {
                    number = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(IString.color(Main.message.getString("IntgerFormatError")));
                    return true;
                }
                CrateUtil.setGuaranteed(crateName, number);
                player.sendMessage(IString.color(Main.message.getString("SettingSuccess")));
                HologramUtil.update();
                return true;
            }
            if (args[0].equalsIgnoreCase("money")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                int money = 0;
                try {
                    money = Integer.parseInt(args[2]);
                    if (money < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(IString.color(Main.message.getString("IntgerFormatError")));
                    return true;
                }
                CrateUtil.setMoney(crateName, money);
                player.sendMessage(IString.color(Main.message.getString("SettingSuccess")));
                HologramUtil.update();
                return true;
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (IItem.isEmptyHand(player)) {
                    player.sendMessage(IString.color(Main.message.getString("EmptyHand")));
                    return true;
                }
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                if (!args[2].contains("%")) {
                    player.sendMessage(IString.color(Main.message.getString("DoubleFormatError")));
                    return true;
                }
                double probability = 0;
                try {
                    probability = Double.parseDouble(args[2].replace("%", ""));
                    if (probability <= 0 || probability > 100) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException | NullPointerException e) {
                    player.sendMessage(IString.color(Main.message.getString("DoubleFormatError")));
                    return true;
                }
                if (CrateUtil.getItemList(crateName).size() >= Main.config.getInt("MaxSize")) {
                    player.sendMessage(IString.color(Main.message.getString("MaxSize").replace("[max]", Main.config.getInt("MaxSize") + "")));
                    return true;
                }
                ItemStack itemStack = player.getItemInHand();
                if (Main.config.getStringList("BlackType").contains(itemStack.getType().toString())) {
                    player.sendMessage(IString.color(Main.message.getString("NoAdd")));
                    return true;
                }
                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
                    for (String lore : itemStack.getItemMeta().getLore()) {
                        for (String l : Main.config.getStringList("BlackLore")) {
                            if (lore.contains(l)) {
                                player.sendMessage(IString.color(Main.message.getString("NoAdd")));
                                return true;
                            }
                        }
                    }
                }
                CrateUtil.addItem(crateName, ISerializer.serializerItemStack(itemStack) + ":" + args[2]);
                player.setItemInHand(null);
                player.sendMessage(IString.color(Main.message.getString("SettingSuccess")));
                return true;
            }
            if (args[0].equalsIgnoreCase("adds")) {
                String crateName = CrateUtil.getFormattedName(args[1]);
                if (!CrateUtil.hasCrate(crateName)) {
                    player.sendMessage(IString.color(Main.message.getString("NotFoundCrate").replace("[crateName]", crateName)));
                    return true;
                }
                if (!CrateUtil.getOwner(crateName).equals(name) && !player.isOp()) {
                    player.sendMessage(IString.color(Main.message.getString("NotTheOwner")));
                    return true;
                }
                if (!args[2].contains("%")) {
                    player.sendMessage(IString.color(Main.message.getString("DoubleFormatError")));
                    return true;
                }
                double probability = 0;
                try {
                    probability = Double.parseDouble(args[2].replace("%", ""));
                    if (probability <= 0 || probability > 100) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException | NullPointerException e) {
                    player.sendMessage(IString.color(Main.message.getString("DoubleFormatError")));
                    return true;
                }
                IInventory.openInventory(new AddInterface(crateName, player, args[2]), player);
                return true;
            }
        }
        if (sender.isOp()) {
            sender.sendMessage("§a/dc show  §2查看自己所有抽奖箱和收益情况");
            sender.sendMessage("§a/dc check 抽奖箱名  §2查看指定抽奖箱的抽奖情况");
            sender.sendMessage("§a/dc create 抽奖箱名  §2创建一个新的抽奖箱");
            sender.sendMessage("§a/dc bind 抽奖箱名  §2绑定方块为抽奖箱");
            sender.sendMessage("§a/dc enable 抽奖箱名  §2启动和关闭抽奖箱");
            sender.sendMessage("§a/dc money 抽奖箱名 游戏币  §2设置这个抽奖箱所需要的游戏币");
            sender.sendMessage("§a/dc gadd 抽奖箱名  §2把这个物品添加到抽奖箱的保底里");
            sender.sendMessage("§a/dc gadds 抽奖箱名  §2打开批量添加抽奖保底奖励界面");
            sender.sendMessage("§a/dc add 抽奖箱名 概率  §2把这个物品添加到抽奖箱里，概率为10%就填10%");
            sender.sendMessage("§a/dc adds 抽奖箱名 概率  §2打开批量添加抽奖奖励界面");
            sender.sendMessage("§a/dc g 抽奖箱名 保底次数  §2设置这个抽奖箱保底次数");
            sender.sendMessage("§a/dc sg 抽奖箱名  §2补充手上的货物到抽奖箱");
            sender.sendMessage("§a/dc sgs 抽奖箱名  §2打开批量补充货物界面");
            sender.sendMessage("§a/dc remove 抽奖箱  §2删除抽奖箱，里面的物品都会回到背包");
            sender.sendMessage("§a/dc reload  §2重载配置文件");
        } else {
            sender.sendMessage("§a/dc show  §2查看自己所有抽奖箱和收益情况");
            sender.sendMessage("§a/dc check 抽奖箱名  §2查看指定抽奖箱的抽奖情况");
            sender.sendMessage("§a/dc create 抽奖箱名  §2创建一个新的抽奖箱");
            sender.sendMessage("§a/dc bind 抽奖箱名  §2绑定方块为抽奖箱");
            sender.sendMessage("§a/dc enable 抽奖箱名  §2启动和关闭抽奖箱");
            sender.sendMessage("§a/dc money 抽奖箱名 游戏币  §2设置这个抽奖箱所需要的游戏币");
            sender.sendMessage("§a/dc gadd 抽奖箱名  §2把这个物品添加到抽奖箱的保底里");
            sender.sendMessage("§a/dc gadds 抽奖箱名  §2打开批量添加抽奖保底奖励界面");
            sender.sendMessage("§a/dc add 抽奖箱名 概率  §2把这个物品添加到抽奖箱里，概率为10%就填10%");
            sender.sendMessage("§a/dc adds 抽奖箱名 概率  §2打开批量添加抽奖奖励界面");
            sender.sendMessage("§a/dc g 抽奖箱名 保底次数  §2设置这个抽奖箱保底次数");
            sender.sendMessage("§a/dc sg 抽奖箱名  §2补充手上的货物到抽奖箱");
            sender.sendMessage("§a/dc sgs 抽奖箱名  §2打开批量补充货物界面");
            sender.sendMessage("§a/dc remove 抽奖箱  §2删除抽奖箱，里面的物品都会回到背包");
        }
        return true;
    }
}
