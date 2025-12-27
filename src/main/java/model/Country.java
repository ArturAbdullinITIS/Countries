package model;
import java.util.List;

public class Country {
    private String name;
    private String officialName;
    private String capital;
    private long population;
    private double area;
    private List<String> languages;
    private List<String> currencies;
    private List<String> timezones;
    private String flagUrl;
    private String region;
    private List<String> borders;
    private String cca3;

    // геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOfficialName() { return officialName; }
    public void setOfficialName(String officialName) { this.officialName = officialName; }

    public String getCapital() { return capital; }
    public void setCapital(String capital) { this.capital = capital; }

    public long getPopulation() { return population; }
    public void setPopulation(long population) { this.population = population; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public List<String> getCurrencies() { return currencies; }
    public void setCurrencies(List<String> currencies) { this.currencies = currencies; }

    public List<String> getTimezones() { return timezones; }
    public void setTimezones(List<String> timezones) { this.timezones = timezones; }

    public String getFlagUrl() { return flagUrl; }
    public void setFlagUrl(String flagUrl) { this.flagUrl = flagUrl; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public List<String> getBorders() { return borders; }
    public void setBorders(List<String> borders) { this.borders = borders; }

    public String getCca3() { return cca3; }
    public void setCca3(String cca3) { this.cca3 = cca3; }
}