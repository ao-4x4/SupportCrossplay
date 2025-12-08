package jp.reitou_mugicha.supportCrossplay;

import jp.reitou_mugicha.supportCrossplay.block.BlockCompressor;
import jp.reitou_mugicha.supportCrossplay.block.BlockHomeBlock;
import jp.reitou_mugicha.supportCrossplay.command.CommandCurrentPosition;
import jp.reitou_mugicha.supportCrossplay.command.CommandSCItems;
import jp.reitou_mugicha.supportCrossplay.command.CommandSCSpawn;
//import jp.reitou_mugicha.supportCrossplay.command.CommandShop;
import jp.reitou_mugicha.supportCrossplay.command.CommandSupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.feature.*;
//import jp.reitou_mugicha.supportCrossplay.feature.FeatureShop;
import jp.reitou_mugicha.supportCrossplay.item.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SupportCrossplay extends JavaPlugin
{
    public static SupportCrossplay Instance;
    private static final List<ItemStack> customItems =  new ArrayList<ItemStack>();

    public static final String KEY = "SupportCrossplay";
    public static Random random = new Random();

    public SupportCrossplay()
    {
        Instance = this;
    }

    @Override
    public void onEnable()
    {
        getLogger().info("Support Crossplay has been enabled!");
        initPlugin();
    }

    @Override
    public void onDisable()
    {
        getLogger().info("SupportCrossplay has been disabled.");
    }

    private void registerEvent(Listener listener)
    {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void initCustomItems()
    {
        // Items
        registerEvent(new ItemBedrockDestroyer());
        registerEvent(new ItemGPSTracker());
        registerEvent(new ItemReturnStick());
        registerEvent(new ItemCraftingTableOnAStick());
        registerEvent(new ItemAnvilOnAStick());
        registerEvent(new ItemStoneCutterOnAStick());
        registerEvent(new ItemSmithingTableOnAStick());
        registerEvent(new ItemEnchantShard());
        registerEvent(new ItemStarterPack());
        registerEvent(new ItemShikattoStick());
        registerEvent(new ItemVillagerMeat());

        // Blocks
        registerEvent(new BlockHomeBlock());
        registerEvent(new BlockCompressor());
    }

    private void initFeatures()
    {
        // Feature Register
        registerEvent(new FeatureAdvancedAnvil());
        registerEvent(new FeatureBulkTrading());
        registerEvent(new FeatureExperienceTrading());
        registerEvent(new FeatureReapJobs());
        registerEvent(new FeatureDisableEmerald());
        registerEvent(new FeaturePiglinBarter());
        registerEvent(new FeatureRandomRollEnchant());
        registerEvent(new FeatureFirstLoginBonus());
        registerEvent(new FeatureRepairAnvil());
    }

    private void initCommands()
    {
        // Command Register
        this.getCommand("scspawn").setExecutor(new CommandSCSpawn());
        this.getCommand("scspawn").setTabCompleter(new CommandSCSpawn());
        this.getCommand("currentposition").setExecutor(new CommandCurrentPosition());
        this.getCommand("scitems").setExecutor(new CommandSCItems());
        this.getCommand("supportcrossplay").setExecutor(new CommandSupportCrossplay());
        this.getCommand("supportcrossplay").setTabCompleter(new CommandSupportCrossplay());

        // GUI Register
        registerEvent(new CommandSCItems());
    }

    private void initPlugin()
    {
        initCustomItems();
        initFeatures();
        initCommands();
    }

    public static SupportCrossplay getInstance()
    {
        return Instance;
    }

    public static void addItem(ItemStack item)
    {
        customItems.add(item);
    }

    public static List<ItemStack> getCustomItems()
    {
        return customItems;
    }
}
