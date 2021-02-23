package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HideAndSeekGame extends Game implements Runnable {
    private final HideAndSeek hideAndSeek;

    Scoreboard scoreboard;

    int timeLeft;

    ArrayList<Location> spawnLocations = new ArrayList<>();
    ArrayList<Player> hunters = new ArrayList<>();
    HashMap<Player, Material> playerBlocks = new HashMap<>();
    HashMap<Player, Boolean> solidBlock = new HashMap<>();
    HashMap<Player, Integer> solidTime = new HashMap<>();
    HashMap<Player, Entity> falling = new HashMap<>();

    public HideAndSeekGame(Minigame mg) {
        super(mg, false);
        hideAndSeek = (HideAndSeek) mg;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public String getRandomMap() {
        try {
            ArrayList<String> maps = new ArrayList<>();
            for(File file : HideAndSeek.SCHEM_FOLDER.listFiles()) {
                if(file.isFile())
                    maps.add(file.getName());
            }
            if(maps.size() == 0) new IOException("Schematic File is missing, please add maps.");
            return maps.get((int) Math.random() * maps.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isGameOver() {
        if(players.size() - hunters.size() > 0 && timeLeft > 0) return false;
        return true;
    }

    @Override
    public String getWinningTeamName() {
        if(players.size() - hunters.size() <= 0) return "Hunters";
        if(timeLeft <= 0) return "Hiders";
        return "Unknown";
    }

    private void resetGame() {
        for(Player p : players)
            p.setWalkSpeed(0.2f);
        finishGame();
    }

    public void updateScore() {
        scoreboard.resetScores(ChatColor.GREEN + "Alive: " + (players.size() - hunters.size() +1));
        scoreboard.resetScores(ChatColor.RED + "Hunters: " + (hunters.size() - 1));
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.GREEN + "Alive: " + (players.size() - hunters.size())).setScore(6);
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.RED + "Hunters: " + hunters.size()).setScore(5);
        if(isGameOver()) {
            resetGame();
        }
    }

    @Override
    public void run() {
        scoreboard.resetScores(ChatColor.LIGHT_PURPLE + "Time: " + timeLeft);
        timeLeft--;
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.LIGHT_PURPLE + "Time: " + timeLeft).setScore(2);
        if(isGameOver()) {
            resetGame();
        } else
            HideAndSeek.getPlugin().getServer().getScheduler().runTaskLater(HideAndSeek.getPlugin(), this, 20);
    }
}
