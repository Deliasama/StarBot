package de.delia.starBot.main;

import de.delia.starBot.basics.TestCommand;
import de.delia.starBot.commands.CommandManager;
import de.delia.starBot.features.basics.StatusCommand;
import de.delia.starBot.features.stars.TradeManager;
import de.delia.starBot.features.stars.commands.*;
import de.delia.starBot.features.stars.listeners.ButtonInteractionListener;
import de.delia.starBot.features.stars.menus.StarDropMenu;
import de.delia.starBot.features.stars.listeners.MessageReceivedListener;
import de.delia.starBot.features.stars.tables.*;
import de.delia.starBot.guildConfig.ConfigCommand;
import de.delia.starBot.listeners.GuildReadyListener;
import de.delia.starBot.listeners.MessageListener;
import de.delia.starBot.listeners.SlashCommandInteractionListener;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Bot {
    public String version = "24w33b";
    public Instant startTime = Instant.now();

    public final JDA jda;
    public final CommandManager commandManager;
    public final EntityManagerFactory entityManagerFactory;

    public StarProfileTable starProfileTable;
    public DailyTable dailyTable;
    public StockHistory.StockHistoryTable stockHistoryTable;
    public Stock.StockTable stockTable;
    public Dividend.DividendTable dividendTable;

    public Map<Long, TradeManager> tradeManagers = new HashMap<>();

    // Menus
    public StarDropMenu starDropMenu;

    public Bot(String token) {

        entityManagerFactory = initDB();

        jda = JDABuilder.createDefault(token)
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setBulkDeleteSplittingEnabled(false)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("Bus verpassen"))
                .build();

        commandManager = new CommandManager(this);

        commandManager.registerCommand(TestCommand.class);
        commandManager.registerCommand(StatusCommand.class);

        // Stars
        commandManager.registerCommand(StarsCommand.class);
        commandManager.registerCommand(LeaderboardCommand.class);
        commandManager.registerCommand(DailyCommand.class);
        commandManager.registerCommand(RobCommand.class);
        commandManager.registerCommand(TradeCommand.class);

        commandManager.registerCommand(ConfigCommand.class);

        jda.addEventListener(new MessageReceivedListener());
        starDropMenu = new StarDropMenu(jda);

        jda.addEventListener(new MessageListener());
        jda.addEventListener(new SlashCommandInteractionListener());
        jda.addEventListener(new GuildReadyListener());
        jda.addEventListener(new ButtonInteractionListener());
    }

    public void initTables() {
        if(entityManagerFactory == null) {
            System.out.println("Datenbank fehler!");
            return;
        }
        starProfileTable = new StarProfileTable();
        dailyTable = new DailyTable();
        stockTable = new Stock.StockTable();
        stockHistoryTable = new StockHistory.StockHistoryTable();
        dividendTable = new Dividend.DividendTable();

    }

    public EntityManagerFactory initDB() {
        Dotenv dotenv = Main.DOTENV;
        String dbUrl = dotenv.get("DATABASE_HOST");
        String dbUsername = dotenv.get("DATABASE_USER");
        String dbPassword = dotenv.get("DATABASE_PASSWORD");

        if (dbUrl == null || dbUsername == null || dbPassword == null) return null;

        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", dbUrl);
        properties.put("javax.persistence.jdbc.user", dbUsername);
        properties.put("javax.persistence.jdbc.password", dbPassword);

        return Persistence.createEntityManagerFactory("PU2", properties);
    }
}
