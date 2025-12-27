<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${country.name} - REST Countries</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 800px; margin: 0 auto; }
        .flag { max-width: 200px; }
        .back-link { margin-bottom: 20px; }
        .info-section { margin: 20px 0; }
        .border-link { margin-right: 10px; }
    </style>
</head>
<body>
<div class="container">
    <div class="back-link">
        <a href=".">← Назад к списку стран</a>
    </div>

    <h1>${country.name}</h1>

    <#if country.flagUrl??>
        <img src="${country.flagUrl}" alt="Флаг ${country.name}" class="flag">
    </#if>

    <div class="info-section">
        <p><strong>Официальное название:</strong> ${country.officialName}</p>
        <p><strong>Столица:</strong> ${country.capital!}</p>
        <p><strong>Население:</strong> ${country.population?string(',')}</p>
        <p><strong>Площадь:</strong> ${country.area?string(',')} км²</p>

        <#if country.languages?? && country.languages?size gt 0>
            <p><strong>Языки:</strong> ${country.languages?join(", ")}</p>
        </#if>

        <#if country.currencies?? && country.currencies?size gt 0>
            <p><strong>Валюты:</strong> ${country.currencies?join(", ")}</p>
        </#if>

        <#if country.timezones?? && country.timezones?size gt 0>
            <p><strong>Часовые пояса:</strong> ${country.timezones?join(", ")}</p>
        </#if>

        <#if country.borders?? && country.borders?size gt 0>
            <div class="border-countries">
                <strong>Граничит с:</strong>
                <#list country.borders as border>
                    <a href="?action=country&name=${border?url}" class="border-link">${border}</a>
                </#list>
            </div>
        </#if>
    </div>

    <p><em>Запрос обработан: ${timestamp?datetime}</em></p>
</div>
</body>
</html>