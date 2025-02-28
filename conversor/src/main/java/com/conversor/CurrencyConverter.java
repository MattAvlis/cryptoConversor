package com.conversor;

import java.io.IOException;
import java.util.Map;

public class CurrencyConverter {
    private final CryptoService cryptoService;
    private final AwesomeApiService currencyService;

    public CurrencyConverter(CryptoService cryptoService, AwesomeApiService currencyService) {
        this.cryptoService = cryptoService;
        this.currencyService = currencyService;
    }

    public String convertCurrency(String query, String value) {
        StringBuilder result = new StringBuilder();

        try {
            if (query.isBlank() && value.isBlank()) {
                // Retornar apenas as cotações sem a quantidade de retorno
                result.append(getAllRatesWithoutAmount());
            } else {
                double amount = value.isBlank() ? 0 : Double.parseDouble(value);

                if (query.isBlank()) {
                    // Retornar conversão padrão
                    result.append(convertWithFallbackCurrencies(amount));
                } else {
                    // Conversão específica
                    result.append(convertSpecificCurrency(query, amount));
                    if (!value.isBlank()) {
                        result.append(convertWithFallbackCurrencies(amount, false));
                    } else {
                        result.append(convertWithFallbackCurrencies(0, false));
                    }
                }
            }
        } catch (NumberFormatException e) {
            result.append("Valor inválido.");
        } catch (IOException e) {
            result.append("Erro ao obter informações: ").append(e.getMessage());
        }

        return result.toString();
    }

    private String convertSpecificCurrency(String query, double amount) throws IOException {
        StringBuilder result = new StringBuilder();
        Map<String, Double> rates = currencyService.getCurrencyRates();

        if (rates != null && rates.containsKey(query.toUpperCase())) {
            double rate = rates.get(query.toUpperCase());
            if (amount > 0) {
                result.append(String.format("%s (%s) - R$%.2f - Quantidade de retorno: %.2f %s\n\n",
                        query, query.toUpperCase(), rate, amount / rate, query.toUpperCase()));
            } else {
                result.append(String.format("%s (%s) - R$%.2f\n\n", query, query.toUpperCase(), rate));
            }
        } else {
            CryptoInfo cryptoInfo = cryptoService.findCryptoInfo(query);
            if (cryptoInfo != null) {
                double price = cryptoService.getCryptoPrice(cryptoInfo.getId());
                if (price != -1) {
                    if (amount > 0) {
                        result.append(String.format("%s (%s) - $%.4f - Quantidade de retorno: %.4f %s\n\n",
                                cryptoInfo.getName(), cryptoInfo.getSymbol(), price, amount / price, cryptoInfo.getSymbol()));
                    } else {
                        result.append(String.format("%s (%s) - $%.4f\n\n", cryptoInfo.getName(), cryptoInfo.getSymbol(), price));
                    }
                }
            }
        }

        return result.toString();
    }

    private String convertWithFallbackCurrencies(double amount) throws IOException {
        return convertWithFallbackCurrencies(amount, true);
    }

    private String convertWithFallbackCurrencies(double amount, boolean includeCrypto) throws IOException {
        StringBuilder result = new StringBuilder();
        Map<String, Double> rates = currencyService.getCurrencyRates();

        if (rates != null) {
            double usdRate = rates.get("USD");
            double eurRate = rates.get("EUR");
            if (amount > 0) {
                result.append(String.format("USD (USD) - R$%.2f - Quantidade de retorno: %.2f USD\n\n", usdRate, amount / usdRate));
                result.append(String.format("EUR (EUR) - R$%.2f - Quantidade de retorno: %.2f EUR\n\n", eurRate, amount / eurRate));
            } else {
                result.append(String.format("USD (USD) - R$%.2f\n\n", usdRate));
                result.append(String.format("EUR (EUR) - R$%.2f\n\n", eurRate));
            }
        }

        if (includeCrypto) {
            CryptoInfo bitcoinInfo = cryptoService.findCryptoInfo("bitcoin");
            if (bitcoinInfo != null) {
                double bitcoinPrice = cryptoService.getCryptoPrice(bitcoinInfo.getId());
                if (bitcoinPrice != -1) {
                    if (amount > 0) {
                        result.append(String.format("Bitcoin (BTC) - $%.4f - Quantidade de retorno: %.4f BTC\n\n", bitcoinPrice, amount / bitcoinPrice));
                    } else {
                        result.append(String.format("Bitcoin (BTC) - $%.4f\n\n", bitcoinPrice));
                    }
                }
            }

            CryptoInfo ethereumInfo = cryptoService.findCryptoInfo("ethereum");
            if (ethereumInfo != null) {
                double ethereumPrice = cryptoService.getCryptoPrice(ethereumInfo.getId());
                if (ethereumPrice != -1) {
                    if (amount > 0) {
                        result.append(String.format("Ethereum (ETH) - $%.4f - Quantidade de retorno: %.4f ETH\n\n", ethereumPrice, amount / ethereumPrice));
                    } else {
                        result.append(String.format("Ethereum (ETH) - $%.4f\n\n", ethereumPrice));
                    }
                }
            }
        }

        return result.toString();
    }

    private String getAllRatesWithoutAmount() throws IOException {
        StringBuilder result = new StringBuilder();
        Map<String, Double> rates = currencyService.getCurrencyRates();

        if (rates != null) {
            result.append(String.format("USD (USD) - R$%.2f\n\n", rates.get("USD")));
            result.append(String.format("EUR (EUR) - R$%.2f\n\n", rates.get("EUR")));
        }

        CryptoInfo bitcoinInfo = cryptoService.findCryptoInfo("bitcoin");
        if (bitcoinInfo != null) {
            double bitcoinPrice = cryptoService.getCryptoPrice(bitcoinInfo.getId());
            if (bitcoinPrice != -1) {
                result.append(String.format("Bitcoin (BTC) - $%.4f\n\n", bitcoinPrice));
            }
        }

        CryptoInfo ethereumInfo = cryptoService.findCryptoInfo("ethereum");
        if (ethereumInfo != null) {
            double ethereumPrice = cryptoService.getCryptoPrice(ethereumInfo.getId());
            if (ethereumPrice != -1) {
                result.append(String.format("Ethereum (ETH) - $%.4f\n\n", ethereumPrice));
            }
        }

        return result.toString();
    }
}
