package net.justminecraft.minigames.hideandseek;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.titleapi.TitleAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FreezePlayer implements Runnable {
    private final HideAndSeek plugin;
    private final Player player;
    private int seconds;

    public FreezePlayer(Player player, int seconds) {
        this.plugin = HideAndSeek.getPlugin();
        this.player = player;
        this.seconds = seconds;
        run();
    }

    @Override
    public void run() {
        Game g = MG.core().getGame(player);
        if(g == null || g.minigame != plugin) return;
        player.setWalkSpeed(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 99999, 1), false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 99999, 128), false);
        if(seconds == 0) {
            TitleAPI.sendTitle(player, 10, seconds > 1 ? 30 : 20, 20, ChatColor.GREEN + "Begin The Hunt!", "");
            player.setWalkSpeed(0.2f);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.JUMP);
            return;
        }

        if(seconds % 5 == 0 || seconds <= 5)
            TitleAPI.sendTitle(player, seconds <= 5 ? 0 : 10, seconds > 1 ? 30 : 20, 10, ChatColor.GREEN + "Beginning in " + seconds + " second" + (seconds == 1 ? "" : "s"), "");
        seconds--;
        plugin.getServer().getScheduler().runTaskLater(plugin, this, 20);
    }
}
