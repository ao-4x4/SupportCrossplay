package jp.reitou_mugicha.supportCrossplay.data;

import jp.reitou_mugicha.supportCrossplay.SupportCrossplay;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OfflineStorageData
{
    public static File file = new File(SupportCrossplay.Instance.getDataFolder(), "offline_storage_data.yml");
    public static FileConfiguration config = YamlConfiguration.loadConfiguration(file);

    public static void saveConfig()
    {
        try {
            config.save(file);
        } catch (Exception e) {
            SupportCrossplay.getInstance().getLogger().warning(e.getMessage());
        }
    }

    public static void addItem(String uuid, ItemStack item)
    {
        List<ItemStack> items = getStoredItems(uuid);
        items.add(item);
        config.set(uuid, items);
        saveConfig();
    }

    public static List<ItemStack> getStoredItems(String uuid)
    {
        List<ItemStack> itemList = new ArrayList<>();
        List<?> rawList = config.getList(uuid);
        if (rawList != null) {
            for (Object obj : rawList) {
                if (obj instanceof ItemStack) {
                    itemList.add((ItemStack) obj);
                }
            }
        }
        return itemList;
    }

    public static void clearStoredItems(String uuid)
    {
        config.set(uuid, null);
        saveConfig();
    }
}