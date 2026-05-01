package it.leaderboardPlugin;

import it.leaderboardPlugin.commands.LbCommand;
import it.leaderboardPlugin.listeners.StatsListener;
import it.leaderboardPlugin.managers.CitizensManager;
import it.leaderboardPlugin.managers.DataManager;
import it.leaderboardPlugin.managers.HologramManager;
import it.leaderboardPlugin.managers.LeaderboardManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LeaderboardPlugin extends JavaPlugin {

    private static LeaderboardPlugin instance;
    private DataManager dataManager;
    private HologramManager hologramManager;
    private LeaderboardManager leaderboardManager;
    private CitizensManager citizensManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.dataManager        = new DataManager(this);
        this.hologramManager    = new HologramManager(this);
        this.citizensManager    = new CitizensManager(this);
        this.leaderboardManager = new LeaderboardManager(this);

        getServer().getScheduler().runTask(this, () -> {
            hologramManager.removeOrphanedHolograms();
            leaderboardManager.loadAll();
        });

        getCommand("lb").setExecutor(new LbCommand(this));
        getServer().getPluginManager().registerEvents(new StatsListener(this), this);

        long intervalTicks = getConfig().getLong("update-interval-minutes", 60) * 60 * 20;
        getServer().getScheduler().runTaskTimer(this, () -> {
            leaderboardManager.updateAll();
            getLogger().info("Classifiche aggiornate automaticamente.");
        }, intervalTicks, intervalTicks);

        if (citizensManager.isCitizensEnabled()) {
            getLogger().info("Citizens trovato! NPC skin abilitati.");
        } else {
            getLogger().info("Citizens non trovato. NPC skin disabilitati.");
        }

        getLogger().info("LeaderboardPlugin abilitato!");
    }

    @Override
    public void onDisable() {
        citizensManager.removeAll();
        dataManager.saveAll();
        hologramManager.removeAll();
        getLogger().info("LeaderboardPlugin disabilitato.");
    }

    public static LeaderboardPlugin getInstance()     { return instance; }
    public DataManager getDataManager()               { return dataManager; }
    public HologramManager getHologramManager()       { return hologramManager; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public CitizensManager getCitizensManager()       { return citizensManager; }
}
