/*
 * PoreRT - A Bukkit to Sponge Bridge
 *
 * Copyright (c) 2016-2017, Maxqia <https://github.com/Maxqia> AGPLv3
 * Copyright (c) 2014-2016, Lapis <https://github.com/LapisBlue> MIT
 * Copyright (c) Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * An exception applies to this license, see the LICENSE file in the main directory for more information.
 */

package blue.lapis.pore.impl;

import blue.lapis.pore.Pore;
import blue.lapis.pore.PoreVersion;
import blue.lapis.pore.converter.type.attribute.InventoryTypeConverter;
import blue.lapis.pore.converter.type.world.GeneratorTypeConverter;
import blue.lapis.pore.converter.type.world.WorldArchetypeConverter;
import blue.lapis.pore.converter.wrapper.WrapperConverter;
import blue.lapis.pore.converter.wrapper.world.ChunkGeneratorWrapper;
import blue.lapis.pore.impl.command.PoreCommandMap;
import blue.lapis.pore.impl.command.PoreConsoleCommandSender;
import blue.lapis.pore.impl.enchantments.PoreEnchantment;
import blue.lapis.pore.impl.entity.PorePlayer;
import blue.lapis.pore.impl.generator.PoreChunkData;
import blue.lapis.pore.impl.help.PoreHelpMap;
import blue.lapis.pore.impl.inventory.PoreInventory;
import blue.lapis.pore.impl.inventory.PoreItemFactory;
import blue.lapis.pore.impl.scheduler.PoreBukkitScheduler;
import blue.lapis.pore.impl.scoreboard.PoreScoreboardManager;
import blue.lapis.pore.impl.util.PoreCachedServerIcon;
import blue.lapis.pore.util.PoreCollections;
import blue.lapis.pore.util.PoreText;
import blue.lapis.pore.util.PoreWrapper;

import com.avaje.ebean.config.ServerConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.Warning;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.CachedServerIcon;
import org.bukkit.util.StringUtil;
import org.bukkit.util.permissions.DefaultPermissions;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.WorldProperties;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("deprecation") //TODO : fix this
public class PoreServer extends PoreWrapper<org.spongepowered.api.Server> implements Server {

    private final Game game;
    private final Logger logger;
    private final SimpleCommandMap commandMap;
    private final PluginManager pluginManager;
    private final ServicesManager servicesManager;
    private final Messenger messenger = new StandardMessenger();
    private final Warning.WarningState warnState = Warning.WarningState.DEFAULT;
    private final HelpMap helpMap = new PoreHelpMap();
    private final File pluginsDir = new File(".", "plugins");
    //TODO: use actual server directory, currently set to working directory

    private final BukkitScheduler scheduler = new PoreBukkitScheduler();
    private final BanList profileBans = new PoreUserBanList();
    private final BanList ipBans = new PoreIpBanList();
    private final ItemFactory itemFactory = new PoreItemFactory();
    private CachedServerIcon icon = null;

    public PoreServer(org.spongepowered.api.Game handle, org.slf4j.Logger logger) {
        super(handle.getServer());
        this.game = handle;
        this.logger = Logger.getLogger(logger.getName());
        this.commandMap = new PoreCommandMap(this);
        this.pluginManager = new SimplePluginManager(this, commandMap);
        this.servicesManager = new SimpleServicesManager();
        Bukkit.setServer(this);
        getFavicon();
        registerEnchantments();
    }

    public MinecraftServer getServer() {
        return (MinecraftServer) getHandle();
    }

    public Game getGame() {
        return game;
    }

    private void getFavicon() {
        try {
            icon = loadServerIcon(new File("server-icon.png"));
        } catch (Exception e) {
            logger.log(Level.CONFIG, "Could not find the server icon!");
        }
    }

