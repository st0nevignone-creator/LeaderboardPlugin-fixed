package it.leaderboardPlugin.managers;

import it.leaderboardPlugin.LeaderboardPlugin;
import it.leaderboardPlugin.models.LeaderboardData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.*;

public class CitizensManager {

    private final LeaderboardPlugin plugin;
    private final Map<String, Integer> npcMap = new HashMap<>();

    public CitizensManager(LeaderboardPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isCitizensEnabled() {
        return Bukkit.getPluginManager().getPlugin("Citizens") != null
                && Bukkit.getPluginManager().getPlugin("Citizens").isEnabled();
    }

    public void spawnOrUpdateNPC(LeaderboardData lb, UUID firstUUID) {
        if (!isCitizensEnabled()) return;
        if (lb.getLocation() == null) return;

        removeNPC(lb.getId());

        String playerName = Bukkit.getOfflinePlayer(firstUUID).getName();
        if (playerName == null) return;

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, "§6#1 §e" + playerName);

        Location npcLoc = lb.getLocation().clone();
        npc.spawn(npcLoc);
        npc.setProtected(true);
        npc.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, true);
        npc.data().set(NPC.Metadata.AMBIENT_SOUND_CHANCE, 0);

        // Applica skin tramite reflection per evitare problemi di package
        try {
            Class<?> skinTraitClass = Class.forName("net.citizensnpcs.api.trait.trait.SkinTrait");
            Object skinTrait = npc.getClass().getMethod("getOrAddTrait", Class.class)
                    .invoke(npc, skinTraitClass);
            skinTraitClass.getMethod("setSkinName", String.class, boolean.class)
                    .invoke(skinTrait, playerName, false);
        } catch (Exception e) {
            plugin.getLogger().warning("Skin non applicabile: " + e.getMessage());
        }

        npcMap.put(lb.getId(), npc.getId());
    }

    public void removeNPC(String lbId) {
        if (!isCitizensEnabled()) return;
        Integer npcId = npcMap.remove(lbId);
        if (npcId == null) return;
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.getById(npcId);
        if (npc != null) npc.destroy();
    }

    public void removeAll() {
        if (!isCitizensEnabled()) return;
        for (String id : new HashSet<>(npcMap.keySet())) removeNPC(id);
        npcMap.clear();
    }
}
