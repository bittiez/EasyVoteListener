package US.bittiez.EasyVoteListener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
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
import java.util.logging.Logger;

/**
 * Created by tadtaylor on 5/16/16.
 */
public class main extends JavaPlugin implements Listener{
    private FileConfiguration dueConfig;
    private String dueConfigYml = "playersDue.yml";
    private static Logger log = Logger.getLogger("EasyVoteListener");
    private boolean debug = false;

    @Override
    public void onEnable(){
        log = getLogger();
        loadSignData();

        getServer().getPluginManager().registerEvents(this, this);
    }


    @EventHandler(priority= EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        Player who = getServer().getPlayerExact(vote.getUsername());
        log.info(vote.getUsername() + " voted for us!");
        if(who != null){
            checkPlayer(who);
        } else {
            if(dueConfig.contains(vote.getUsername())){
                int t = dueConfig.getInt(vote.getUsername());
                t++;
                dueConfig.set(vote.getUsername(), t);
            } else
                dueConfig.set(vote.getUsername(), 1);
            saveDueConfig();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if(dueConfig.contains(p.getName())){
            int c = dueConfig.getInt(p.getName());
            for (int i = 0; i < c; i++) {
                checkPlayer(p);
            }
            dueConfig.set(p.getName(), null);
            saveDueConfig();
        }
    }

    private void checkPlayer(Player who){
        if(debug)
            log.info("Checking " + who.getName());

        if(who.hasPermission("EVL.inmate")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cr givekey " + who.getName() + " inmateKey 1");
        }
        if(who.hasPermission("EVL.player")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cr givekey " + who.getName() + " playerKey 1");
        }
    }


    private void saveDueConfig(){
        try {
            dueConfig.save(new File(this.getDataFolder(), dueConfigYml));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadSignData(){
        File dueFile = new File(this.getDataFolder(), dueConfigYml);

        if(!this.getDataFolder().exists())
            this.getDataFolder().mkdirs();

        if(!dueFile.exists()) {
            try {
                dueFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(dueFile.exists())
            dueConfig = YamlConfiguration.loadConfiguration(dueFile);
        else
            getServer().getPluginManager().disablePlugin(this);
    }
}
