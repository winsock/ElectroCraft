package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityWire;
import net.minecraft.src.Block;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.forge.MinecraftForge;

public class Wire extends ElectroBlock {
	
	public Wire(int itemId) {
		super(itemId, Material.cloth);
	}
	
	@Override
	protected int damageDropped(int metadata) {
		return metadata;
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
		return ElectroBlocks.WIRE.getDefaultTextureIndices()[metadata];
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileEntityWire();
	}
}
