package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.minigamecore.Minigame;
import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import net.justminecraft.minigames.titleapi.TitleAPI;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class HideAndSeek extends Minigame implements Listener {

    private static HideAndSeek hideAndSeek;

    public static File DATA_FOLDER;
    public static File SCHEM_FOLDER;

    @Override
    public void onEnable() {
        saveConfig();
        hideAndSeek = this;
        DATA_FOLDER = getDataFolder();
        SCHEM_FOLDER = new File (DATA_FOLDER.getPath() + System.getProperty("file.separator") + "schematics");
        SCHEM_FOLDER.mkdir();
        MG.core().registerMinigame(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(("Hide And Seek Enabled"));
    }

    @Override
    public void onDisable() {
        getLogger().info(("Hide And Seek Disabled"));
    }

    @Override
    public int getMaxPlayers() {
        return 10;
    }

    @Override
    public int getMinPlayers() {
        return 2;
    }

    @Override
    public String getMinigameName() {
        return "HideAndSeek";
    }

    // Game

    @Override
    public Game newGame() {
        return new HideAndSeekGame(this);
    }

    @Override
    public void startGame(Game game) {
        HideAndSeekGame g = (HideAndSeekGame) game;
        Player hunter = g.players.get((int) Math.random() * g.players.size());
        g.hunters.add(hunter);
        int waitTime = 30 + (g.players.size() * 10);
        new FreezePlayer(hunter, waitTime);
        g.world.setDifficulty(Difficulty.PEACEFUL);
        g.world.setSpawnLocation(0,64,0);

        g.timeLeft = waitTime * 5;

        //scoreboards

        Team hunterObj = g.scoreboard.registerNewTeam("hunter");
        Team hiderObj = g.scoreboard.registerNewTeam("hider");
        Objective aliveObj = g.scoreboard.registerNewObjective("alive", "dummy");

        aliveObj.setDisplayName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "HIDE AND SEEK");
        aliveObj.setDisplaySlot(DisplaySlot.SIDEBAR);

        aliveObj.getScore(" ").setScore(7);
        aliveObj.getScore(ChatColor.GREEN + "Alive: " + (g.players.size() - 1)).setScore(6);
        aliveObj.getScore(ChatColor.RED + "Hunters: " + 1).setScore(5);
        aliveObj.getScore(" ").setScore(4);
        aliveObj.getScore(ChatColor.LIGHT_PURPLE + "Time: " + g.timeLeft).setScore(3);
        aliveObj.getScore(" ").setScore(2);
        aliveObj.getScore(ChatColor.YELLOW + "justminecraft.net").setScore(1);

        hunterObj.setAllowFriendlyFire(false);

        hiderObj.setAllowFriendlyFire(false);

        hunterObj.addEntry(hunter.getName());

        hunterObj.setNameTagVisibility(NameTagVisibility.ALWAYS);
        hiderObj.setNameTagVisibility(NameTagVisibility.NEVER);

        //spawns
        ArrayList<Location> spawnLoc = new ArrayList<>(g.spawnLocations);

        for(Player p : g.players) {
            Location playerSpawn = spawnLoc.get((int) Math.random() * spawnLoc.size());
            playerSpawn.setWorld(g.world);
            p.teleport(playerSpawn);
            spawnLoc.remove(playerSpawn);
            p.setScoreboard(g.scoreboard);
            if(p != hunter)
                hiderObj.addEntry(p.getName());
        }
        getServer().getScheduler().runTaskLater(this, () -> {
            g.run();
            for(Player p : g.players)
                if(p != hunter)
                    TitleAPI.sendTitle(p, 0, 30, 20, ChatColor.DARK_RED + "WARNING", ChatColor.DARK_RED + "The Hunter Has Been Released!");
            }, waitTime * 20);
    }

    @EventHandler
    public void onDamageEvent(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            Player p = (Player) e.getEntity();
            Player damager = (Player) e.getDamager();
            Game g = MG.core().getGame(p);
            if(g == null || g.minigame != this) return;
            HideAndSeekGame game = (HideAndSeekGame) g;
            if(game.hunters.contains(damager) && !game.hunters.contains(p)) {
                game.scoreboard.getTeam("hider").removeEntry(p.getName());
                game.scoreboard.getTeam("hunter").addEntry(p.getName());
                game.hunters.add(p);
                game.updateScore();
                for(Player player : game.players)
                    player.sendMessage(ChatColor.RED + p.getName() + " was caught by " + damager.getName());
            }
            e.setCancelled(true);
        }
    }

    @Override
    public void generateWorld(Game game, WorldBuffer w) {
        HideAndSeekGame g = (HideAndSeekGame) game;

        String map = g.getRandomMap();

        g.disablePvP = false;
        g.moneyPerDeath = 5;
        g.moneyPerWin = 20;
        g.disableBlockBreaking = true;
        g.disableBlockPlacing = true;
        g.disableHunger = true;

        Map m = new Map();
        Location l = new Location(g.world, 0, 64, 0);
        m.placeSchematic(w,l,map,g);
    }

    public static HideAndSeek getPlugin() {
        return hideAndSeek;
    }
}
