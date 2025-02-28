package com.conversor;

public class CryptoInfo {
    private String id;
    private String name;
    private String symbol;

    public CryptoInfo() {
    }

    public CryptoInfo(String id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSymbol() { return symbol; }

    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}
