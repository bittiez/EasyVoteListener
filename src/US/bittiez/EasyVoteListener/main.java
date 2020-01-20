package US.bittiez.EasyVoteListener;

import US.bittiez.EasyVoteListener.Config.Configurator;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

public class main extends JavaPlugin implements Listener {
    private FileConfiguration dueConfig;
    private String dueConfigYml = "playersDue.yml";
    private static Logger log = Logger.getLogger("EasyVoteListener");
    private static boolean debug = false;
    private Configurator configurator = new Configurator();

    @Override
    public void onEnable() {
        log = getLogger();
        loadDueData();

        configurator.setConfig(this);
        configurator.saveDefaultConfig(this);

        getServer().getPluginManager().registerEvents(this, this);
        if (debug == false)
            debug = configurator.config.getBoolean("debug", false);
    }


    public boolean onCommand(CommandSender who, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("EVL")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("test") && who instanceof Player && who.hasPermission("EVL.test")) {
                who.sendMessage(colorize("&9[EVL] &3Simulating a vote for &b" + who.getName()));
                rewardPlayer((Player) who);
                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("reload") && who instanceof Player && who.hasPermission("EVL.reload")) {
                configurator.reloadPluginDefaultConfig(this);
                debug = configurator.config.getBoolean("debug", false);
                who.sendMessage(colorize("&9[EVL] &3The config has been reloaded!"));
                return true;
            }
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        Player who = getServer().getPlayerExact(vote.getUsername());
        log.info(vote.getUsername() + " voted for us!");

        if (who != null) { //The player is online
            rewardPlayer(who);
        } else { //The player is offline
            if (dueConfig.contains(vote.getUsername())) { //The player already has a vote pending, add another
                int t = dueConfig.getInt(vote.getUsername());
                t++;
                dueConfig.set(vote.getUsername(), t);
            } else //The player does not have any votes pending, save the first one
                dueConfig.set(vote.getUsername(), 1);
            saveDueConfig();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (dueConfig.contains(p.getName())) {
            int c = dueConfig.getInt(p.getName());
            for (int i = 0; i < c; i++) {
                rewardPlayer(p);
            }
            dueConfig.set(p.getName(), null);
            saveDueConfig();
        }
    }

    private void rewardPlayer(Player who) {
        if (debug)
            log.info("Checking " + who.getName());
        Set<String> permSet = configurator.config.getConfigurationSection("permissions").getKeys(false);
        for (String perm : permSet) { //Loop through all permissions
            if (debug)
                who.sendMessage(colorize("&9[EVL] &3Testing for permission: " + perm));
            if (who.hasPermission(perm)) {
                if (debug)
                    who.sendMessage(colorize("&9[EVL] &3You have the permission: " + perm));
                for (String cmd : configurator.config.getStringList("permissions." + perm)) { //Loop through all commands for this permission
                    cmd = cmd.replace("[PLAYER]", who.getName());
                    if (debug) {
                        who.sendMessage(colorize("&9[EVL] &3This command would have run if not in debug mode:"));
                        who.sendMessage(colorize("&9[EVL] &3" + cmd));
                    } else { //Only run commands when debug mode is disabled
                        getServer().dispatchCommand(Bukkit.getConsoleSender(), colorize(cmd));
                    }
                }
            }
        }
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void saveDueConfig() {
        try {
            dueConfig.save(new File(this.getDataFolder(), dueConfigYml));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDueData() {
        File dueFile = new File(this.getDataFolder(), dueConfigYml);

        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdirs();

        if (!dueFile.exists()) {
            try {
                dueFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (dueFile.exists())
            dueConfig = YamlConfiguration.loadConfiguration(dueFile);
        else
            getServer().getPluginManager().disablePlugin(this);
    }
}
