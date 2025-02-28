package com.conversor;

import java.util.Scanner;

public class CryptoApp {
    public static void main(String[] args) {
        CryptoService cryptoService = new CoinGeckoService();
        AwesomeApiService currencyService = new AwesomeApiService();
        CurrencyConverter converter = new CurrencyConverter(cryptoService, currencyService);

        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite a moeda desejada: ");
        String query = scanner.nextLine();

        System.out.print("Digite o valor em reais que deseja investir: ");
        String amount = scanner.nextLine();
        scanner.close();

        String result = converter.convertCurrency(query, amount);
        System.out.println(result);
    }
}
