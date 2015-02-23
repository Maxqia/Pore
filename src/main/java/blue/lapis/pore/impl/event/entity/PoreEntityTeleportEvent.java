/*
 * Pore
 * Copyright (c) 2014-2015, Lapis <https://github.com/LapisBlue>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package blue.lapis.pore.impl.event.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import blue.lapis.pore.converter.type.EntityConverter;
import blue.lapis.pore.converter.vector.LocationConverter;
import blue.lapis.pore.impl.entity.PoreEntity;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.spongepowered.api.event.SpongeEventFactory;

public class PoreEntityTeleportEvent extends EntityTeleportEvent {

    private final org.spongepowered.api.event.entity.EntityTeleportEvent handle;

    public PoreEntityTeleportEvent(org.spongepowered.api.event.entity.EntityTeleportEvent handle) {
        super(null, null, null);
        this.handle = checkNotNull(handle, "handle");
    }

    public org.spongepowered.api.event.entity.EntityTeleportEvent getHandle() {
        return this.handle;
    }

    @Override
    public Entity getEntity() {
        return PoreEntity.of(this.getHandle().getEntity());
    }

    @Override
    public EntityType getEntityType() {
        return EntityConverter.of(this.getHandle().getEntity().getType());
    }

    @Override
    public Location getFrom() {
        return LocationConverter.of(this.getHandle().getOldLocation());
    }

    @Override
    public void setFrom(Location from) {
        throw new NotImplementedException(); //TODO: eh, not sure of how to implement this
    }

    @Override
    public Location getTo() {
        return LocationConverter.of(this.getHandle().getNewLocation());
    }

    @Override
    public void setTo(Location to) {
        this.getHandle().setCancelled(true);
        this.getHandle().getEntity().setLocation(LocationConverter.of(to));
    }

    @Override
    public boolean isCancelled() {
        return this.getHandle().isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.getHandle().setCancelled(cancelled);
    }
}
