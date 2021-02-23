package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.minigamecore.Minigame;
import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import net.justminecraft.minigames.titleapi.TitleAPI;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.ArrayList;

public class HideAndSeek extends Minigame implements Listener {

    private static HideAndSeek hideAndSeek;

    public static File DATA_FOLDER;
    public static File SCHEM_FOLDER;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        hideAndSeek = this;
        DATA_FOLDER = getDataFolder();
        SCHEM_FOLDER = new File (DATA_FOLDER.getPath() + System.getProperty("file.separator") + "schematics");
        SCHEM_FOLDER.mkdir();
        MG.core().registerMinigame(this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new MovementEvent(), this);
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
            if(p != hunter) {
                for(Player p2 : g.players)
                    p2.hidePlayer(p);
                hiderObj.addEntry(p.getName());
                new SetPlayerBlock(p);
                new SetFallingBlocks(p);
                new PlayerSolidBlock(p);
            }
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
        if(e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Game g = MG.core().getGame(damager);
            if(g == null || g.minigame != this) return;
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void playerHitEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Game g = MG.core().getGame(p);
        if(g == null || g.minigame != this) return;
        HideAndSeekGame game = (HideAndSeekGame) g;
        Location loc = p.getLocation();
        int range = 8;
        double yAdd = 1.62;
        if(p.isSneaking())
            yAdd = 1.50; //sneaking
        loc = loc.add(loc.getDirection().getX(),loc.getDirection().getY()+yAdd,loc.getDirection().getZ());
        for(int i = 0; i < range; i++) { //total distance travel
            loc = loc.add(loc.getDirection().getX()/1.5, loc.getDirection().getY()/1.5, loc.getDirection().getZ()/1.5);
            if(loc.getBlock() != null && loc.getBlock().getType().isSolid()) {
                break;
            }
            for(Entity ent : loc.getWorld().getNearbyEntities(loc, 0.2, 0.2, 0.2)) {
                if(ent != p && ent.getType().isAlive()) {
                    if(ent instanceof Player) {
                        Player player = (Player) ent;
                        if(game.hunters.contains(p) && !game.hunters.contains(player)) {
                            game.scoreboard.getTeam("hider").removeEntry(player.getName());
                            game.scoreboard.getTeam("hunter").addEntry(player.getName());
                            game.hunters.add(player);
                            game.updateScore();
                            for(Player p1 : game.players) {
                                p1.showPlayer(player);
                                p1.sendMessage(ChatColor.RED + p.getName() + " was caught by " + player.getName());
                            }
                        }
                        i = range;
                        break;
                    }
                }
            }
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
