package com.conversor;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class CoinGeckoService implements CryptoService {
    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final LoadingCache<String, CryptoInfo> cache;

    public CoinGeckoService() {
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();

        this.cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public CryptoInfo load(String key) throws IOException {
                    if (key.isBlank()) {
                        throw new IOException("Query cannot be blank");
                    }
                    return findCryptoInfoFromAPI(key);
                }
            });

        loadCache();
    }

    @Override
    public CryptoInfo findCryptoInfo(String query) {
        try {
            return cache.get(query);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private CryptoInfo findCryptoInfoFromAPI(String query) throws IOException {
        Request request = new Request.Builder()
            .url("https://api.coingecko.com/api/v3/search?query=" + query)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseData = response.body().string();
            Map<String, Object> result = mapper.readValue(responseData, Map.class);
            List<?> coins = (List<?>) result.get("coins");
            if (coins == null || coins.isEmpty()) {
                System.out.println("No coins found for query: " + query);
                return null;
            }

            Map<String, Object> coin = (Map<String, Object>) coins.get(0);
            return new CryptoInfo((String) coin.get("id"), (String) coin.get("name"), (String) coin.get("symbol"));
        }
    }

    @Override
    public double getCryptoPrice(String id) {
        try {
            Request request = new Request.Builder()
                .url("https://api.coingecko.com/api/v3/simple/price?ids=" + id + "&vs_currencies=usd")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseData = response.body().string();
                Map<String, Map<String, Object>> result = mapper.readValue(responseData, new TypeReference<Map<String, Map<String, Object>>>() {});
                if (!result.containsKey(id)) throw new IOException("Price not found");

                Object priceObj = result.get(id).get("usd");
                if (priceObj instanceof Integer) {
                    return ((Integer) priceObj).doubleValue();
                } else if (priceObj instanceof Double) {
                    return (Double) priceObj;
                } else {
                    throw new IOException("Unexpected price format");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public String getCryptoSummary(String query) {

        CryptoInfo cryptoInfo = findCryptoInfo(query);
        if (cryptoInfo == null) {
            return "Crypto not found.";
        }

        double price = getCryptoPrice(cryptoInfo.getId());
        if (price == -1) {
            return "Error retrieving price.";
        }

        return cryptoInfo + " - Current price: $" + price;
    }

    private void loadCache() {
        File file = new File("cryptoCache.json");
        if (file.exists()) {
            try {
                Map<String, CryptoInfo> loadedCache = mapper.readValue(file, new TypeReference<Map<String, CryptoInfo>>() {});
                cache.putAll(loadedCache);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
