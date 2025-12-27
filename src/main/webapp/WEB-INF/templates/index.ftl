<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>REST Countries App</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 1200px; margin: 0 auto; }
        .search-box { margin: 20px 0; }
        input, select, button { padding: 8px; margin: 5px; }
        .country-info { margin-top: 20px; border: 1px solid #ccc; padding: 20px; }
        .flag { max-width: 200px; }
        .borders { margin-top: 10px; }
        .border-link { margin-right: 10px; }
        .autocomplete-suggestions {
            border: 1px solid #ccc;
            max-height: 200px;
            overflow-y: auto;
            position: absolute;
            background: white;
            z-index: 1000;
        }
        .suggestion-item {
            padding: 8px;
            cursor: pointer;
        }
        .suggestion-item:hover {
            background-color: #f0f0f0;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>REST Countries Application</h1>

    <div class="search-box">
        <!-- Фильтр по континентам -->
        <label for="region-filter">Фильтр по континентам:</label>
        <select id="region-filter">
            <option value="">Все континенты</option>
            <#list regions as region>
                <option value="${region}"
                        <#if selectedRegion?? && selectedRegion == region>selected</#if>>
                    ${region}
                </option>
            </#list>
        </select>

        <!-- Поиск с автодополнением -->
        <label for="country-search">Поиск страны:</label>
        <input type="text" id="country-search" placeholder="Начните вводить название...">
        <div id="autocomplete-suggestions"></div>

        <!-- Выбор страны из списка -->
        <label for="country-select">Или выберите страну:</label>
        <select id="country-select">
            <option value="">Выберите страну...</option>
            <#list countries as country>
                <option value="${country.name}">${country.name}</option>
            </#list>
        </select>
    </div>

    <div id="country-info"></div>
</div>

<script>
    $(document).ready(function() {
        // Загрузка списка стран при загрузке страницы
        $.get("?action=countries", function(data) {
            $('#country-select').empty();
            $('#country-select').append('<option value="">Выберите страну...</option>');
            $.each(data.countries, function(index, country) {
                $('#country-select').append('<option value="' + country + '">' + country + '</option>');
            });
        });

        // Фильтр по континентам
        $('#region-filter').change(function() {
            var region = $(this).val();
            window.location.href = region ? '?region=' + encodeURIComponent(region) : '.';
        });

        // Автодополнение
        $('#country-search').on('input', function() {
            var query = $(this).val();
            if (query.length >= 2) {
                $.get('?action=autocomplete&query=' + encodeURIComponent(query), function(data) {
                    var suggestions = $('#autocomplete-suggestions');
                    suggestions.empty();
                    if (data.length > 0) {
                        suggestions.show();
                        $.each(data, function(index, suggestion) {
                            suggestions.append(
                                '<div class="suggestion-item" data-name="' + suggestion + '">' +
                                suggestion + '</div>'
                            );
                        });
                    } else {
                        suggestions.hide();
                    }
                });
            } else {
                $('#autocomplete-suggestions').hide();
            }
        });

        // Обработка выбора из автодополнения
        $(document).on('click', '.suggestion-item', function() {
            var countryName = $(this).data('name');
            $('#country-search').val(countryName);
            $('#autocomplete-suggestions').hide();
            loadCountryInfo(countryName);
        });

        // Выбор страны из dropdown
        $('#country-select').change(function() {
            var countryName = $(this).val();
            if (countryName) {
                loadCountryInfo(countryName);
            } else {
                $('#country-info').empty();
            }
        });

        // Функция загрузки информации о стране
        function loadCountryInfo(countryName) {
            $.ajax({
                url: '?action=country&name=' + encodeURIComponent(countryName),
                type: 'GET',
                dataType: 'json',
                beforeSend: function(xhr) {
                    xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
                },
                success: function(country) {
                    displayCountryInfo(country);
                },
                error: function() {
                    $('#country-info').html('<p>Страна не найдена</p>');
                }
            });
        }

        // Функция отображения информации о стране
        function displayCountryInfo(country) {
            var html = '<div class="country-info">';
            html += '<h2>' + country.name + '</h2>';
            html += '<img src="' + country.flagUrl + '" alt="Флаг" class="flag">';
            html += '<p><strong>Официальное название:</strong> ' + country.officialName + '</p>';
            html += '<p><strong>Столица:</strong> ' + (country.capital || 'N/A') + '</p>';
            html += '<p><strong>Население:</strong> ' + country.population.toLocaleString() + '</p>';
            html += '<p><strong>Площадь:</strong> ' + country.area.toLocaleString() + ' км²</p>';

            if (country.languages && country.languages.length > 0) {
                html += '<p><strong>Языки:</strong> ' + country.languages.join(', ') + '</p>';
            }

            if (country.currencies && country.currencies.length > 0) {
                html += '<p><strong>Валюты:</strong> ' + country.currencies.join(', ') + '</p>';
            }

            if (country.timezones && country.timezones.length > 0) {
                html += '<p><strong>Часовые пояса:</strong> ' + country.timezones.join(', ') + '</p>';
            }

            if (country.borders && country.borders.length > 0) {
                html += '<div class="borders">';
                html += '<strong>Граничит с:</strong> ';
                country.borders.forEach(function(border) {
                    html += '<a href="#" class="border-link" data-name="' + border + '">' + border + '</a> ';
                });
                html += '</div>';
            }

            html += '</div>';

            $('#country-info').html(html);

            // Обработка кликов по граничным странам
            $('.border-link').click(function(e) {
                e.preventDefault();
                var borderCountry = $(this).data('name');
                loadCountryInfo(borderCountry);
                $('#country-search').val(borderCountry);
                $('#country-select').val(borderCountry);
            });
        }
    });
</script>
</body>
</html>