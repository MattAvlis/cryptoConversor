package com.conversor;

import java.io.IOException;

public interface CryptoService {
    CryptoInfo findCryptoInfo(String query) throws IOException;
    double getCryptoPrice(String id) throws IOException;
    String getCryptoSummary(String query);
}
