package jp.reitou_mugicha.supportCrossplay.feature;

import jp.reitou_mugicha.supportCrossplay.data.PlayerData;
import jp.reitou_mugicha.supportCrossplay.item.ItemStarterPack;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FeatureFirstLoginBonus implements Listener
{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if (!PlayerData.config.getBoolean(player.getUniqueId() + ".gotFirstLoginBonus"))
        {
            player.getInventory().addItem(ItemStarterPack.getItem());
            player.sendMessage(ChatColor.GREEN + "初回ログインボーナスでスターターパックを受け取りました！");
            PlayerData.config.set(player.getUniqueId() + ".gotFirstLoginBonus", true);
        }
    }
}