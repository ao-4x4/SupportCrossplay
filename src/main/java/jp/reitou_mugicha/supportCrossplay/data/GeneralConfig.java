package jp.reitou_mugicha.supportCrossplay.data;

import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class GeneralConfig
{
    public static File generalFile = new File(SupportCrossplay.Instance.getDataFolder(), "general_config.yml");
    public static FileConfiguration config = YamlConfiguration.loadConfiguration(generalFile);

    public static void saveConfig()
    {
        try {
            config.save(generalFile);
        } catch (Exception e) {
            SupportCrossplay.getInstance().getLogger().warning(e.getMessage());
        }
    }
}