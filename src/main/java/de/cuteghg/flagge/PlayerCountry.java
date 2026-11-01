package de.cuteghg.flagge;

public record PlayerCountry(String country, String countryCode) {
    public static final PlayerCountry UNKNOWN = new PlayerCountry("Unbekannt", "??");
}
