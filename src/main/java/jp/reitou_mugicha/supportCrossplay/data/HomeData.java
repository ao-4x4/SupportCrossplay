package jp.reitou_mugicha.supportCrossplay.data;

import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class HomeData
{
    public static File homeFile = new File(SupportCrossplay.Instance.getDataFolder(), "home_data.yml");
    public static FileConfiguration config = YamlConfiguration.loadConfiguration(homeFile);

    public static void saveConfig()
    {
        try {
            config.save(homeFile);
        } catch (Exception e) {
            SupportCrossplay.getInstance().getLogger().warning(e.getMessage());
        }
    }
}