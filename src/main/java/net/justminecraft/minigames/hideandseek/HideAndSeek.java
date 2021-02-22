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
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
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

        //scoreboards

        Team hunterObj = g.scoreboard.registerNewTeam("hunter");
        Team hiderObj = g.scoreboard.registerNewTeam("hider");
        Objective aliveObj = g.scoreboard.registerNewObjective("alive", "dummy");

        aliveObj.setDisplayName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "HIDING");
        aliveObj.setDisplaySlot(DisplaySlot.SIDEBAR);

        aliveObj.getScore(" ").setScore(5);
        aliveObj.getScore(ChatColor.GREEN + "Alive: " + (g.players.size() - 1)).setScore(3);
        aliveObj.getScore(ChatColor.RED + "Hunters: " + 1).setScore(3);
        aliveObj.getScore(" ").setScore(2);
        aliveObj.getScore(ChatColor.YELLOW + "justminecraft.net").setScore(1);

        hunterObj.setAllowFriendlyFire(false);
        hunterObj.addEntry("hunter" + ChatColor.DARK_RED);

        hiderObj.setAllowFriendlyFire(false);
        hiderObj.addEntry("hider" + ChatColor.GREEN);

        hunterObj.addEntry(hunter.getName());

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
            for(Player p : g.players)
                if(p != hunter)
                    TitleAPI.sendTitle(p, 0, 30, 20, ChatColor.DARK_RED + "The Hunter Has Been Released!", "");
            }, waitTime * 20);
    }

    @Override
    public void generateWorld(Game game, WorldBuffer w) {
        HideAndSeekGame g = (HideAndSeekGame) game;

        String map = g.getRandomMap();

        g.disablePvP = true;
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
