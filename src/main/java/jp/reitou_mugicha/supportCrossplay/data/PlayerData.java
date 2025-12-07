package jp.reitou_mugicha.supportCrossplay.data;

import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class PlayerData
{
    public static File playerInfoFile = new File(SupportCrossplay.Instance.getDataFolder(), "player_data.yml");
    public static FileConfiguration config = YamlConfiguration.loadConfiguration(playerInfoFile);

    public static void saveConfig()
    {
        try {
            config.save(playerInfoFile);
        } catch (Exception e) {
            SupportCrossplay.getInstance().getLogger().warning(e.getMessage());
        }
    }
}
