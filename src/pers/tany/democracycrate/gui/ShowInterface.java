package pers.tany.democracycrate.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pers.tany.democracycrate.Main;
import pers.tany.democracycrate.utils.CrateUtil;
import pers.tany.democracycrate.utils.ItemUtil;
import pers.tany.yukinoaapi.interfacepart.builder.IItemBuilder;
import pers.tany.yukinoaapi.interfacepart.item.IItem;
import pers.tany.yukinoaapi.interfacepart.other.IRandom;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.realizationpart.builder.ItemBuilder;
import pers.tany.yukinoaapi.realizationpart.item.GlassPaneUtil;

import java.util.List;

public class ShowInterface implements InventoryHolder, Listener {
    private final String serial;
    private final Inventory inventory;
    private final String crateName;
    private final String ownerName;
    private final Player player;
    private int page;
    private boolean hasLast;
    private boolean hasNext;

    public ShowInterface(String crateName, Player player, int page) {

        this.crateName = crateName;
        this.ownerName = CrateUtil.getOwner(crateName);
        this.player = player;
        this.page = page;
        this.inventory = Bukkit.createInventory(this, 54, IString.color(Main.message.getString("Title").replace("[crateName]", crateName).replace("[owner]", ownerName).replace("[money]", CrateUtil.getMoney(crateName) + "")));
        this.serial = IRandom.createRandomString(8);

        Bukkit.getPluginManager().registerEvents(this, Main.plugin);

        update(1);
    }

    private void update(int page) {
        inventory.clear();
        List<String> itemList = CrateUtil.getItemList(crateName);

        IItemBuilder frame = GlassPaneUtil.getStainedGlass(15);
        IItemBuilder last = GlassPaneUtil.getStainedGlass(11);
        IItemBuilder next = GlassPaneUtil.getStainedGlass(1);

        frame.setDisplayName(Main.message.getString("HelpName"));
        last.setDisplayName(Main.message.getString("LastName"));
        next.setDisplayName(Main.message.getString("NextName"));

        frame.addLoreAll(Main.message.getStringList("HelpLore"));
        last.addLoreAll(Main.message.getStringList("LastLore"));
        next.addLoreAll(Main.message.getStringList("NextLore"));

        if (page > 1) {
            inventory.setItem(45, last.getItemStack());
            hasLast = true;
        } else {
            inventory.setItem(45, frame.getItemStack());
            hasLast = false;
        }
        for (int i = 46; i <= 52; i++) {
            inventory.setItem(i, frame.getItemStack());
        }
        if (itemList.size() > 45 + (page - 1) * 45) {
            inventory.setItem(53, next.getItemStack());
            hasNext = true;
        } else {
            inventory.setItem(53, frame.getItemStack());
            hasNext = false;
        }

        int index = (page - 1) * 45;
        int location = 0;
        int size = itemList.size() - 1;
        while (index <= size && index <= 44 + (page - 1) * 45) {
            String itemInfo = itemList.get(index);
            ItemStack itemStack = ItemUtil.getItemStack(itemInfo.split(":")[0]);
            String probability = itemInfo.split(":")[1];
            IItemBuilder iItemBuilder = new ItemBuilder(itemStack);
            iItemBuilder.addLore(Main.message.getString("ShowLore").replace("[probability]", probability + ""));

            itemStack = iItemBuilder.getItemStack();
            inventory.setItem(location++, itemStack);
            index++;
        }
    }


    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getWhoClicked() instanceof Player && evt.getWhoClicked().equals(player)) {
            int rawSlot = evt.getRawSlot();
            if (rawSlot != -999) {
                if (evt.getInventory().getHolder() instanceof ShowInterface) {
                    evt.setCancelled(true);
                    if (evt.getClickedInventory().getHolder() instanceof ShowInterface) {
                        if (!IItem.isEmpty(evt.getCurrentItem())) {
                            if (rawSlot == 45 && hasLast) {
                                update(--page);
                            } else if (rawSlot == 53 && hasNext) {
                                update(++page);
                            }
                        }
                    }
                }
            }
        }
    }

    public String getSerial() {
        return serial;
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent evt) {
        if (evt.getInventory().getHolder() instanceof ShowInterface && evt.getPlayer() instanceof Player) {
            ShowInterface showInterface = (ShowInterface) evt.getInventory().getHolder();
            if (evt.getPlayer().equals(player) && showInterface.getSerial().equals(serial)) {
                HandlerList.unregisterAll(this);
            }
        }
    }
}
