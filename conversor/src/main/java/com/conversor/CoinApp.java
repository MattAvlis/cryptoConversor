package com.conversor;

import java.util.Scanner;

public class CoinApp {
    public static void main(String[] args) {
        CryptoService service = new CoinGeckoService();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite a moeda desejada: ");
        String query = scanner.nextLine();
        scanner.close();

        String summary = service.getCryptoSummary(query);
        System.out.println(summary);
    }
}
