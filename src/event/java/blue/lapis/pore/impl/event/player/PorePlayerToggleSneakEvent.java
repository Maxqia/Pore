/*
 * PoreRT - A Bukkit to Sponge Bridge
 *
 * Copyright (c) 2016, Maxqia <https://github.com/Maxqia> AGPLv3
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

package blue.lapis.pore.impl.event.player;

import static com.google.common.base.Preconditions.checkNotNull;
import blue.lapis.pore.event.PoreEvent;
import blue.lapis.pore.event.PoreEventRegistry;
//import blue.lapis.pore.event.RegisterEvent;
import blue.lapis.pore.impl.entity.PorePlayer;

import com.google.common.collect.ImmutableList;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;

public final class PorePlayerToggleSneakEvent extends PlayerToggleSneakEvent implements PoreEvent<ChangeDataHolderEvent.ValueChange> {

    private final ChangeDataHolderEvent.ValueChange handle;
    private final ImmutableValue<?> changedData;

    public PorePlayerToggleSneakEvent(ChangeDataHolderEvent.ValueChange handle, ImmutableValue<?> changedData) {
        super(null, false);
        this.handle = checkNotNull(handle, "handle");
        this.changedData = checkNotNull(changedData, "changedData");
    }

    public ChangeDataHolderEvent.ValueChange getHandle() {
        return handle;
    }

    @Override
    public org.bukkit.entity.Player getPlayer() {
        return PorePlayer.of((Player) handle);
    }

    @Override
    public boolean isSneaking() {
        return (Boolean) changedData.get();
    } // casted to boolean object, not type, otherwise there are compile errors

    @Override
    public boolean isCancelled() {
        return getHandle().isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        getHandle().setCancelled(cancel);
    }

    @Override
    public String toString() {
        return toStringHelper()
                .add("changedData", this.changedData)
                .toString();
    }

    //@RegisterEvent TODO this event isn't implemented :(
    public static void register() {
        PoreEventRegistry.register(PorePlayerToggleSneakEvent.class, ChangeDataHolderEvent.ValueChange.class, event -> {
            ImmutableList.Builder<PorePlayerToggleSneakEvent> builder = ImmutableList.builder();
            if (event.getTargetHolder() instanceof Player) {
                for (ImmutableValue<?> data : event.getEndResult().getSuccessfulData()) {
                    if (data.getKey().equals(Keys.IS_SNEAKING)) {
                        builder.add(new PorePlayerToggleSneakEvent(event, data));
                    }
                }
            }
            return builder.build();
        });
    }

}
