package info.cerios.electrocraft.core.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import info.cerios.electrocraft.core.items.ElectroItems;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class MagnetiteOre extends Block {

    public MagnetiteOre(int blockId) {
        super(Material.rock);
        this.setHardness(1.0f);
        this.setResistance(2.0f);
        this.setHarvestLevel("pickaxe", 0);
        Block.blockRegistry.addObject(blockId, "magnetiteOre", this);
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int par3, int par4, int par5) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack == null)
            return 0f;
        if (Item.getIdFromItem(stack.getItem()) == Item.getIdFromItem((Item) Item.itemRegistry.getObject("pickaxeGold")) || Item.getIdFromItem(stack.getItem()) == Item.getIdFromItem((Item) Item.itemRegistry.getObject("pickaxeSteel")))
            return 0f;
        return super.getPlayerRelativeBlockHardness(player, world, par3, par4, par5);
    }

    @Override
    public int quantityDropped(Random rand) {
        return rand.nextInt(5) + 2;
    }

    @Override
    public Item getItemDropped(int par1, Random par2Random, int par3) {
        return ElectroItems.MAGNETITE_DUST.getItem();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister) {
        this.blockIcon = par1IconRegister.registerIcon("electrocraft:magnetiteOre");
    }
}
