package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.mod_ElectroCraft;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityStaticGenerator;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;

public class StaticGenerator extends ElectroBlock {

	public StaticGenerator(int id) {
		super(id, 14, Material.iron);
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileEntityStaticGenerator();
	}
}
