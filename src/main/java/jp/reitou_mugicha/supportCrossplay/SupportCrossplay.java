package jp.reitou_mugicha.supportCrossplay;

import jp.reitou_mugicha.supportCrossplay.block.BlockCompressor;
import jp.reitou_mugicha.supportCrossplay.block.BlockHomeBlock;
import jp.reitou_mugicha.supportCrossplay.command.CommandCurrentPosition;
import jp.reitou_mugicha.supportCrossplay.command.CommandSCItems;
import jp.reitou_mugicha.supportCrossplay.command.CommandSCSpawn;
import jp.reitou_mugicha.supportCrossplay.command.CommandSupportCrossplay;
import jp.reitou_mugicha.supportCrossplay.enchantment.EnchantmentPoisonAspect;
import jp.reitou_mugicha.supportCrossplay.feature.*;
import jp.reitou_mugicha.supportCrossplay.item.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SupportCrossplay extends JavaPlugin
{
    public static SupportCrossplay Instance;
    public static Economy economy;
    public static Random random = new Random();

    private static final List<ItemStack> customItems =  new ArrayList<ItemStack>();
    public static final String KEY = "SupportCrossplay";

    public SupportCrossplay()
    {
        Instance = this;
        new DatapackInstaller(this).installForAllWorlds();
    }

    @Override
    public void onEnable()
    {
        initPlugin();
        getLogger().info("Support Crossplay has been enabled!");
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

    private boolean initEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
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

    private void initEnchantment()
    {
        registerEvent(new EnchantmentPoisonAspect());
    }

    private void initFeatures()
    {
        // Feature Register
        registerEvent(new FeatureAdvancedAnvil());
        registerEvent(new FeatureBulkTrading());
        registerEvent(new FeatureExperienceTrading());
        registerEvent(new FeatureReapJobs());
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
        if (!initEconomy())
        {
            disablePlugin();
            return;
        }

        initEconomy();
        initCustomItems();
        initFeatures();
        initCommands();
    }

    private void disablePlugin()
    {
        getServer().getPluginManager().disablePlugin(this);
        getLogger().warning("Support Crossplay has been disabled due to there is no vault plugin!");
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
