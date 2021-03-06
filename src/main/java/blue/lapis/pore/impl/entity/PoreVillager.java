/*
 * Pore(RT)
 * Copyright (c) 2014-2016, Lapis <https://github.com/LapisBlue>
 * Copyright (c) 2014-2016, Contributors
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

package blue.lapis.pore.impl.entity;

import blue.lapis.pore.converter.type.entity.ProfessionConverter;
import blue.lapis.pore.converter.wrapper.WrapperConverter;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.MerchantRecipe;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.entity.living.Villager;

import java.util.List;

public class PoreVillager extends PoreAgeable implements org.bukkit.entity.Villager {

    public static PoreVillager of(Villager handle) {
        return WrapperConverter.of(PoreVillager.class, handle);
    }

    protected PoreVillager(Villager handle) {
        super(handle);
    }

    @Override
    public Villager getHandle() {
        return (Villager) super.getHandle();
    }

    @Override
    public EntityType getType() {
        return EntityType.VILLAGER;
    }

    @Override
    public Profession getProfession() {
        return ProfessionConverter.of(getHandle().getCareerData().type().get().getProfession());
    }

    @Override
    public void setProfession(Profession profession) {
        Career career = Iterables.getFirst(ProfessionConverter.of(profession).getCareers(), Careers.FARMER);
        assert career != null;
        getHandle().getCareerData().type().set(career);
    }

    @Override
    public List<MerchantRecipe> getRecipes() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void setRecipes(List<MerchantRecipe> recipes) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public MerchantRecipe getRecipe(int i) throws IndexOutOfBoundsException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void setRecipe(int i, MerchantRecipe recipe) throws IndexOutOfBoundsException {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getRecipeCount() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public Inventory getInventory() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public boolean isTrading() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public HumanEntity getTrader() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public int getRiches() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void setRiches(int riches) {
        throw new NotImplementedException("TODO");
    }
}
