package info.cerios.electrocraft.core.blocks;

import info.cerios.electrocraft.api.computer.NetworkBlock;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.network.NetworkAddressPacket;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public abstract class BlockNetwork extends ElectroBlock {

    protected BlockNetwork(Material material) {
        super(material);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        if (world.getTileEntity(x, y, z) instanceof NetworkBlock) {
            ((NetworkBlock) world.getTileEntity(x, y, z)).update((NetworkBlock) world.getTileEntity(x, y, z));
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block par5, int par6) {
        if (world.getTileEntity(x, y, z) instanceof NetworkBlock) {
            NetworkBlock block = (NetworkBlock) world.getTileEntity(x, y, z);
            if (block.getComputerNetwork() != null) {
                block.getComputerNetwork().removeDevice(block);
            }
            super.breakBlock(world, x, y, z, par5, par6);
            block.update(block);
        } else {
            super.breakBlock(world, x, y, z, par5, par6);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block nBlock) {
        super.onNeighborBlockChange(world, x, y, z, nBlock);
        if (world.getTileEntity(x, y, z) instanceof NetworkBlock) {
            ((NetworkBlock) world.getTileEntity(x, y, z)).update((NetworkBlock) world.getTileEntity(x, y, z));
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (player instanceof EntityPlayerMP) {
            if (player.isSneaking()) {
                if (Block.getIdFromBlock(world.getBlock(x, y, z)) == Block.getIdFromBlock(this)) {
                    if (world.getTileEntity(x, y, z) instanceof NetworkBlock) {
                        NetworkBlock block = (NetworkBlock) world.getTileEntity(x, y, z);
                        NetworkAddressPacket networkAddressPacket = new NetworkAddressPacket();
                        networkAddressPacket.setControlAddress(block.getControlAddress());
                        networkAddressPacket.setDataAddress(block.getDataAddress());
                        networkAddressPacket.setLocation(world.provider.dimensionId, x, y, z);
                        ElectroCraft.instance.getNetworkWrapper().sendTo(networkAddressPacket, (EntityPlayerMP) player);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
