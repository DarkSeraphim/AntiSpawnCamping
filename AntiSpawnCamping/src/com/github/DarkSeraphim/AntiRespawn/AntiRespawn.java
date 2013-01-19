package com.github.DarkSeraphim.AntiRespawn;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.events.DisallowedPVPEvent;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author DarkSeraphim
 */
public class AntiRespawn extends JavaPlugin implements Listener
{
    
    public Logger log;
    
    HashSet<String> spawned;
    
    WorldGuardPlugin wgp;
    
    @Override
    public void onEnable()
    {
        log = getLogger();
        spawned = new HashSet<String>();
        Bukkit.getPluginManager().registerEvents(this, this);
        wgp = (WorldGuardPlugin)Bukkit.getPluginManager().getPlugin("WorldGuard");
    }
    
    @Override
    public void onDisable()
    {
        spawned.clear();
    }
    
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e)
    {
        spawned.add(e.getPlayer().getName());
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        if(!e.getPlayer().hasPlayedBefore())
        {
            spawned.add(e.getPlayer().getName());
        }
    }
    
    @EventHandler
    public void onDenyPvP(DisallowedPVPEvent e)
    {
        Player def = e.getDefender();
        if(!spawned.contains(def.getName()))
        {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e)
    {
        if(!spawned.contains(e.getPlayer().getName()))
        {
            return;
        }
        Location l = e.getTo();
        for(Iterator<ProtectedRegion> regIter = wgp.getRegionManager(l.getWorld()).getApplicableRegions(l).iterator(); regIter.hasNext(); )
        {
            ProtectedRegion prot = regIter.next();
            if(prot == null)
            {
                continue;
            }
            boolean pvp = prot.getFlag(DefaultFlag.PVP) == State.ALLOW;
            if(pvp)
            {
                spawned.remove(e.getPlayer().getName());
            }
        }
    }
}
