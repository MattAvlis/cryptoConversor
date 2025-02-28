package com.conversor;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AwesomeApiService implements CurrencyRateService {
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    // Lista de moedas fiduciárias conhecidas
    private static final Set<String> FIAT_CURRENCIES = Set.of("USD", "EUR", "RUB", "ARS", "GBP", "JPY");

    public AwesomeApiService() {
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public Map<String, Double> getCurrencyRates() {
        try {
            return fetchCurrencyRates();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Double> fetchCurrencyRates() throws IOException {
        Request request = new Request.Builder()
            .url("https://economia.awesomeapi.com.br/json/last/USD-BRL,EUR-BRL,RUB-BRL,ARS-BRL,GBP-BRL,JPY-BRL")
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseData = response.body().string();
            Map<String, Map<String, String>> result = mapper.readValue(responseData, new TypeReference<>() {});

            Map<String, Double> rates = new HashMap<>();
            rates.put("USD", Double.parseDouble(result.get("USDBRL").get("bid")));
            rates.put("EUR", Double.parseDouble(result.get("EURBRL").get("bid")));
            rates.put("RUB", Double.parseDouble(result.get("RUBBRL").get("bid")));
            rates.put("ARS", Double.parseDouble(result.get("ARSBRL").get("bid")));
            rates.put("GBP", Double.parseDouble(result.get("GBPBRL").get("bid")));
            rates.put("JPY", Double.parseDouble(result.get("JPYBRL").get("bid")));

            return rates;
        }
    }

    private boolean isCrypto(String query) {
        return !FIAT_CURRENCIES.contains(query.toUpperCase());
    }

    public String getCurrencyRatesWithFallback(String query) {
        Map<String, Double> rates = getCurrencyRates();

        if (query.isBlank() || isCrypto(query) || !(query.equalsIgnoreCase("USD") || query.equalsIgnoreCase("EUR"))) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cotação em relação ao Real:\n");
            if (rates != null) {
                sb.append("USD: $").append(String.format("%.2f", rates.get("USD"))).append("\n");
                sb.append("EUR: $").append(String.format("%.2f", rates.get("EUR"))).append("\n");
            } else {
                sb.append("Erro ao obter as cotações.\n");
            }
            return sb.toString();
        }

        return formatRates(rates);
    }

    private String formatRates(Map<String, Double> rates) {
        StringBuilder sb = new StringBuilder();
        sb.append("Cotação em relação ao Real:\n");
        if (rates != null) {
            for (Map.Entry<String, Double> rate : rates.entrySet()) {
                sb.append(rate.getKey()).append(": $").append(String.format("%.2f", rate.getValue())).append("\n");
            }
        } else {
            sb.append("Erro ao obter as cotações.\n");
        }
        return sb.toString();
    }
}
