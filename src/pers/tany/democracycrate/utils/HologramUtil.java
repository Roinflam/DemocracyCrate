package pers.tany.democracycrate.utils;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import pers.tany.democracycrate.Main;
import pers.tany.yukinoaapi.interfacepart.other.IString;
import pers.tany.yukinoaapi.interfacepart.serializer.ISerializer;

public class HologramUtil {

    public static void addHologram(Location location) {
        String crateName = CrateUtil.getLocationCrate(location);
        if (crateName != null) {
            Location newLocation = location.clone();
            newLocation.setX(newLocation.getX() + 0.5);
            newLocation.setY(newLocation.getY() + 2.25);
            newLocation.setZ(newLocation.getZ() + 0.5);

            Hologram hologram = HologramsAPI.createHologram(Main.plugin, newLocation);
            for (String text : Main.message.getStringList("HolographicDisplays")) {
                hologram.appendTextLine(IString.color(text.replace("[crateName]", crateName).replace("[owner]", CrateUtil.getOwner(crateName)).replace("[money]", CrateUtil.getMoney(crateName) + "").replace("[number]",CrateUtil.getGuaranteed(crateName)+"")));
            }
            Main.holographicList.put(ISerializer.serializerLocation(location), hologram);
        }
    }

    public static void delHologram(Location location) {
        String crateLocation = ISerializer.serializerLocation(location);
        Main.holographicList.get(crateLocation).delete();
        Main.holographicList.remove(crateLocation);
    }

    public static void initHologram() {
        for (String crateLocation : Main.data.getConfigurationSection("Location").getKeys(false)) {
            addHologram(ISerializer.deserializerLocation(crateLocation));
        }
    }

    public static void update() {
        for (Hologram hologram : Main.holographicList.values()) {
            hologram.delete();
        }
        HologramUtil.initHologram();
    }

}
