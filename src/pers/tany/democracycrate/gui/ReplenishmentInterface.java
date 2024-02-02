package pers.tany.democracycrate.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pers.tany.democracycrate.Main;
import pers.tany.democracycrate.utils.CrateUtil;
import pers.tany.democracycrate.utils.ItemUtil;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IRandom;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.player.IPlayer;

import java.util.List;

public class ReplenishmentInterface implements InventoryHolder, Listener {
    private final String serial;
    private final Inventory inventory;
    private final String crateName;
    private final Player player;

    public ReplenishmentInterface(String crateName, Player player) {

        this.crateName = crateName;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, IString.color(Main.message.getString("ReplenishmentTitle").replace("[crateName]", crateName)));
        this.serial = IRandom.createRandomString(8);

        Bukkit.getPluginManager().registerEvents(this, Main.plugin);
    }


    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String getSerial() {
        return serial;
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent evt) {
        if (evt.getInventory().getHolder() instanceof ReplenishmentInterface && evt.getPlayer() instanceof Player) {
            ReplenishmentInterface replenishmentInterface = (ReplenishmentInterface) evt.getInventory().getHolder();
            if (evt.getPlayer().equals(player) && replenishmentInterface.getSerial().equals(serial)) {
                HandlerList.unregisterAll(this);
                for (int i = 0; i < 54; i++) {
                    ItemStack addItemStack = inventory.getItem(i);
                    if (!IItem.isEmpty(addItemStack)) {
                        boolean success = false;
                        List<String> itemList = CrateUtil.getItemList(crateName);
                        String itemName = IItem.getName(addItemStack);
                        for (int index = 0; index < itemList.size(); index++) {
                            String itemInfo = itemList.get(index);
                            ItemStack itemStack = ItemUtil.getItemStack(itemInfo.split(":")[0]);
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
                                    inventory.setItem(i, null);
                                }
                                String probability = itemInfo.split(":")[1];
                                itemList.set(index, ItemUtil.getItemData(itemStack) + ":" + probability);
                                break;
                            }
                        }
                        CrateUtil.setItemList(crateName, itemList);
                        if (success) {
                            player.sendMessage(IString.color(Main.message.getString("ReplenishmentSuccess").replace("[crateName]", crateName).replace("[item]", itemName)));
                        } else {
                            player.sendMessage(IString.color(Main.message.getString("ReplenishmentFail").replace("[crateName]", crateName).replace("[item]", itemName)));
                        }
                    }
                }
                for (int i = 0; i < 54; i++) {
                    ItemStack addItemStack = inventory.getItem(i);
                    if (!IItem.isEmpty(addItemStack)) {
                        IPlayer.giveItem(player, addItemStack);
                    }
                }
            }
        }
    }
}
