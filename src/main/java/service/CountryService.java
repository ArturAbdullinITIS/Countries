package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Country;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class CountryService {
    private static final Logger logger = LogManager.getLogger(CountryService.class);
    private static final String BASE_URL = "https://restcountries.com/v3.1";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<Country> getAllCountries() throws Exception {
        return fetchCountries("/all?fields=name");
    }

    public List<Country> getCountriesByRegion(String region) throws Exception {
        return fetchCountries("/region/" + region);
    }

    public Country getCountryByName(String name) throws Exception {
        List<Country> countries = fetchCountries("/name/" + URLEncoder.encode(name, "UTF-8"));
        return countries.isEmpty() ? null : countries.get(0);
    }

    public Country getCountryByCode(String code) throws Exception {
        List<Country> countries = fetchCountries("/alpha/" + code);
        return countries.isEmpty() ? null : countries.get(0);
    }

    public List<String> searchCountries(String query) throws Exception {
        List<String> results = new ArrayList<>();
        List<Country> countries = getAllCountries();

        for (Country country : countries) {
            if (country.getName().toLowerCase().contains(query.toLowerCase())) {
                results.add(country.getName());
            }
        }

        return results.subList(0, Math.min(10, results.size()));
    }

    private List<Country> fetchCountries(String endpoint) throws Exception {
        HttpURLConnection conn = null;
        List<Country> countries = new ArrayList<>();

        try {
            URL url = new URL(BASE_URL + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonNode root = objectMapper.readTree(response.toString());

            for (JsonNode node : root) {
                Country country = new Country();

                // Обработка названий
                JsonNode nameNode = node.get("name");
                if (nameNode != null) {
                    country.setName(nameNode.get("common").asText());
                    country.setOfficialName(nameNode.get("official").asText());
                }

                country.setCapital(node.has("capital") ?
                        node.get("capital").get(0).asText() : "N/A");

                country.setPopulation(node.has("population") ?
                        node.get("population").asLong() : 0);

                country.setArea(node.has("area") ?
                        node.get("area").asDouble() : 0.0);

                // Языки
                if (node.has("languages")) {
                    List<String> languages = new ArrayList<>();
                    node.get("languages").fields().forEachRemaining(entry -> {
                        languages.add(entry.getValue().asText());
                    });
                    country.setLanguages(languages);
                }

                // Валюты
                if (node.has("currencies")) {
                    List<String> currencies = new ArrayList<>();
                    node.get("currencies").fields().forEachRemaining(entry -> {
                        currencies.add(entry.getKey() + " - " +
                                entry.getValue().get("name").asText());
                    });
                    country.setCurrencies(currencies);
                }

                // Часовые пояса
                if (node.has("timezones")) {
                    List<String> timezones = new ArrayList<>();
                    node.get("timezones").forEach(tz -> {
                        timezones.add(tz.asText());
                    });
                    country.setTimezones(timezones);
                }

                // Флаг
                if (node.has("flags")) {
                    country.setFlagUrl(node.get("flags").get("png").asText());
                }

                // Регион
                country.setRegion(node.has("region") ?
                        node.get("region").asText() : "N/A");

                // Граничащие страны
                if (node.has("borders")) {
                    List<String> borders = new ArrayList<>();
                    node.get("borders").forEach(border -> {
                        borders.add(border.asText());
                    });
                    country.setBorders(borders);
                }

                // Код страны
                country.setCca3(node.has("cca3") ?
                        node.get("cca3").asText() : "");

                countries.add(country);
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return countries;
    }
}