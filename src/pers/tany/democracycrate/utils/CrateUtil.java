package pers.tany.democracycrate.utils;

import org.bukkit.Location;
import pers.tany.democracycrate.Main;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;

import java.util.ArrayList;
import java.util.List;

public class CrateUtil {

    public static String getFormattedName(String arg) {
        return arg.replace("\\.", "。").replace("&", "§");
    }

    public static void createCrate(String crateName, String ownerName) {
        Main.data.set("Crate." + crateName + ".Enable", true);
        Main.data.set("Crate." + crateName + ".Owner", ownerName);
        Main.data.set("Crate." + crateName + ".Money", Main.config.getInt("BaseMoney"));
        Main.data.set("Crate." + crateName + ".Guaranteed", 0);
        Main.data.set("Crate." + crateName + ".Items", new ArrayList<>());
        Main.data.set("Crate." + crateName + ".GuaranteedItems", new ArrayList<>());
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }


    public static void removeCrate(String crateName) {
        Main.data.set("Crate." + crateName, null);
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }

    public static boolean hasCrate(String crateName) {
        return Main.data.getConfigurationSection("Crate." + crateName) != null;
    }

    public static void setLotteryNumber(String crateName, String name, int number) {
        Main.data.set("Guaranteed." + name + "." + crateName, number);
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }

    public static int getLotteryNumber(String crateName, String name) {
        return Main.data.getInt("Guaranteed." + name + "." + crateName);
    }

    public static String getOwner(String crateName) {
        return Main.data.getString("Crate." + crateName + ".Owner");
    }

    public static int getMoney(String crateName) {
        return Main.data.getInt("Crate." + crateName + ".Money");
    }

    public static int getGuaranteed(String crateName) {
        return Main.data.getInt("Crate." + crateName + ".Guaranteed");
    }

    public static void setMoney(String crateName, int money) {
        Main.data.set("Crate." + crateName + ".Money", money);
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }

    public static void setGuaranteed(String crateName, int number) {
        Main.data.set("Crate." + crateName + ".Guaranteed", number);
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }

    public static boolean isEnable(String crateName) {
        return Main.data.getBoolean("Crate." + crateName + ".Enable");
    }

    public static void setEnable(String crateName, boolean enable) {
        Main.data.set("Crate." + crateName + ".Enable", enable);
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }

    public static List<String> getItemList(String crateName) {
        return Main.data.getStringList("Crate." + crateName + ".Items");
    }

    public static List<String> getGuaranteedItemList(String crateName) {
        return Main.data.getStringList("Crate." + crateName + ".GuaranteedItems");
    }

    public static void setGuaranteedItemList(String crateName, List<String> list) {
        Main.data.set("Crate." + crateName + ".GuaranteedItems", list);
    }

    public static void setItemList(String crateName, List<String> list) {
        Main.data.set("Crate." + crateName + ".Items", list);
    }

    public static void addItem(String crateName, String itemInfo) {
        List<String> list = getItemList(crateName);
        list.add(itemInfo);
        Main.data.set("Crate." + crateName + ".Items", list);
    }

    public static void addGuaranteedItem(String crateName, String itemInfo) {
        List<String> list = getGuaranteedItemList(crateName);
        list.add(itemInfo);
        Main.data.set("Crate." + crateName + ".GuaranteedItems", list);
    }

    public static int getPlayerCrateNumber(String name) {
        int number = 0;
        for (String crateName : Main.data.getConfigurationSection("Crate").getKeys(false)) {
            if (getOwner(crateName).equals(name)) {
                number++;
            }
        }
        return number;
    }

    public static String getLocationCrate(Location location) {
        try {
            return Main.data.getString("Location." + ISerializer.serializerLocation(location));
        } catch (Exception e) {
            return null;
        }
    }

    public static int getBindCrateNumber(String crateName) {
        int number = 0;
        for (String location : Main.data.getConfigurationSection("Location").getKeys(false)) {
            if (Main.data.getString("Location." + location).equals(crateName)) {
                number++;
            }
        }
        return number;
    }

    public static void addLocation(Location location, String crateName) {
        Main.data.set("Location." + ISerializer.serializerLocation(location), crateName);
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }

    public static void delLocation(Location location) {
        Main.data.set("Location." + ISerializer.serializerLocation(location), null);
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
    }
}
