package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.core.items.ElectroItems;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class MagnetiteOre extends Block {

    public MagnetiteOre(int blockId) {
        super(blockId, 4, Material.rock);
        this.setHardness(1.0f);
        this.setResistance(2.0f);
        this.setStepSound(soundStoneFootstep);
        MinecraftForge.setBlockHarvestLevel(this, "pickaxe", 0);
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int par3, int par4, int par5) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack == null)
            return 0f;
        if (stack.itemID == Item.pickaxeGold.shiftedIndex || stack.itemID == Item.pickaxeSteel.shiftedIndex)
            return 0f;
        return super.getPlayerRelativeBlockHardness(player, world, par3, par4, par5);
    }

    @Override
    public int quantityDropped(Random rand) {
        return rand.nextInt(5) + 2;
    }

    @Override
    public int idDropped(int par1, Random par2Random, int par3) {
        return ElectroItems.MAGNETITE_DUST.getItem().shiftedIndex;
    }

    @Override
    public String getTextureFile() {
        return "/info/cerios/electrocraft/gfx/blocks.png";
    }

    @Override
    public void addCreativeItems(ArrayList itemList) {
        itemList.add(this);
    }
}
