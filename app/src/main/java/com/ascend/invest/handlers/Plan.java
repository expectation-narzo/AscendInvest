package com.ascend.invest.handlers;

public class Plan {
    private String id;
    private String name;
    private String description;
    private double investAmount;
    private double dailyProfit;
    private double totalProfit;
    private double profitPercentage;
    private int durationDays;
    private String category;
    private boolean active;
    private int purchaseLimit;
    private boolean featured;

    public Plan() {
        // Required for Firebase
    }

    public Plan(String id, String name, String description, double investAmount, double dailyProfit, double totalProfit, double profitPercentage, int durationDays, String category, boolean active, int purchaseLimit, boolean featured) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.investAmount = investAmount;
        this.dailyProfit = dailyProfit;
        this.totalProfit = totalProfit;
        this.profitPercentage = profitPercentage;
        this.durationDays = durationDays;
        this.category = category;
        this.active = active;
        this.purchaseLimit = purchaseLimit;
        this.featured = featured;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getInvestAmount() { return investAmount; }
    public void setInvestAmount(double investAmount) { this.investAmount = investAmount; }

    public double getDailyProfit() { return dailyProfit; }
    public void setDailyProfit(double dailyProfit) { this.dailyProfit = dailyProfit; }

    public double getTotalProfit() { return totalProfit; }
    public void setTotalProfit(double totalProfit) { this.totalProfit = totalProfit; }

    public double getProfitPercentage() { return profitPercentage; }
    public void setProfitPercentage(double profitPercentage) { this.profitPercentage = profitPercentage; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getPurchaseLimit() { return purchaseLimit; }
    public void setPurchaseLimit(int purchaseLimit) { this.purchaseLimit = purchaseLimit; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
}
