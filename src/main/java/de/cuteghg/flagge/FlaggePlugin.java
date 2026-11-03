package de.cuteghg.flagge;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FlaggePlugin extends JavaPlugin implements Listener, CommandExecutor {
    private final Map<UUID, CachedCountry> countries = new ConcurrentHashMap<>();
    private CountryLookup lookup;
    private long cacheMillis;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("flaggereload").setExecutor(this);
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new FlagPlaceholderExpansion(this).register();
        }
    }

    private void loadSettings() {
        reloadConfig();
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
            return;
        }

        CachedCountry cached = countries.get(player.getUniqueId());
        if (cached != null && !cached.expired(cacheMillis)) {
            return;
        }

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
        }));
    }

    private String getIp(Player player) {
        InetSocketAddress address = player.getAddress();
        return address == null || address.getAddress() == null ? null : address.getAddress().getHostAddress();
    }

    private String countryFlag(String countryCode) {
        if (countryCode == null || countryCode.length() != 2 || "??".equals(countryCode)) {
            return "\uE000";
        }

        String code = countryCode.toUpperCase();
        if (code.charAt(0) < 'A' || code.charAt(0) > 'Z'
                || code.charAt(1) < 'A' || code.charAt(1) > 'Z') {
            return "\uE000";
        }
        int index = (code.charAt(0) - 'A') * 26 + code.charAt(1) - 'A';
        return String.valueOf((char) (0xE000 + index));
    }

    String getFlag(Player player) {
        CachedCountry cached = countries.get(player.getUniqueId());
        return cached == null ? "\uE000" : countryFlag(cached.country().countryCode());
    }

    String getCountryCode(Player player) {
        CachedCountry cached = countries.get(player.getUniqueId());
        return cached == null ? "??" : cached.country().countryCode();
    }

    private static final class FlagPlaceholderExpansion extends PlaceholderExpansion {
        private final FlaggePlugin plugin;

        private FlagPlaceholderExpansion(FlaggePlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getIdentifier() {
            return "flagge";
        }

        @Override
        public String getAuthor() {
            return "cuteGHG";
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, String params) {
            if (player == null) {
                return "";
            }
            return switch (params.toLowerCase()) {
                case "flag" -> plugin.getFlag(player);
                case "country_code" -> plugin.getCountryCode(player);
                default -> null;
            };
        }
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
