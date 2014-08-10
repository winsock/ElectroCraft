package info.cerios.electrocraft.core.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import info.cerios.electrocraft.core.blocks.tileentities.TileEntityRedstoneAdapter;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRedstoneAdapter extends BlockNetwork {

    private IIcon redstoneAdapterOn, redstoneAdapterOff, redstoneAdapeterSides;

    public BlockRedstoneAdapter(int id) {
        super(Material.rock);
        this.setBlockBounds(0f, 0f, 0f, 1.0f, 0.55f, 1.0f);
        Block.blockRegistry.addObject(id, "redstoneAdapter", this);
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockAccess, int par2, int par3, int par4, int par5) {
        return isProvidingStrongPower(blockAccess, par2, par3, par4, par5);
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        if (par1IBlockAccess.getTileEntity(par2, par3, par4) instanceof TileEntityRedstoneAdapter) {
            TileEntityRedstoneAdapter adapter = (TileEntityRedstoneAdapter) par1IBlockAccess.getTileEntity(par2, par3, par4);
            return adapter.getRedstonePower() ? 1 :0;
        }
        return 0;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        super.onNeighborBlockChange(world, x, y, z, block);
        if (world.getTileEntity(x, y, z) instanceof TileEntityRedstoneAdapter) {
            TileEntityRedstoneAdapter adapter = (TileEntityRedstoneAdapter) world.getTileEntity(x, y, z);
            if (world.isBlockIndirectlyGettingPowered(x, y, z) != adapter.getState()) {
                adapter.setExternalState(world.isBlockIndirectlyGettingPowered(x, y, z));
            }
        }
    }

    @Override
    public IIcon getIcon(int side, int metadata) {
        switch (side) {
            case 0:
                return redstoneAdapterOn;
            case 1:
                return redstoneAdapterOff;
        }
        return redstoneAdapeterSides;
    }

    // Needed to allow block to emit a redstone signal
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 0;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister) {
        redstoneAdapeterSides = par1IconRegister.registerIcon("electrocraft:redstoneAdapterSide");
        redstoneAdapterOn = par1IconRegister.registerIcon("electrocraft:redstoneAdapterOff");
        redstoneAdapterOff = par1IconRegister.registerIcon("electrocraft:redstoneAdapterSideOn");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int p_149915_2_) {
        return new TileEntityRedstoneAdapter();
    }
}
