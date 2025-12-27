package servlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import model.Country;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.CountryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet(name = "MainServlet", urlPatterns = {"", "/", "/search", "/autocomplete", "/countries"})
public class MainServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(MainServlet.class);
    private CountryService countryService;
    private Configuration freemarkerConfig;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        countryService = new CountryService();
        objectMapper = new ObjectMapper();

        // Настройка FreeMarker
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        freemarkerConfig.setServletContextForTemplateLoading(
                getServletContext(), "/WEB-INF/templates");
        freemarkerConfig.setDefaultEncoding("UTF-8");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        long startTime = System.currentTimeMillis();

        try {
            if ("autocomplete".equals(action)) {
                handleAutocomplete(req, resp);
            } else if ("countries".equals(action)) {
                handleGetCountries(req, resp);
            } else if ("country".equals(action)) {
                handleGetCountry(req, resp);
            } else if ("regions".equals(action)) {
                handleGetRegions(req, resp);
            } else {
                displayMainPage(req, resp);
            }
        } catch (Exception e) {
            logger.error("Ошибка обработки запроса", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Ошибка: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        logger.info("Время обработки: {} мс", (endTime - startTime));
    }

    private void displayMainPage(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String region = req.getParameter("region");
        List<Country> countries;

        if (region != null && !region.isEmpty()) {
            countries = countryService.getCountriesByRegion(region);
        } else {
            countries = countryService.getAllCountries();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("countries", countries);
        data.put("regions", Arrays.asList("Africa", "Americas", "Asia", "Europe", "Oceania"));
        data.put("selectedRegion", region);

        renderTemplate("index.ftl", data, resp);
    }

    private void handleGetCountry(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String name = req.getParameter("name");
        String code = req.getParameter("code");

        Country country = null;
        if (name != null && !name.isEmpty()) {
            country = countryService.getCountryByName(name);
            logger.info("Поиск страны по имени: {}", name);
        } else if (code != null && !code.isEmpty()) {
            country = countryService.getCountryByCode(code);
            logger.info("Поиск страны по коду: {}", code);
        }

        if (country != null) {
            if (country.getBorders() != null && !country.getBorders().isEmpty()) {
                List<Country> borderCountries = new ArrayList<>();
                for (String borderCode : country.getBorders()) {
                    try {
                        Country borderCountry = countryService.getCountryByCode(borderCode);
                        if (borderCountry != null) {
                            borderCountries.add(borderCountry);
                        }
                    } catch (Exception e) {
                        logger.warn("Не удалось загрузить граничную страну: {}", borderCode);
                    }
                }
                country.setBorders(List.of(borderCountries.stream()
                        .map(Country::getName)
                        .toArray(String[]::new)));
            }

            logger.info("Найдена страна: {}", country.getName());

            Map<String, Object> data = new HashMap<>();
            data.put("country", country);
            data.put("timestamp", new Date());

            if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(resp.getWriter(), country);
            } else {
                renderTemplate("country.ftl", data, resp);
            }
        } else {
            logger.warn("Страна не найдена: name={}, code={}", name, code);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Страна не найдена");
        }
    }

    private void handleGetCountries(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String region = req.getParameter("region");
        List<Country> countries;

        if (region != null && !region.isEmpty()) {
            countries = countryService.getCountriesByRegion(region);
            logger.info("Получение стран для региона: {}", region);
        } else {
            countries = countryService.getAllCountries();
            logger.info("Получение всех стран");
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> response = new HashMap<>();
        response.put("countries", countries.stream()
                .map(Country::getName)
                .sorted()
                .toArray());

        objectMapper.writeValue(resp.getWriter(), response);
    }

    private void handleAutocomplete(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        String query = req.getParameter("query");
        String region = req.getParameter("region");

        if (query == null || query.length() < 2) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("[]");
            return;
        }

        List<Country> countries;
        if (region != null && !region.isEmpty()) {
            countries = countryService.getCountriesByRegion(region);
        } else {
            countries = countryService.getAllCountries();
        }

        List<String> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Country country : countries) {
            if (country.getName().toLowerCase().contains(lowerQuery)) {
                results.add(country.getName());
                if (results.size() >= 10) {
                    break;
                }
            }
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), results);
    }

    private void handleGetRegions(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {

        List<String> regions = Arrays.asList("Africa", "Americas", "Asia", "Europe", "Oceania");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resp.getWriter(), regions);
    }

    private void renderTemplate(String templateName, Map<String, Object> data,
                                HttpServletResponse resp) throws IOException, TemplateException {

        resp.setContentType("text/html;charset=UTF-8");
        Template template = freemarkerConfig.getTemplate(templateName);
        template.process(data, resp.getWriter());
    }
}