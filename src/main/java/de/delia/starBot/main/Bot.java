package de.delia.starBot.main;

import de.delia.starBot.commands.CommandManager;
import de.delia.starBot.features.basics.InviteCommand;
import de.delia.starBot.features.basics.StatusCommand;
import de.delia.starBot.features.birthday.BirthdayCommand;
import de.delia.starBot.features.birthday.BirthdayManager;
import de.delia.starBot.features.birthday.BirthdayTable;
import de.delia.starBot.features.stars.StarProfileManager;
import de.delia.starBot.features.stars.TradeManager;
import de.delia.starBot.features.stars.commands.*;
import de.delia.starBot.features.stars.listeners.ButtonInteractionListener;
import de.delia.starBot.features.stars.listeners.MessageReceivedListener;
import de.delia.starBot.features.stars.listeners.VoiceStarsListeners;
import de.delia.starBot.features.stars.menus.StarDropMenu;
import de.delia.starBot.features.stars.tables.*;
import de.delia.starBot.guildConfig.ConfigCommand;
import de.delia.starBot.guildConfig.GuildConfig;
import de.delia.starBot.listeners.GuildReadyListener;
import de.delia.starBot.listeners.SlashCommandInteractionListener;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Bot {
    public final JDA jda;
    public final CommandManager commandManager;
    public final EntityManagerFactory entityManagerFactory;
    public String version;
    public Instant startTime = Instant.now();
    public StarProfileTable starProfileTable;
    public DailyTable dailyTable;
    public StockHistory.StockHistoryTable stockHistoryTable;
    public Stock.StockTable stockTable;
    public Dividend.DividendTable dividendTable;
    public GuildConfig.GuildConfigTable guildConfigTable;
    public BirthdayTable birthdayTable;
    public BuildingEntity.BuildingTable buildingTable;

    // Managers
    public BirthdayManager birthdayManager;
    public StarProfileManager starProfileManager;
    public Map<Long, TradeManager> tradeManagers = new HashMap<>();

    // Menus
    public StarDropMenu starDropMenu;

    // Discord Logger
    public DiscordLogging discordLogging;

    public Bot(String token) {
        // get Project version
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("version.properties"));
            version = properties.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Project version: " + version);

        // init Database
        entityManagerFactory = initDB();

        // Create Discord JDA Instance
        jda = JDABuilder.createDefault(token)
                .disableCache(CacheFlag.MEMBER_OVERRIDES)
                .setBulkDeleteSplittingEnabled(false)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
                .build();

        // Commands
        commandManager = new CommandManager(this);

        commandManager.registerCommand(StatusCommand.class);
        commandManager.registerCommand(ConfigCommand.class);
        commandManager.registerCommand(InviteCommand.class);

        // Stars
        commandManager.registerCommand(StarsCommand.class);
        commandManager.registerCommand(LeaderboardCommand.class);
        commandManager.registerCommand(DailyCommand.class);
        commandManager.registerCommand(RobCommand.class);
        commandManager.registerCommand(TradeCommand.class);
        commandManager.registerCommand(TownCommand.class);
        commandManager.registerCommand(Visit.class);

        starProfileManager = new StarProfileManager();

        commandManager.registerCommand(BirthdayCommand.class);

        jda.addEventListener(new MessageReceivedListener());
        starDropMenu = new StarDropMenu(jda);

        jda.addEventListener(new SlashCommandInteractionListener());
        jda.addEventListener(new GuildReadyListener());
        jda.addEventListener(new ButtonInteractionListener());
        jda.addEventListener(new VoiceStarsListeners());

        birthdayManager = new BirthdayManager(this);

        discordLogging = new DiscordLogging(this);

        System.out.println(jda.getInviteUrl(Permission.ADMINISTRATOR));
    }

    public void initTables() {
        if (entityManagerFactory == null) {
            System.err.println("Database error!");
            return;
        }
        starProfileTable = new StarProfileTable();
        dailyTable = new DailyTable();
        stockTable = new Stock.StockTable();
        stockHistoryTable = new StockHistory.StockHistoryTable();
        dividendTable = new Dividend.DividendTable();
        buildingTable = new BuildingEntity.BuildingTable();

        guildConfigTable = new GuildConfig.GuildConfigTable();

        birthdayTable = new BirthdayTable();
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
