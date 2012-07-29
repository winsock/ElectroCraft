package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.blocks.tileentities.ElectroTileEntity;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityElectroWire;
import info.cerios.electrocraft.core.electricity.ElectricityProvider;
import info.cerios.electrocraft.core.electricity.ElectricityReceiver;
import info.cerios.electrocraft.core.electricity.ElectricityTransporter;
import info.cerios.electrocraft.core.electricity.ElectricityTypes;
import info.cerios.electrocraft.core.electricity.ElectricBlock;
import info.cerios.electrocraft.core.utils.ObjectTriplet;
import info.cerios.electrocraft.items.ElectroItems;

import java.util.Random;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Direction;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.forge.IConnectRedstone;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForgeClient;

public class ElectroWire extends ElectroBlock implements ITextureProvider {

	public static int renderId = ModLoader.getUniqueBlockModelID(mod_ElectroCraft.instance, false);

	public ElectroWire(int id) {
		super(id, 6, Material.circuits);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
	}

	@Override
	public int idDropped(int par1, Random par2Random, int par3) {
		return ElectroItems.ELECTRO_DUST.getItem().shiftedIndex;
	}

	// Adapted from BlockRedstoneWire
	public static boolean isPowerProviderOrWire(IBlockAccess blockAccess, int x, int y, int z, int par4) {
		int blockId = blockAccess.getBlockId(x, y, z);

		if (blockId == ElectroBlocks.ELECTRO_WIRE.getBlock().blockID) {
			return true;
		} else if (blockAccess.getBlockTileEntity(x, y, z) instanceof ElectricityReceiver || blockAccess.getBlockTileEntity(x, y, z) instanceof ElectricityProvider) {
			return true;
		}

		return false;  
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileEntityElectroWire();
	}

	// ============ BEGIN COPIED STUFF FROM BlockRedstoneWire ================= //

	@Override
	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
        return par1World.isBlockSolidOnSide(par2, par3 - 1, par4, 1) || par1World.getBlockId(par2, par3 - 1, par4) == Block.glowStone.blockID;
    }

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return renderId;
	}
}