    private static void registerEnchantments() {
        for (Field field : Enchantments.class.getFields()) {
            try {
                Enchantment.registerEnchantment(new PoreEnchantment(
                        (org.spongepowered.api.item.Enchantment) field.get(null)));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Enchantment.stopAcceptingRegistrations();
    }

    public void loadPlugins() {
        if (pluginsDir.isDirectory()) {
            // Clear plugins and prepare to load
            pluginManager.clearPlugins();
            pluginManager.registerInterface(JavaPluginLoader.class);

            // Call onLoad methods
            for (Plugin plugin : pluginManager.loadPlugins(pluginsDir)) {
                try {
                    getLogger().info(String.format("Loading %s", plugin.getDescription().getFullName()));
                    plugin.onLoad();
                } catch (RuntimeException ex) {
                    getLogger().log(Level.SEVERE,
                            ex.getMessage() + " initializing " + plugin.getDescription().getFullName()
                                    + " (Is it up to date?)", ex);
                }
            }
        } else {
            if (!pluginsDir.mkdir()) {
                logger.log(Level.SEVERE, "Could not create plugins directory: " + pluginsDir);
            }
        }
    }

    private void loadPlugin(Plugin plugin) {
        for (Permission perm : plugin.getDescription().getPermissions()) {
            try {
                pluginManager.addPermission(perm);
            } catch (IllegalArgumentException ex) {
                getLogger().log(Level.WARNING,
                        "Plugin " + plugin.getDescription().getFullName()
                                + " tried to register permission '" + perm.getName()
                                + "' but it's already registered", ex);
            }
        }

        try {
            pluginManager.enablePlugin(plugin);
        } catch (Throwable ex) {
            getLogger().log(Level.SEVERE,
                    ex.getMessage() + " loading " + plugin.getDescription().getFullName()
                            + " (Is it up to date?)");
        }
    }

    public void enablePlugins(PluginLoadOrder type) {
        if (type == PluginLoadOrder.STARTUP) {
            // TODO
            //helpMap.clear();
        }

        // Load all the plugins
        for (Plugin plugin : pluginManager.getPlugins()) {
            if (!plugin.isEnabled() && plugin.getDescription().getLoad() == type) {
                loadPlugin(plugin);
            }
        }

        if (type == PluginLoadOrder.POSTWORLD) {
            commandMap.setFallbackCommands();
            //commandMap.registerServerAliases();
            DefaultPermissions.registerCorePermissions();
            //helpMap.initializeCommands();
        }
    }

    public void disablePlugins() {
        pluginManager.disablePlugins();
    }

    @Override
    public String getName() {
        return PoreVersion.NAME;
    }

    @Override
    public String getVersion() {
        return PoreVersion.VERSION + '@' + game.getPlatform().getImplementation().getVersion();
    }

    @Override
    public String getBukkitVersion() {
        return PoreVersion.API_VERSION + '@' + game.getPlatform().getApi().getVersion();
    }

    @Override
    public Player[] _INVALID_getOnlinePlayers() {
        Collection<? extends Player> online = getOnlinePlayers();
        return online.toArray(new Player[online.size()]);
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return PoreCollections.transform(getHandle().getOnlinePlayers(),
                WrapperConverter.<org.spongepowered.api.entity.living.player.Player, PorePlayer>getConverter());
    }

    @Override
    public int getMaxPlayers() {
        return getHandle().getMaxPlayers();
    }

    @Override
    public int getPort() {
        Optional<InetSocketAddress> address = getHandle().getBoundAddress();
        if (address.isPresent()) {
            return address.get().getPort();
        } else {
            return -1;
        }
    }

    @Override
    public int getViewDistance() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public String getIp() {
        Optional<InetSocketAddress> address = getHandle().getBoundAddress();
        if (address.isPresent()) {
            return address.get().getHostName();
        } else {
            return "Unknown";
        }
    }

    @Override
    public String getServerName() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public String getServerId() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public String getWorldType() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean getGenerateStructures() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean getAllowEnd() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean getAllowNether() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean hasWhitelist() {
        return getHandle().hasWhitelist();
    }

    @Override
    public void setWhitelist(boolean value) {
        getHandle().setHasWhitelist(value);
    }

    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void reloadWhitelist() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int broadcastMessage(String message) {
        MessageChannel channel = getHandle().getBroadcastChannel();
        channel.send(PoreText.convert(message));
        return channel.getMembers().size();
    }

    @Override
    public String getUpdateFolder() {
        return "update";
    }

    @Override
    public File getUpdateFolderFile() {
        return new File(pluginsDir, getUpdateFolder());
    }

    @Override
    public long getConnectionThrottle() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Player getPlayer(String name) {
        Preconditions.checkNotNull(name, "name");

        org.spongepowered.api.entity.living.player.Player result = null;
        int delta = Integer.MAX_VALUE;
        for (org.spongepowered.api.entity.living.player.Player player : getHandle().getOnlinePlayers()) {
            if (StringUtil.startsWithIgnoreCase(player.getName(), name)) {
                int newDelta = player.getName().length() - name.length();
                if (newDelta < delta) {
                    result = player;
                    delta = newDelta;
                }

                if (newDelta == 0) {
                    break;
                }
            }
        }

        return result != null ? PorePlayer.of(result) : null;
    }

    @Override
    public Player getPlayerExact(String name) {
        Optional<org.spongepowered.api.entity.living.player.Player> player = getHandle().getPlayer(name);
        return player.isPresent() ? PorePlayer.of(player.get()) : null;
    }

    @Override
    public List<Player> matchPlayer(String name) {
        Preconditions.checkNotNull(name, "name");
        name = name.toLowerCase();

        List<Player> result = Lists.newArrayList();
        for (org.spongepowered.api.entity.living.player.Player player : getHandle().getOnlinePlayers()) {
            String playerName = player.getName().toLowerCase();

            if (name.equals(playerName)) {
                // Exact match
                return ImmutableList.<Player>of(PorePlayer.of(player));
            }

            if (playerName.contains(name)) {
                // Partial match
                result.add(PorePlayer.of(player));
            }
        }

        return result;
    }

    @Override
    public Player getPlayer(UUID id) {
        Optional<org.spongepowered.api.entity.living.player.Player> player = getHandle().getPlayer(id);
        return player.isPresent() ? PorePlayer.of(player.get()) : null;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public BukkitScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    @Override
    public List<World> getWorlds() {
        return PoreCollections.<org.spongepowered.api.world.World, World>transformToList(getHandle().getWorlds(),
                WrapperConverter.<org.spongepowered.api.world.World, PoreWorld>getConverter());
    }


    @Override
    public ChunkGenerator.ChunkData createChunkData(World world) {
        return PoreChunkData.of(((PoreWorld) world).getHandle().getRelativeBlockView());
    }

    @Override
    public World createWorld(WorldCreator creator) { // todo disabling structures, generator wrapper
        try {
            WorldArchetype archetype = WorldArchetype.builder().from(WorldArchetypeConverter.of(creator.environment()))
                    .generator(GeneratorTypeConverter.of(creator.type())).seed(creator.seed())
                    .generateSpawnOnLoad(false)
                    //.generator(type)
                    .build(creator.name(), creator.name());
            WorldProperties properties = Pore.getGame().getServer().createWorldProperties(creator.name(), archetype);

            org.spongepowered.api.world.World world = Pore.getGame().getServer().loadWorld(properties).get();

            world.getWorldGenerator().setBaseGenerationPopulator(new ChunkGeneratorWrapper(creator.generator()));
            world.getProperties().setGenerateSpawnOnLoad(true); // this loads spawn with our generator
            MinecraftServer.class.getMethod("func_71222_d").invoke(Pore.getGame().getServer()); //initialWorldChunkLoad

            return PoreWorld.of(world);
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public boolean unloadWorld(String name, boolean save) {
        Optional<org.spongepowered.api.world.World> world = getHandle().getWorld(name);
        return world.isPresent() && getHandle().unloadWorld(world.get());
    }

    @Override
    public boolean unloadWorld(World world, boolean save) {
        return getHandle().unloadWorld(((PoreWorld) world).getHandle());
    }

    @Override
    public World getWorld(String name) {
        Optional<org.spongepowered.api.world.World> world = getHandle().getWorld(name);
        return world.isPresent() ? PoreWorld.of(world.get()) : null;
    }

    @Override
    public World getWorld(UUID uid) {
        Optional<org.spongepowered.api.world.World> world = getHandle().getWorld(uid);
        return world.isPresent() ? PoreWorld.of(world.get()) : null;
    }

    @Override
    public MapView getMap(short id) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public MapView createMap(World world) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException("Sponge does not support server reloads");
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public PluginCommand getPluginCommand(String name) {
        Command command = commandMap.getCommand(name);

        if (command instanceof PluginCommand) {
            return (PluginCommand) command;
        } else {
            return null;
        }
    }

    @Override
    public void savePlayers() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) throws CommandException {
        Preconditions.checkNotNull(sender, "sender");
        Preconditions.checkNotNull(commandLine, "commandLine");

        return commandMap.dispatch(sender, commandLine);
    }

    @Override
    public void configureDbConfig(ServerConfig config) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean addRecipe(Recipe recipe) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public List<Recipe> getRecipesFor(ItemStack result) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Iterator<Recipe> recipeIterator() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void clearRecipes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void resetRecipes() {
        //throw new NotImplementedException("TODO");
    } // commented for ProtocolSupport

    @Override
    public Map<String, String[]> getCommandAliases() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getSpawnRadius() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void setSpawnRadius(int value) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean getOnlineMode() {
        return getHandle().getOnlineMode();
    }

    @Override
    public boolean getAllowFlight() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean isHardcore() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void shutdown() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int broadcast(String message, String permission) {
        int count = 0;
        Set<Permissible> permissibles = getPluginManager().getPermissionSubscriptions(permission);

        for (Permissible permissible : permissibles) {
            if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
                CommandSender user = (CommandSender) permissible;
                user.sendMessage(message);
                count++;
            }
        }

        return count;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String name) {
        User user = null;
        Optional<GameProfile> profile = Sponge.getServer().getGameProfileManager().getCache().getByName(name);
        if (profile.isPresent()) {
            Optional<UserStorageService> service = Sponge.getServiceManager().provide(UserStorageService.class);
            if (service.isPresent()) {
                UserStorageService usa = service.get();
                user = usa.get(profile.get()).orElse(null);
            }
        }
        return PoreOfflinePlayer.of(user);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
        User user = null;
        Optional<GameProfile> profile = Sponge.getServer().getGameProfileManager().getCache().getById(id);
        if (profile.isPresent()) {
            Optional<UserStorageService> service = Sponge.getServiceManager().provide(UserStorageService.class);
            if (service.isPresent()) {
                UserStorageService usa = service.get();
                user = usa.get(profile.get()).orElse(null);
            }
        }
        return PoreOfflinePlayer.of(user);
    }

    @Override
    public Set<String> getIPBans() {
        return PoreBanList.getBanService().getIpBans().stream().map(Ban.Ip::getAddress).map(Object::toString)
                .collect(GuavaCollectors.toImmutableSet());
    }

    @Override
    public void banIP(String address) {
        try {
            PoreBanList.getBanService().addBan(Ban.builder().address(InetAddress.getByName(address)).build());
        } catch (UnknownHostException ignored) {
            // Ignore silently
        }
    }

    @Override
    public void unbanIP(String address) {
        try {
            PoreBanList.getBanService().pardon(InetAddress.getByName(address));
        } catch (UnknownHostException ignored) {
            // Ignore silently
        }
    }

    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        return PoreBanList.getBanService().getProfileBans().stream().map(Ban.Profile::getProfile)
                .map(game.getServiceManager().provideUnchecked(UserStorageService.class)::get)
                .filter(Optional::isPresent).map(Optional::get).map(PoreOfflinePlayer::of)
                .collect(GuavaCollectors.toImmutableSet());
    }

    @Override
    public BanList getBanList(BanList.Type type) {
        switch (type) {
            case NAME:
                return profileBans;
            case IP:
                return ipBans;
            default:
                return null;
        }
    }

    @Override
    public Set<OfflinePlayer> getOperators() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public GameMode getDefaultGameMode() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void setDefaultGameMode(GameMode mode) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return PoreConsoleCommandSender.of((ConsoleSource) getHandle());
    }

    @Override
    public File getWorldContainer() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public PoreOfflinePlayer[] getOfflinePlayers() {
        ArrayList<PoreOfflinePlayer> array = new ArrayList<PoreOfflinePlayer>();
        Optional<UserStorageService> service = Sponge.getServiceManager().provide(UserStorageService.class);
        if (service.isPresent()) {
            UserStorageService usa = service.get();
            for (GameProfile profile : usa.getAll()) {
                Optional<User> user = usa.get(profile);
                if (user.isPresent()) {
                    array.add(PoreOfflinePlayer.of(user.get()));
                }
            }
        }
        return array.toArray(new PoreOfflinePlayer[array.size()]);
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public HelpMap getHelpMap() {
        return helpMap;
    }

    @Override
    public org.bukkit.inventory.Inventory createInventory(InventoryHolder owner, InventoryType type) {
        return createInventory(owner, type, null);
    }

    @Override
    public org.bukkit.inventory.Inventory createInventory(InventoryHolder owner, int size)
            throws IllegalArgumentException {
        return createInventory(owner, size, null);
    }

    private PoreInventory finalizeInventory(Inventory.Builder inventory, InventoryHolder owner, String title) {
        if (owner != null) {
            try {
                inventory = inventory.withCarrier((Carrier) owner.getClass().getMethod("getHandle").invoke(owner));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                // I guess it's not a carrier
                e.printStackTrace();
            }
        }

        if (title != null) {
            inventory = inventory.property(InventoryTitle.PROPERTY_NAME, new InventoryTitle(PoreText.convert(title)));
        }

        return PoreInventory.of(inventory.build(Pore.getPlugin()));
    }

    @Override
    public org.bukkit.inventory.Inventory createInventory(InventoryHolder owner, InventoryType type, String title) {
        Inventory.Builder inventory = Inventory.builder().of(InventoryTypeConverter.of(type));
        return finalizeInventory(inventory, owner, title);
    }

    @Override
    public org.bukkit.inventory.Inventory createInventory(InventoryHolder owner, int size, String title)
            throws IllegalArgumentException {
        if (size % 9 != 0) {
            throw new IllegalArgumentException("Size not divisable by 9!");
        }

        Inventory.Builder inventory = Inventory.builder().property(
                InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, size / 9));

        return finalizeInventory(inventory, owner, title);
    }

    @Override
    public int getMonsterSpawnLimit() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getAnimalSpawnLimit() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getAmbientSpawnLimit() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean isPrimaryThread() {
        return getHandle().isMainThread();
    }

    @Override
    public String getMotd() {
        return PoreText.convert(getHandle().getMotd());
    }

    @Override
    public String getShutdownMessage() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Warning.WarningState getWarningState() {
        return warnState;
    }

    @Override
    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    @Override
    public ScoreboardManager getScoreboardManager() {
        return PoreScoreboardManager.of(Scoreboard.builder());
    }

    @Override
    public CachedServerIcon getServerIcon() {
        return icon;
    }

    @Override
    public CachedServerIcon loadServerIcon(File file) throws Exception {
        return PoreCachedServerIcon.of(game.getRegistry().loadFavicon(new FileInputStream(file)));
    }

    @Override
    public CachedServerIcon loadServerIcon(BufferedImage image) throws Exception {
        return PoreCachedServerIcon.of(game.getRegistry().loadFavicon(image));
    }

    @Override
    public void setIdleTimeout(int threshold) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getIdleTimeout() {
        throw new NotImplementedException("TODO");
    }

    @Deprecated
    @Override
    public UnsafeValues getUnsafe() {
        return PoreUnsafeValues.INSTANCE;
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(getMessenger(), source, channel, message);

        for (Player player : getOnlinePlayers()) {
            player.sendPluginMessage(source, channel, message);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        Set<String> result = new HashSet<>();

        for (Player player : getOnlinePlayers()) {
            result.addAll(player.getListeningPluginChannels());
        }

        return result;
    }

    @Override
    public BossBar createBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Merchant createMerchant(String title) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Server.Spigot spigot() {
        return new Spigot();
    }

    public class Spigot extends Server.Spigot {
        @Override
        public void restart() {
            PoreServer.this.shutdown();
        }
    }
}
