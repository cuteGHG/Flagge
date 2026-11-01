package de.cuteghg.flagge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FlaggePlugin extends JavaPlugin implements Listener, CommandExecutor {
    private final Map<UUID, CachedCountry> countries = new ConcurrentHashMap<>();
    private LegacyComponentSerializer serializer;
    private CountryLookup lookup;
    private long cacheMillis;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("flaggereload").setExecutor(this);
    }

    private void loadSettings() {
        reloadConfig();
        serializer = LegacyComponentSerializer.legacyAmpersand();
        lookup = new CountryLookup(
                getConfig().getString("geolocation.endpoint"),
                Math.max(500, getConfig().getLong("geolocation.timeout-ms", 3000))
        );
        cacheMillis = Duration.ofHours(Math.max(1, getConfig().getLong("geolocation.cache-hours", 24))).toMillis();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = getIp(player);
        if (ip == null) {
            applyCountry(player, PlayerCountry.UNKNOWN);
            return;
        }

        CachedCountry cached = countries.get(player.getUniqueId());
        if (cached != null && !cached.expired(cacheMillis)) {
            applyCountry(player, cached.country());
            return;
        }

        applyCountry(player, PlayerCountry.UNKNOWN);
        lookup.lookup(ip).whenComplete((country, error) -> getServer().getScheduler().runTask(this, () -> {
            if (error != null) {
                getLogger().warning("Konnte Land für " + player.getName() + " nicht ermitteln: "
                        + error.getMessage());
                if (player.isOnline()) {
                    player.sendMessage(color(getConfig().getString("messages.lookup-error",
                            "&eDein Herkunftsland konnte nicht ermittelt werden.")
                            .replace("%player%", player.getName())));
                }
                return;
            }
            countries.put(player.getUniqueId(), new CachedCountry(country, System.currentTimeMillis()));
            if (player.isOnline()) {
                applyCountry(player, country);
            }
        }));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Keep the cache for reconnects during the configured cache period.
    }

    private String getIp(Player player) {
        InetSocketAddress address = player.getAddress();
        return address == null || address.getAddress() == null ? null : address.getAddress().getHostAddress();
    }

    private void applyCountry(Player player, PlayerCountry country) {
        String namePath = country.equals(PlayerCountry.UNKNOWN) ? "unknown-format" : "name-format";
        String name = format(namePath, player, country);
        Component component = serializer.deserialize(name);

        if (getConfig().getBoolean("tablist.enabled", true)) {
            player.playerListName(component);
        }
        if (getConfig().getBoolean("display-name.enabled", true)) {
            player.displayName(component);
        }
    }

    private String format(String path, Player player, PlayerCountry country) {
        String value = getConfig().getString(path, "%player%");
        return ChatColor.translateAlternateColorCodes('&', value
                .replace("%player%", player.getName())
                .replace("%country%", country.country())
                .replace("%country_code%", country.countryCode())
                .replace("%flag%", countryFlag(country.countryCode())));
    }

    private String countryFlag(String countryCode) {
        if (countryCode == null || countryCode.length() != 2 || "??".equals(countryCode)) {
            return "🏳";
        }
        String code = countryCode.toUpperCase();
        return new String(Character.toChars(0x1F1E6 + code.charAt(0) - 'A'))
                + new String(Character.toChars(0x1F1E6 + code.charAt(1) - 'A'));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("flagge.reload")) {
            sender.sendMessage(color("&cDafür hast du keine Berechtigung."));
            return true;
        }
        loadSettings();
        sender.sendMessage(color(getConfig().getString("messages.reload", "&aNeu geladen.")));
        return true;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private record CachedCountry(PlayerCountry country, long loadedAt) {
        private boolean expired(long maxAgeMillis) {
            return System.currentTimeMillis() - loadedAt > maxAgeMillis;
        }
    }
}
