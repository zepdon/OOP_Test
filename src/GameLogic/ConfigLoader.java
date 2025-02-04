package GameLogic;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static ConfigLoader instance;
    private Properties properties;

    public final long spawnCost;
    public final long hexPurchaseCost;
    public final long initBudget;
    public final long initHP;
    public final long turnBudget;
    public final long maxBudget;
    public final long interestPct;
    public final long maxTurns;
    public final long maxSpawns;

    private ConfigLoader(String configFilePath) throws IOException {
        properties = new Properties();
        try (FileInputStream in = new FileInputStream(configFilePath)) {
            properties.load(in);
        }
        spawnCost = Long.parseLong(properties.getProperty("spawn_cost"));
        hexPurchaseCost = Long.parseLong(properties.getProperty("hex_purchase_cost"));
        initBudget = Long.parseLong(properties.getProperty("init_budget"));
        initHP = Long.parseLong(properties.getProperty("init_hp"));
        turnBudget = Long.parseLong(properties.getProperty("turn_budget"));
        maxBudget = Long.parseLong(properties.getProperty("max_budget"));
        interestPct = Long.parseLong(properties.getProperty("interest_pct"));
        maxTurns = Long.parseLong(properties.getProperty("max_turns"));
        maxSpawns = Long.parseLong(properties.getProperty("max_spawns"));
    }

    public static ConfigLoader getInstance(String configFilePath) throws IOException {
        if (instance == null) {
            instance = new ConfigLoader(configFilePath);
        }
        return instance;
    }
}
