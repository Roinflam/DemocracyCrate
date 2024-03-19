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
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IRandom;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;

public class GuaranteedAddInterface implements InventoryHolder, Listener {
    private final String serial;
    private final Inventory inventory;
    private final String crateName;
    private final Player player;

    public GuaranteedAddInterface(String crateName, Player player) {

        this.crateName = crateName;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, IString.color(Main.message.getString("AddsTitle").replace("[crateName]", crateName)));
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
        if (evt.getInventory().getHolder() instanceof GuaranteedAddInterface && evt.getPlayer() instanceof Player) {
            GuaranteedAddInterface replenishmentInterface = (GuaranteedAddInterface) evt.getInventory().getHolder();
            if (evt.getPlayer().equals(player) && replenishmentInterface.getSerial().equals(serial)) {
                HandlerList.unregisterAll(this);
                int number = 0;
                for (int i = 0; i < 54; i++) {
                    ItemStack addItemStack = inventory.getItem(i);
                    if (!IItem.isEmpty(addItemStack)) {
                        if (Main.config.getStringList("BlackType").contains(addItemStack.getType().toString())) {
                            player.sendMessage(IString.color(Main.message.getString("NoAdd")));
                            continue;
                        }
                        boolean noAdd = false;
                        if (addItemStack.hasItemMeta() && addItemStack.getItemMeta().hasLore()) {
                            for (String lore : addItemStack.getItemMeta().getLore()) {
                                for (String l : Main.config.getStringList("BlackLore")) {
                                    if (lore.contains(l)) {
                                        noAdd = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if(noAdd){
                            player.sendMessage(IString.color(Main.message.getString("NoAdd")));
                        }
                        CrateUtil.addGuaranteedItem(crateName, ISerializer.serializerItemStack(addItemStack));
                        number++;
                    }
                }
                player.sendMessage(IString.color(Main.message.getString("GuaranteedAddsSettingSuccess").replace("[number]", number + "")));
            }
        }
    }
}
