package pers.tany.democracycrate.utils;

import com.comphenix.protocol.utility.StreamSerializer;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    public static String getItemData(ItemStack itemStack) {
        ItemStack i = itemStack.clone();
        try {
            return new StreamSerializer().serializeItemStack(i);
        } catch (Exception e) {
            return null;
        }
    }

    public static ItemStack getItemStack(String data) {
        try {
            return new StreamSerializer().deserializeItemStack(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
