package de.raidcraft.servershop;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Strings;
import de.raidcraft.servershop.commands.AdminCommands;
import de.raidcraft.servershop.commands.PlayerCommands;
import de.raidcraft.servershop.entities.*;
import de.raidcraft.servershop.listener.SignListener;
import io.ebean.Database;
import kr.entree.spigradle.annotations.PluginMain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.Config;
import net.silthus.ebean.EbeanWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@PluginMain
public class ServerShopPlugin extends JavaPlugin {

    public static final String DEFAULT_SHOP_IDENTIFIER = "default";

    @Getter
    @Accessors(fluent = true)
    private static ServerShopPlugin instance;

    private Database database;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private PluginConfig pluginConfig;

    private PaperCommandManager commandManager;
    private SignListener signListener;
    @Getter
    private ShopManager shopManager;

    @Getter
    private static boolean testing = false;

    public ServerShopPlugin() {
        instance = this;
    }

    public ServerShopPlugin(
            JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        instance = this;
        testing = true;
    }

    @Override
    public void onEnable() {

        loadConfig();
        setupDatabase();
        createDefaultShop();
        setupShopManager();
        setupListener();
        setupCommands();
    }

    public void reload() {

        loadConfig();
    }

    private void loadConfig() {

        getDataFolder().mkdirs();
        pluginConfig = new PluginConfig(new File(getDataFolder(), "config.yml").toPath());
        pluginConfig.loadAndSave();
    }

    private void setupShopManager() {

        this.shopManager = new ShopManager(this);
    }

    private void setupListener() {

        signListener = new SignListener(this);
        Bukkit.getPluginManager().registerEvents(signListener, this);
    }

    private void createDefaultShop() {

        ServerShop.defaultShop()
                .name(pluginConfig.getDefaultShopName())
                .save();
    }

    private void setupCommands() {

        this.commandManager = new PaperCommandManager(this);

        registerItemsCompletion(commandManager);
        registerShopCompletion(commandManager);

        registerShopContext(commandManager);
        registerMaterialContext(commandManager);

        commandManager.registerCommand(new AdminCommands(this));
        commandManager.registerCommand(new PlayerCommands(this));
    }

    private final Set<String> items = new HashSet<>();

    private void registerItemsCompletion(PaperCommandManager commandManager) {

        items.clear();
        items.addAll(Arrays.stream(Material.values())
                .map(Material::getKey)
                .map(NamespacedKey::getKey)
                .collect(Collectors.toSet())
        );
        commandManager.getCommandCompletions().registerAsyncCompletion("items", context -> items);
    }

    private void registerShopCompletion(PaperCommandManager commandManager) {

        commandManager.getCommandCompletions().registerAsyncCompletion("shops", context -> ServerShop.find.all()
                .stream().map(ServerShop::identifier)
                .collect(Collectors.toSet()));
    }

    private void registerMaterialContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerContext(Material.class, context -> {
            String name = context.popFirstArg();
            Material material = Material.matchMaterial(name);
            if (material == null) {
                throw new InvalidCommandArgument("Das Item \"" + name + "\" wurde nicht gefunden.");
            }
            return material;
        });
    }

    private void registerShopContext(PaperCommandManager commandManager) {

        commandManager.getCommandContexts().registerContext(ServerShop.class, context -> {
            String identifier = context.popFirstArg();
            if (Strings.isNullOrEmpty(identifier)) {
                return ServerShop.defaultShop();
            }

            return ServerShop.byIdentifier(identifier)
                    .orElseThrow(() -> new InvalidCommandArgument("Der Server Shop \"" + identifier + "\" wurde nicht gefunden."));
        });
    }

    private void setupDatabase() {

        this.database = new EbeanWrapper(Config.builder(this)
                .entities(
                        Offer.class,
                        ServerShop.class,
                        ShopPlayer.class,
                        ShopSign.class,
                        Transaction.class
                )
                .build()).connect();
    }
}
