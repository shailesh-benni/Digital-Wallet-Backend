package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wallet")
public class WalletConfig {

    private TopUp topup;
    private Transaction transaction;

    public static class TopUp {
        private int maxCountPerDay;
        private double maxAmountPerDay;

        public int getMaxCountPerDay() { return maxCountPerDay; }
        public void setMaxCountPerDay(int maxCountPerDay) { this.maxCountPerDay = maxCountPerDay; }
        public double getMaxAmountPerDay() { return maxAmountPerDay; }
        public void setMaxAmountPerDay(double maxAmountPerDay) { this.maxAmountPerDay = maxAmountPerDay; }
    }

    public static class Transaction {
        private double dailyLimit;
        public double getDailyLimit() { return dailyLimit; }
        public void setDailyLimit(double dailyLimit) { this.dailyLimit = dailyLimit; }
    }

    public TopUp getTopup() { return topup; }
    public void setTopup(TopUp topup) { this.topup = topup; }
    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }
}
