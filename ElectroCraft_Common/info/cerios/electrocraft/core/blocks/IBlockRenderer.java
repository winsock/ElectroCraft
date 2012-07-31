package info.cerios.electrocraft.core.blocks;

import net.minecraft.src.Block;

public interface IBlockRenderer {
	public boolean render(Block block, int x, int y, int z);
}
