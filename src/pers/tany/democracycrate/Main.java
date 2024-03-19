package pers.tany.democracycrate;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import pers.tany.democracycrate.command.Commands;
import pers.tany.democracycrate.listenevent.Events;
import pers.tany.democracycrate.utils.HologramUtil;
import pers.tany.yukinoaapi.interfacepart.configuration.IConfig;
import pers.tany.yukinoaapi.interfacepart.register.IRegister;
import pers.tany.yukinoaapi.realizationpart.VaultUtil;

import java.util.HashMap;


public class Main extends JavaPlugin {
    public static Plugin plugin;
    public static YamlConfiguration config;
    public static YamlConfiguration data;
    public static YamlConfiguration message;
    public static YamlConfiguration log;
    public static Economy economy;

    public static HashMap<String, Hologram> holographicList = new HashMap<String, Hologram>();

    @Override
    public void onDisable() {
        for (Hologram hologram : holographicList.values()) {
            hologram.delete();
        }
        IConfig.saveConfig(Main.plugin, Main.data, "", "data");
        Bukkit.getConsoleSender().sendMessage("§7「§fDemocracyCrate§7」§c已卸载");
    }

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getConsoleSender().sendMessage("§7「§fDemocracyCrate§7」§a已启用");

        IConfig.createResource(this, "", "config.yml", false);
        IConfig.createResource(this, "", "data.yml", false);
        IConfig.createResource(this, "", "message.yml", false);
        IConfig.createResource(this, "", "log.yml", false);

        config = IConfig.loadConfig(this, "", "config");
        data = IConfig.loadConfig(this, "", "data");
        message = IConfig.loadConfig(this, "", "message");
        log = IConfig.loadConfig(this, "", "log");

        IRegister.registerEvents(this, new Events());
        IRegister.registerCommands(this, "DemocracyCrate", new Commands());

        economy = VaultUtil.getEconomy();

        HologramUtil.initHologram();

        new BukkitRunnable() {

            @Override
            public void run() {
                IConfig.saveConfig(Main.plugin, Main.data, "", "data");
            }

        }.runTaskTimer(Main.plugin, 1200, 1200);

        new BukkitRunnable() {

            @Override
            public void run() {
                HologramUtil.update();
            }

        }.runTaskTimer(Main.plugin, 2400, 6000);
    }
}
