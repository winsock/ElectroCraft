package me.querol.electrocraft.core.blocks;

import me.querol.electrocraft.core.items.ElectroItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import me.querol.electrocraft.core.items.ElectroItems;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MagnetiteOre extends Block {

    public MagnetiteOre() {
        super(Material.rock);
        this.setHardness(1.0f);
        this.setResistance(2.0f);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer playerIn, World worldIn, BlockPos pos) {
        ItemStack stack = playerIn.inventory.getCurrentItem();
        if (stack == null)
            return 0f;
        if (Item.getIdFromItem(stack.getItem()) == Item.getIdFromItem((Item) Item.itemRegistry.getObject("golden_pickaxe")) || Item.getIdFromItem(stack.getItem()) == Item.getIdFromItem((Item) Item.itemRegistry.getObject("iron_pickaxe")))
            return 0f;
        return super.getPlayerRelativeBlockHardness(playerIn, worldIn, pos);
    }

    @Override
    public int quantityDropped(Random rand) {
        return rand.nextInt(5) + 2;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        return (quantityDropped(random) - 2) * fortune;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ElectroItems.MAGNETITE_DUST.getItem();
    }
}
