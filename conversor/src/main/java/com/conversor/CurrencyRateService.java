package com.conversor;

import java.io.IOException;
import java.util.Map;

public interface CurrencyRateService {
    Map<String, Double> getCurrencyRates() throws IOException;
}
