/*
 * PoreRT - A Bukkit to Sponge Bridge
 *
 * Copyright (c) 2016, Maxqia <https://github.com/Maxqia> AGPLv3
 * Copyright (c) 2014-2016, Lapis <https://github.com/LapisBlue> MIT
 * Copyright (c) Spigot/Craftbukkit Project <https://hub.spigotmc.org/stash/projects/SPIGOT> LGPLv3
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

package blue.lapis.pore.impl.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.api.data.manipulator.catalog.CatalogEntityData.EXPERIENCE_HOLDER_DATA;
import static org.spongepowered.api.data.manipulator.catalog.CatalogEntityData.JOIN_DATA;

import blue.lapis.pore.Pore;
import blue.lapis.pore.converter.type.entity.EntityConverter;
import blue.lapis.pore.converter.type.material.MaterialConverter;
import blue.lapis.pore.converter.type.statistic.AchievementConverter;
import blue.lapis.pore.converter.type.statistic.StatisticConverter;
import blue.lapis.pore.converter.type.world.effect.EffectConverter;
import blue.lapis.pore.converter.type.world.effect.ParticleConverter;
import blue.lapis.pore.converter.type.world.effect.SoundCategoryConverter;
import blue.lapis.pore.converter.type.world.effect.SoundConverter;
import blue.lapis.pore.converter.vector.LocationConverter;
import blue.lapis.pore.converter.vector.VectorConverter;
import blue.lapis.pore.converter.wrapper.WrapperConverter;
import blue.lapis.pore.impl.PoreWorld;
import blue.lapis.pore.impl.inventory.PoreInventory;
import blue.lapis.pore.impl.inventory.PoreInventoryView;
import blue.lapis.pore.impl.metadata.PoreMetadataStore;
import blue.lapis.pore.impl.scoreboard.PoreScoreboard;
import blue.lapis.pore.util.PoreText;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;

import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MainHand;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataStore;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.TargetedLocationData;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.api.data.type.HandPreferences;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.resourcepack.ResourcePacks;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.api.statistic.BlockStatistic;
import org.spongepowered.api.statistic.EntityStatistic;
import org.spongepowered.api.statistic.StatisticGroup;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.world.World;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PorePlayer extends PoreHumanEntity implements org.bukkit.entity.Player {

    private static final MetadataStore<org.bukkit.entity.Player> playerMeta =
            new PoreMetadataStore<org.bukkit.entity.Player>();
    private String displayName = this.getName();

    public static PorePlayer of(Player handle) {
        return WrapperConverter.of(PorePlayer.class, handle);
    }

    protected PorePlayer(Player handle) {
        super(handle);
    }

    @Override
    public EntityPlayerMP getMCHandle() {
        return (EntityPlayerMP) getHandle();
    }

    @Override
    public Player getHandle() {
        return (Player) super.getHandle();
    }

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        playerMeta.setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return playerMeta.getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return playerMeta.hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        playerMeta.removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String name) {
        displayName = (name == null ? getName() : name);
    }

    @Override
    public String getPlayerListName() {
        Optional<TabListEntry> info = this.getHandle().getTabList().getEntry(this.getUniqueId());
        return info.isPresent() ? PoreText.convert(info.get().getDisplayName().orElse(null)) : this.getDisplayName();
    }

    @Override
    public void setPlayerListName(String name) {
        Optional<TabListEntry> info = this.getHandle().getTabList().getEntry(this.getUniqueId());
        if (info.isPresent()) {
            info.get().setDisplayName(PoreText.convert(name));
        }
    }

    @Override
    public Location getCompassTarget() {
        Optional<TargetedLocationData> data = getHandle().get(TargetedLocationData.class);
        if (data.isPresent()) {
            return LocationConverter.fromVector3d(getHandle().getWorld(), data.get().target().get());
        }
        return null;
    }

    @Override
    public void setCompassTarget(Location loc) {
        Optional<TargetedLocationData> data = getHandle().getOrCreate(TargetedLocationData.class);
        if (data.isPresent()) {
           data.get().target().set(LocationConverter.of(loc).getPosition());
        }
    }

    @Override
    public InetSocketAddress getAddress() {
        return getHandle().getConnection().getAddress();
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(String input) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void sendRawMessage(String message) {
        this.sendMessage(message);
    }

    @Override
    public void kickPlayer(String message) {
        getHandle().kick(PoreText.convert(message));
    }

    @Override
    public void chat(String msg) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean performCommand(String command) {
        // TODO: Does this work properly?
        CommandResult result = Pore.getGame().getCommandManager().process(getHandle(), command);
        return result.getSuccessCount().isPresent() && result.getSuccessCount().get() > 0;
    }

    @Override
    public boolean isSneaking() {
        return getHandle().get(Keys.IS_SNEAKING).get();
    }

    @Override
    public void setSneaking(boolean sneak) {
        getHandle().offer(Keys.IS_SNEAKING, sneak);
    }

    @Override
    public boolean isSprinting() {
        return getHandle().get(Keys.IS_SPRINTING).get();
    }

    @Override
    public void setSprinting(boolean sprinting) {
        getHandle().offer(Keys.IS_SPRINTING, sprinting);
    }

    @Override
    public void saveData() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void loadData() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean isSleepingIgnored() {
        //TODO: This feature is deeply implemented in CB, so I have no damn clue how we're going to manage to implement
        // this on top of Sponge short of mixins (which is obviously a really bad idea).
        throw new NotImplementedException("TODO");
    }

    @Override
    public void setSleepingIgnored(boolean isSleeping) {
        //TODO: Same deal here. I commented the NotImplementedExcpetion out temporarily to keep Essentials from
        // freaking out every two seconds when it tries to call this method from a scheduler.
        //throw new NotImplementedException("TODO");
    }

    @Override
    public void playNote(Location loc, byte instrument, byte note) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void playNote(Location loc, Instrument instrument, Note note) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch) {
        this.playSound(location, Sound.valueOf(sound), category, volume, pitch);
    }

    @Override
    public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {
        getHandle().playSound(SoundConverter.of(sound),
                SoundCategoryConverter.of(category),
                VectorConverter.create3d(location), volume, pitch);
    }

    @Override
    public void playSound(Location location, String sound, float volume, float pitch) {
        this.playSound(location, Sound.valueOf(sound), volume, pitch);
    }

    @Override
    public void playSound(Location location, Sound sound, float volume, float pitch) {
        getHandle().playSound(SoundConverter.of(sound), VectorConverter.create3d(location), volume, pitch);
    }

    @Override
    public void playEffect(Location location, Effect effect, int data) {
        EffectConverter.playEffect(getHandle(), location, effect, data, PoreWorld.DEFAULT_EFFECT_RADIUS);
    }

    @Override
    public <T> void playEffect(Location location, Effect effect, T data) {
        EffectConverter.playEffect(getHandle(), location, effect, data, PoreWorld.DEFAULT_EFFECT_RADIUS);
    }

    @Override
    public void sendBlockChange(Location loc, Material material, byte data) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean sendChunkChange(Location loc, int sx, int sy, int sz, byte[] data) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void sendBlockChange(Location loc, int material, byte data) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void sendSignChange(Location loc, String[] lines) throws IllegalArgumentException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void sendMap(MapView map) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Inventory getEnderChest() {
        return PoreInventory.of(getHandle().getEnderChestInventory());
    }

    @Override
    public void updateInventory() {
        EntityPlayerMP player = ((EntityPlayerMP) getHandle());
        player.inventoryContainer.detectAndSendChanges();
    } // No function to do this natively in Sponge

    @Override
    public InventoryView openInventory(Inventory inventory) {
        getHandle().openInventory(((PoreInventory)inventory).getHandle(), Cause.source(this).build());
        return PoreInventoryView.builder().setPlayer(getHandle())
                .setBottomInventory(this.getInventory()).setTopInventory(inventory).build();
    }

    @Override
    public void openInventory(InventoryView inventory) {
        this.openInventory(inventory.getTopInventory());
    }

    @Override
    public void awardAchievement(Achievement achievement) {
        getHandle().offer(getHandle().getAchievementData().achievements().add(AchievementConverter.of(achievement)));
    }

    @Override
    public void removeAchievement(Achievement achievement) {
        getHandle().offer(getHandle().getAchievementData().achievements().remove(AchievementConverter.of(achievement)));
    }

    @Override
    public boolean hasAchievement(Achievement achievement) {
        return getHandle().getAchievementData().achievements().contains(AchievementConverter.of(achievement));
    }

    @Override
    public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
        incrementStatistic(statistic, 1);
    }

    @Override
    public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
        decrementStatistic(statistic, 1);
    }

    @Override
    public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
        setStatistic(statistic, getStatistic(statistic) + amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
        incrementStatistic(statistic, -amount);
    }

    private void setStatistic(org.spongepowered.api.statistic.Statistic statistic, int newValue) {
        getHandle().offer(getHandle().getStatisticData().statistics()
                .put(statistic, (long) newValue));
    }

    @Override
    public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {
        checkNotNull(statistic, "Statistic must not be null");
        setStatistic(StatisticConverter.asStdStat(statistic), newValue);
    }

    private int getStatistic(org.spongepowered.api.statistic.Statistic statistic) {
        Long l = getHandle().getStatisticData().statistics().get().get(statistic);
        return l != null ? l.intValue() : 0;
    }

    @Override
    public int getStatistic(Statistic statistic) throws IllegalArgumentException {
        checkNotNull(statistic, "Statistic must not be null");
        checkArgument(statistic.getType() == Statistic.Type.UNTYPED, "Statistic " + statistic.toString()
                + " requires an additional parameter");
        return getStatistic(StatisticConverter.asStdStat(statistic));
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        incrementStatistic(statistic, material, 1);
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        decrementStatistic(statistic, material, 1);
    }

    @Override
    public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
        checkNotNull(statistic, "Statistic must not be null");
        checkArgument(statistic.getType() == Statistic.Type.BLOCK || statistic.getType() == Statistic.Type.ITEM,
                "Statistic " + statistic.name() + " cannot accept a Material parameter");
        StatisticGroup group = StatisticConverter.asGroupStat(statistic);
        Optional<BlockStatistic> stat =
                Pore.getGame().getRegistry().getBlockStatistic(group, MaterialConverter.asBlock(material));
        if (!stat.isPresent()) {
            throw new UnsupportedOperationException("Cannot get block statistic " + statistic.name() + " for material "
                    + material.name());
        }
        return getStatistic(stat.get());
    }

    @Override
    public void setStatistic(Statistic statistic, Material material, int newValue)
            throws IllegalArgumentException {
        checkNotNull(statistic, "Statistic must not be null");
        checkState(statistic.getType() == Statistic.Type.BLOCK,
                "Statistic " + statistic.name() + " cannot accept a Material parameter");
        StatisticGroup group = StatisticConverter.asGroupStat(statistic);
        Optional<BlockStatistic> stat =
                Pore.getGame().getRegistry().getBlockStatistic(group, MaterialConverter.asBlock(material));
        if (!stat.isPresent()) {
            throw new UnsupportedOperationException("Cannot get block statistic " + statistic.name() + " for material "
                    + material.name());
        }
        setStatistic(stat.get(), newValue);
    }

    @Override
    public void incrementStatistic(Statistic statistic, Material material, int amount)
            throws IllegalArgumentException {
        setStatistic(statistic, material, getStatistic(statistic, material) + amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, Material material, int amount)
            throws IllegalArgumentException {
        incrementStatistic(statistic, material, -amount);
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        incrementStatistic(statistic, entityType, 1);
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        decrementStatistic(statistic, entityType, 1);
    }

    @Override
    public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
        checkNotNull(statistic, "Statistic must not be null");
        checkState(statistic.getType() == Statistic.Type.ENTITY,
                "Statistic " + statistic.name() + " cannot accept an Entity parameter");
        StatisticGroup group = StatisticConverter.asGroupStat(statistic);
        Optional<EntityStatistic> stat =
                Pore.getGame().getRegistry().getEntityStatistic(group, EntityConverter.of(entityType));
        if (!stat.isPresent()) {
            throw new UnsupportedOperationException("Cannot get entity statistic " + statistic.name() + " for entity "
                    + entityType.name());
        }
        return getStatistic(stat.get());
    }

    @Override
    public void incrementStatistic(Statistic statistic, EntityType entityType, int amount)
            throws IllegalArgumentException {
        setStatistic(statistic, entityType, getStatistic(statistic, entityType) + amount);
    }

    @Override
    public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
        incrementStatistic(statistic, entityType, -amount);
    }

    @Override
    public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
        checkNotNull(statistic, "Statistic must not be null");
        checkState(statistic.getType() == Statistic.Type.ENTITY,
                "Statistic " + statistic.name() + " cannot accept an entity parameter");
        StatisticGroup group = StatisticConverter.asGroupStat(statistic);
        Optional<EntityStatistic> stat =
                Pore.getGame().getRegistry().getEntityStatistic(group, EntityConverter.of(entityType));
        if (!stat.isPresent()) {
            throw new UnsupportedOperationException("Cannot get entity statistic " + statistic.name() + " for entity "
                    + entityType.name());
        }
        setStatistic(stat.get(), newValue);
    }

    @Override
    public void setPlayerTime(long time, boolean relative) {
        throw new NotImplementedException("CANTDO");
    }

    @Override
    public long getPlayerTime() {
        return getWorld().getTime(); // TODO
    }

    @Override
    public long getPlayerTimeOffset() {
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative() {
        return true;
    }

    @Override
    public void resetPlayerTime() {
        // TODO
    }

    @Override
    public void setPlayerWeather(WeatherType type) {
        throw new NotImplementedException("CANTDO");
    }

    @Override
    public WeatherType getPlayerWeather() {
        return null;
    }

    @Override
    public void resetPlayerWeather() {
        // TODO
    }

    @Override
    public void giveExp(int amount) {
        setTotalExperience(getTotalExperience() + amount);
    }

    @Override
    public void giveExpLevels(int amount) {
        setLevel(getLevel() + amount);
    }

    @Override
    public float getExp() {
        if (!hasData(EXPERIENCE_HOLDER_DATA)) {
            return 0;
        }
        return getHandle().get(EXPERIENCE_HOLDER_DATA).get().experienceSinceLevel().get()
                / getHandle().get(EXPERIENCE_HOLDER_DATA).get().getExperienceBetweenLevels().get();
    }

    @Override
    public void setExp(float exp) {
        //int newExp = (int)
        //        (getHandle().getOrCreate(EXPERIENCE_HOLDER_DATA).get().getExperienceBetweenLevels().get() * exp);
        //TODO: setExperienceSinceLevel(newExp)
        //getHandle().get(EXPERIENCE_HOLDER_DATA).get().totalExperience();
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getLevel() {
        return getHandle().get(Keys.EXPERIENCE_LEVEL).orElse(0);
    }

    @Override
    public void setLevel(int level) {
        getHandle().offer(Keys.EXPERIENCE_LEVEL, level);
    }

    @Override
    public int getTotalExperience() {
        return getHandle().get(Keys.TOTAL_EXPERIENCE).orElse(0);
    }

    @Override
    public void setTotalExperience(int exp) {
        getHandle().offer(Keys.TOTAL_EXPERIENCE, exp);
    }

    @Override
    public float getExhaustion() {
        return getHandle().get(Keys.EXHAUSTION).get().floatValue();
    }

    @Override
    public void setExhaustion(float value) {
        getHandle().offer(Keys.EXHAUSTION, (double) value);
    }

    @Override
    public float getSaturation() {
        return getHandle().get(Keys.SATURATION).get().floatValue();
    }

    @Override
    public void setSaturation(float value) {
        getHandle().offer(Keys.SATURATION, (double) value);
    }

    @Override
    public int getFoodLevel() {
        return getHandle().get(Keys.FOOD_LEVEL).get();
    }

    @Override
    public void setFoodLevel(int value) {
        getHandle().offer(Keys.FOOD_LEVEL, value);
    }

    @Override
    public boolean isOnline() {
        return getHandle().isOnline();
    }

    @Override
    public boolean isBanned() {
        Optional<BanService> bs = Pore.getGame().getServiceManager().provide(BanService.class);
        return bs.isPresent() && bs.get().isBanned(getHandle().getProfile());
    }

    @Override
    public void setBanned(boolean banned) {
        applyBan(getHandle().getProfile(), banned);
    }

    @Override
    public boolean isWhitelisted() {
        Optional<WhitelistService> ws = Pore.getGame().getServiceManager().provide(WhitelistService.class);
        return ws.isPresent() && ws.get().isWhitelisted(getHandle().getProfile());
    }

    @Override
    public void setWhitelisted(boolean value) {
        applyWhitelisting(getHandle().getProfile(), value);
    }

    public static void applyBan(GameProfile profile, boolean banned) {
        Optional<BanService> bs = Pore.getGame().getServiceManager().provide(BanService.class);
        if (bs.isPresent()) {
            if (bs.get().isBanned(profile) != banned) {
                if (banned) {
                    bs.get().addBan(Ban.of(profile));
                } else {
                    bs.get().removeBan(bs.get().getBanFor(profile).get());
                }
            }
        }
    }

    public static void applyWhitelisting(GameProfile profile, boolean value) {
        Optional<WhitelistService> ws = Pore.getGame().getServiceManager().provide(WhitelistService.class);
        if (ws.isPresent()) {
            if (ws.get().isWhitelisted(profile) != value) {
                if (value) {
                    ws.get().addProfile(profile);
                } else {
                    ws.get().removeProfile(profile);
                }
            }
        }
    }

    @Override
    public org.bukkit.entity.Player getPlayer() {
        return this;
    }

    @Override
    public long getFirstPlayed() {
        return getHandle().get(JOIN_DATA).get().firstPlayed().get().getEpochSecond();
    }

    @Override
    public long getLastPlayed() {
        return getHandle().get(JOIN_DATA).get().lastPlayed().get().getEpochSecond();
    }

    @Override
    public boolean hasPlayedBefore() {
        return !getHandle().get(Keys.FIRST_DATE_PLAYED).equals(getHandle().get(Keys.LAST_DATE_PLAYED));
    }

    @Override
    // Taken from Craftbukkit
    public Location getBedSpawnLocation() {
        EntityPlayerMP player = (EntityPlayerMP) getHandle();
        World world = (World) player.getEntityWorld();
        BlockPos bed = player.getBedLocation();

        if (world != null && bed != null) {
            bed = EntityPlayer.getBedSpawnLocation((net.minecraft.world.World) world, bed, player.isSpawnForced());
            if (bed != null) {
                return new Location(PoreWorld.of(world), bed.getX(), bed.getY(), bed.getZ());
            }
        }
        return null;
    } // Keys.RESPAWN_LOCATIONS doesn't work

    @Override
    public void setBedSpawnLocation(Location location) {
        setBedSpawnLocation(location, false);
    }

    @Override
    // Taken from Craftbukkit
    public void setBedSpawnLocation(Location location, boolean override) {
        EntityPlayerMP player = (EntityPlayerMP) getHandle();
        if (location == null) {
            player.setSpawnPoint(null, override);
        } else {
            player.setSpawnPoint(new BlockPos(location.getBlockX(),
                    location.getBlockY(), location.getBlockZ()), override);
        }
    } // Keys.RESPAWN_LOCATIONS doesn't work

    @Override
    public void hidePlayer(org.bukkit.entity.Player player) {
        //TODO: implement this once contextual data is merged into master
    }

    @Override
    public void showPlayer(org.bukkit.entity.Player player) {
        //TODO: implement this once contextual data is merged into master
    }

    @Override
    public boolean canSee(org.bukkit.entity.Player player) {
        //TODO: implement this once contextual data is merged into master
        return true;
    }

    @Override
    public boolean isFlying() {
        return getHandle().get(Keys.IS_FLYING).get();
    }

    @Override
    public void setFlying(boolean value) {
        getHandle().offer(Keys.IS_FLYING, value);
    }

    //TODO: movement speeds and flight toggle will be included with the attributes API
    @Override
    public boolean getAllowFlight() {
        return getHandle().get(Keys.CAN_FLY).get(); // TODO
    }

    @Override
    public void setAllowFlight(boolean flight) {
        getHandle().offer(Keys.CAN_FLY, flight); // TODO
    }

    @Override // Craftbukkit multiplies/divides it by 2 for some reason ...
    public float getWalkSpeed() {
        return getHandle().get(Keys.WALKING_SPEED).get().floatValue() * 2;
    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException {
        getHandle().offer(Keys.WALKING_SPEED, (double) value / 2);
    }

    @Override
    public float getFlySpeed() {
        return getHandle().get(Keys.FLYING_SPEED).get().floatValue() * 2;
    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException {
        getHandle().offer(Keys.FLYING_SPEED, (double) value / 2);
    }

    @Override
    public void setTexturePack(String url) {
        setResourcePack(url);
    }

    @Override
    public void setResourcePack(String url) {
        try {
            getHandle().sendResourcePack(ResourcePacks.fromUri(new URI(url)));
        } catch (URISyntaxException | FileNotFoundException swallow) {
            //TODO: okay to swallow?
        }
    }

    @Override
    public Scoreboard getScoreboard() {
        return PoreScoreboard.of(getHandle().getScoreboard());
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
        getHandle().setScoreboard(((PoreScoreboard) scoreboard).getHandle());
    }

    //TODO: As far as I can tell this is unique to Bukkit, so it'll need to be handled exclusively by Pore.
    @Override
    public boolean isHealthScaled() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public double getHealthScale() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Entity getSpectatorTarget() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void setSpectatorTarget(Entity entity) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void sendTitle(String title, String subtitle) {
        getHandle().sendTitle(Title.of(PoreText.convert(title), PoreText.convert(subtitle)));
    }

    @Override
    public void resetTitle() {
        getHandle().resetTitle();
    }

    @Override
    public void setHealthScaled(boolean scale) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void setHealthScale(double scale) throws IllegalArgumentException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void sendMessage(String message) {
        getHandle().sendMessage(PoreText.convert(message));
    }

    @Override
    public void sendMessage(String[] messages) {
        Text[] texts = new Text[messages.length];
        for (int i = 0; i < messages.length; i++) {
            texts[i] = PoreText.convert(messages[i]);
        }
        this.getHandle().sendMessages(texts);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = Maps.newLinkedHashMap();
        result.put("name", getName());
        return result;
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        Pore.getGame().getChannelRegistrar().getOrCreateRaw(Pore.getPlugin(), channel).sendTo(getHandle(), buf -> {
            buf.writeBytes(message);
        }); // contained classloaders are fun ...
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public UUID getUniqueId() {
        return getHandle().getUniqueId();
    }

    @Override
    public MainHand getMainHand() {
        HandPreference handedness = getHandle().get(Keys.DOMINANT_HAND).orElse(HandPreferences.RIGHT);
        return handedness == HandPreferences.LEFT ? MainHand.LEFT : MainHand.RIGHT;
    }

    @Override
    public InventoryView openMerchant(Villager trader, boolean force) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void stopSound(Sound sound, SoundCategory category) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void stopSound(String sound, SoundCategory category) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void stopSound(String sound) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void stopSound(Sound sound) {
        throw new NotImplementedException("TODO");
    }

    // --snip craftbukkit--
    @Override
    public void spawnParticle(Particle particle, Location location, int count) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {
        spawnParticle(particle, x, y, z, count, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
        spawnParticle(particle, x, y, z, count, 0, 0, 0, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 1, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
    }
    // -- end snip --

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        ParticleConverter.spawnParticle(getHandle(), particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data);
    }

    @Override
    public org.bukkit.entity.Player.Spigot spigot() {
        return new Spigot();
    }

    public class Spigot extends org.bukkit.entity.Player.Spigot {
        @Override
        @Deprecated
        public boolean getCollidesWithEntities() {
            return PorePlayer.this.isCollidable();
        }

        @Override
        @Deprecated
        public void setCollidesWithEntities(boolean collides) {
            PorePlayer.this.setCollidable(collides);
        }

        @Override
        public void respawn() {
            PorePlayer.this.getHandle().respawnPlayer();
        }

        @Override
        public String getLocale() {
            return PorePlayer.this.getHandle().getLocale().toString();
        }
    }

}
