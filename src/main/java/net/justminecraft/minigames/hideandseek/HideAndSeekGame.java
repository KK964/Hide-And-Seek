package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HideAndSeekGame extends Game {
    private final HideAndSeek hideAndSeek;

    Scoreboard scoreboard;

    ArrayList<Location> spawnLocations = new ArrayList<>();
    ArrayList<Player> hunters = new ArrayList<>();

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
}
