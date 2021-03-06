package me.querol.electrocraft.core.drone;

import me.querol.electrocraft.api.drone.upgrade.ICard;
import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.entites.EntityDrone;
import me.querol.electrocraft.core.network.CustomPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;

import me.querol.electrocraft.api.drone.upgrade.ICard;
import me.querol.electrocraft.core.ElectroCraft;
import me.querol.electrocraft.core.entites.EntityDrone;
import me.querol.electrocraft.core.network.CustomPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.ArrayUtils;

public class InventoryDrone implements IInventory {

    public ItemStack[] mainInventory = new ItemStack[36];
    public ItemStack[] tools = new ItemStack[3];
    public ItemStack fuel;
    private EntityDrone drone;

    public InventoryDrone(EntityDrone drone) {
        this.drone = drone;
    }

    public ItemStack[] getAllSlots() {
        return (ItemStack[]) ArrayUtils.addAll(mainInventory, tools, fuel);
    }

    @Override
    public int getSizeInventory() {
        return mainInventory.length + tools.length + 1;
    }

    @Override
    public ItemStack getStackInSlot(int var1) {
        if (var1 >= 36) {
            var1 -= 36;
            if (var1 >= 3)
                return fuel;
            else
                return tools[var1];
        }
        return mainInventory[var1];
    }

    @Override
    public ItemStack decrStackSize(int var1, int var2) {
        ItemStack returnStack = null;
        ItemStack[] slots;

        if (var1 >= 36) {
            var1 -= 36;
            if (var1 >= 3) {
                var1 = 0;
                slots = new ItemStack[] { fuel };
            } else {
                slots = tools;
            }
        } else {
            slots = mainInventory;
        }

        if (slots[var1] == null)
            return null;
        if (var2 >= slots[var1].stackSize) {
            returnStack = slots[var1].copy();
            returnStack.stackSize = var2;
            slots[var1] = null;
        } else if (var2 > 0 && var2 < slots[var1].stackSize) {
            returnStack = slots[var1].splitStack(var2);
        }
        if (slots[var1] != null && slots[var1].stackSize <= 0) {
            slots[var1] = null;
        }

        if (slots.length == 36) {
            mainInventory = slots;
        } else if (slots.length == 3) {
            tools = slots;
        } else if (slots.length == 1) {
            fuel = slots[0];
        }

        return returnStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1) {
        return getStackInSlot(var1);
    }

    @Override
    public void setInventorySlotContents(int var1, ItemStack var2) {
        ItemStack[] slots;

        if (var1 >= 36) {
            var1 -= 36;
            if (var1 >= 3) {
                var1 = 0;
                slots = new ItemStack[]{fuel};
            } else {
                slots = tools;
            }
        } else {
            slots = mainInventory;
        }
        slots[var1] = var2;
        if (slots.length == 36) {
            mainInventory = slots;
        } else if (slots.length == 3) {
            tools = slots;
        } else if (slots.length == 1) {
            fuel = slots[0];
        }
    }

    @Override
    public String getName() {
        return "me.querol.electrocraft.drone.inventory";
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public IChatComponent getDisplayName() {
        return new ChatComponentText("Drone Inventory");
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (!drone.worldObj.isRemote) {
            if (drone.getDrone() != null) {
                if (tools[0] == null) {
                    drone.getDrone().setLeftCard(null, null);
                } else if (tools[0].getItem() instanceof ICard) {
                    drone.getDrone().setLeftCard((ICard) tools[0].getItem(), tools[0]);
                }
                if (tools[2] == null) {
                    drone.getDrone().setRightCard(null, null);
                } else if (tools[2].getItem() instanceof ICard) {
                    drone.getDrone().setRightCard((ICard) tools[2].getItem(), tools[2]);
                }
            }
            CustomPacket packet = new CustomPacket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            NBTTagCompound inventory = new NBTTagCompound();
            writeToNBT(inventory);
            try {
                dos.writeInt(drone.getEntityId());
                CompressedStreamTools.write(inventory, dos);
                packet.id = 4;
                packet.data = bos.toByteArray();
                ElectroCraft.instance.getNetworkWrapper().sendToDimension(packet, drone.worldObj.provider.getDimensionId());
            } catch (IOException e) {
                ElectroCraft.instance.getLogger().fine("Error sending inventory update to entity!");
            }
        }
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.mainInventory.length; i++) {
            this.mainInventory[i] = null;
        }
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList tagList = tagCompound.getTagList("inventory", Constants.NBT.TAG_LIST);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("slot");
            if (slot >= 0 && slot < getSizeInventory()) {
                setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
            }
        }
        if (!drone.worldObj.isRemote) {
            if (tools[0] == null) {
                drone.getDrone().setLeftCard(null, null);
            } else if (tools[0].getItem() instanceof ICard) {
                drone.getDrone().setLeftCard((ICard) tools[0].getItem(), tools[0]);
            }
            if (tools[2] == null) {
                drone.getDrone().setRightCard(null, null);
            } else if (tools[2].getItem() instanceof ICard) {
                drone.getDrone().setRightCard((ICard) tools[2].getItem(), tools[2]);
            }
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        tagCompound.setTag("inventory", itemList);
    }
}
