package jp.reitou_mugicha.supportCrossplay;

import jp.reitou_mugicha.supportCrossplay.block.BlockCompressor;
import jp.reitou_mugicha.supportCrossplay.block.BlockHomeBlock;
import jp.reitou_mugicha.supportCrossplay.command.CommandCurrentPosition;
import jp.reitou_mugicha.supportCrossplay.command.CommandSCItems;
import jp.reitou_mugicha.supportCrossplay.command.CommandSupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.feature.*;
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
    public static Random random = new Random();

    private static final List<ItemStack> customItems =  new ArrayList<>();
    public static final String KEY = "SupportCrossplay";

    public SupportCrossplay()
    {
        Instance = this;
    }

    @Override
    public void onEnable()
    {
        initPlugin();
        getLogger().info("Support Crossplay has been enabled!");
        new DatapackInstaller(this).installServer();
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
        registerEvent(new ItemShikattoStick());

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
        registerEvent(new FeatureRandomRollEnchant());
        registerEvent(new FeatureRepairAnvil());
    }

    private void initCommands()
    {
        // Command Register
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
