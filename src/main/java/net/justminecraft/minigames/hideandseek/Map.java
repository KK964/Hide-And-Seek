package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Map {
    public void placeSchematic(WorldBuffer w, Location l, String key, HideAndSeekGame g) {
        try {
            File schem = new File(HideAndSeek.SCHEM_FOLDER, key);
            if(schem.isFile()) {
                HashMap<Material, ArrayList<Location>> search = w.placeSchematic(l, schem, Material.SPONGE);
                ArrayList<Location> sponges = search.get(Material.SPONGE);
                if(sponges.size() < g.players.size()) throw new IOException("the map \"" + key + "\" does not have enough sponges. The schematic needs at least the amount set in the config.");
                for(Location sponge : sponges) {
                    w.setBlockAt ((int) sponge.getX(), (int) sponge.getY(), (int) sponge.getZ(), newBlock(sponge, w));
                    g.spawnLocations.add(sponge.clone().add(0.5, 1, 0.5));
                    w.getBlockAt(sponge);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int newBlock(Location location, WorldBuffer w) {
        int block = 1;
        ArrayList<Integer> blocks = new ArrayList<>();
        blocks.add(w.getBlockAt(location.clone().add(1,0,0)));
        blocks.add(w.getBlockAt(location.clone().add(1,0,1)));
        blocks.add(w.getBlockAt(location.clone().add(-1,0,0)));
        blocks.add(w.getBlockAt(location.clone().add(0,0,-1)));
        block = blocks.get((int) (Math.random() * blocks.size()));
        return block;
    }
}
