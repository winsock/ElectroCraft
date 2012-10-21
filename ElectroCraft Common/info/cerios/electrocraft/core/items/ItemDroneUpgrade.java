package info.cerios.electrocraft.core.items;

import java.util.List;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import info.cerios.electrocraft.api.drone.upgrade.ICard;
import info.cerios.electrocraft.core.drone.Drone;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraftforge.common.ForgeDirection;

public class ItemDroneUpgrade extends Item implements ICard {
	
	public ItemDroneUpgrade(int id) {
		super(id);
		setHasSubtypes(true);
	}

	@Override
	public String getItemNameIS(ItemStack stack) {
		switch(stack.getItemDamage()) {
		case 0:
			return "info.cerios.electrocraft.items.gyro";
		case 1:
			return "info.cerios.electrocraft.items.gps";
		case 2:
			return "info.cerios.electrocraft.items.anaylzer";
		case 3:
			return "info.cerios.electrocraft.items.engine";
		case 4:
			return "info.cerios.electrocraft.items.ai";
		}
		return "";
	}
	
	@Override
	public String getName(ItemStack stack) {
		switch(stack.getItemDamage()) {
		case 0:
			return "gyro";
		case 1:
			return "gps";
		case 2:
			return "anaylzer";
		case 3:
			return "engine";
		case 4:
			return "ai";
		}
		return "";
	}

	@Override
	public NamedJavaFunction[] getFunctions(ItemStack stack, Drone drone) {
		switch(stack.getItemDamage()) {
		case 0:
			return new NamedJavaFunction[] {
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							ForgeDirection dir = drone.getDirection(drone.getDrone().rotationYaw);
							luaState.pushInteger(dir.ordinal());
							return 1;
						}

						@Override
						public String getName() {
							return "getForgeDir";
						}
					}.init(drone),
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							luaState.pushInteger(drone.getDir(drone.getDrone().rotationYaw));
							return 1;
						}

						@Override
						public String getName() {
							return "getDir";
						}
					}.init(drone),
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							luaState.pushNumber(drone.getDrone().rotationYaw);
							return 1;
						}

						@Override
						public String getName() {
							return "getRotation";
						}
					}.init(drone),
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							drone.getDrone().rotate((float) luaState.checkNumber(-1), 10);
							return 0;
						}

						@Override
						public String getName() {
							return "rotate";
						}
					}.init(drone),
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							if (luaState.isBoolean(-1) && luaState.checkBoolean(-1, false)) {
								drone.getDrone().setPositionAndRotation2(drone.getDrone().posX, drone.getDrone().posY, drone.getDrone().posZ, drone.getRotation(ForgeDirection.getOrientation(luaState.checkInteger(-2))), drone.getDrone().rotationPitch, 10);
							} else {
								drone.getDrone().setPositionAndRotation2(drone.getDrone().posX, drone.getDrone().posY, drone.getDrone().posZ, drone.getRotation(drone.getDirection(luaState.checkInteger(-1))), drone.getDrone().rotationPitch, 10);
							}
							return 0;
						}

						@Override
						public String getName() {
							return "face";
						}
					}.init(drone),
			};
		case 1:
			return new NamedJavaFunction[] {
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							luaState.pushNumber(Math.floor(drone.getDrone().posX));
							luaState.pushNumber(Math.floor(drone.getDrone().posY));
							luaState.pushNumber(Math.floor(drone.getDrone().posZ));
							return 3;
						}

						@Override
						public String getName() {
							return "getLocation";
						}
					}.init(drone),
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							luaState.pushNumber(drone.getDrone().posX - Math.floor(drone.getDrone().posX));
							luaState.pushNumber(drone.getDrone().posY - Math.floor(drone.getDrone().posY));
							luaState.pushNumber(drone.getDrone().posZ - Math.floor(drone.getDrone().posZ));
							return 3;
						}

						@Override
						public String getName() {
							return "getOffset";
						}
					}.init(drone),	
			};
		case 2:
			return new NamedJavaFunction[] {
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							ForgeDirection dir = drone.getDirection(drone.getDrone().rotationYaw);
							luaState.pushInteger(drone.getDrone().worldObj.getBlockId((int)(dir.offsetX + drone.getDrone().posX), (int)(dir.offsetY + drone.getDrone().posY), (int)(dir.offsetZ + drone.getDrone().posZ)));
							return 1;
						}

						@Override
						public String getName() {
							return "sampleFront";
						}
					}.init(drone),
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							ForgeDirection dir = ForgeDirection.getOrientation(luaState.checkInteger(-1));
							int x = (int)(Math.floor(drone.getDrone().posX) + dir.offsetX);
							int y = (int)(Math.floor(drone.getDrone().posY) + dir.offsetY);
							int z = (int)(Math.floor(drone.getDrone().posZ) + dir.offsetZ);
							luaState.pushInteger(drone.getDrone().worldObj.getBlockId(x, y, z));
							luaState.pushInteger(drone.getDrone().worldObj.getBlockMetadata(x, y, z));
							return 2;
						}

						@Override
						public String getName() {
							return "sample";
						}
					}.init(drone),	
			};
		case 3:
			return new NamedJavaFunction[] {
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							double dx = luaState.checkNumber(-3) - drone.getDrone().posX;
							double dy = luaState.checkNumber(-2) - drone.getDrone().posY;
							double dz = luaState.checkNumber(-1) - drone.getDrone().posZ;
							drone.getDrone().moveEntity(dx, dy, dz);
							return 0;
						}

						@Override
						public String getName() {
							return "moveTo";
						}
					}.init(drone),
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							drone.setFlying(luaState.checkBoolean(-1));
							return 0;
						}

						@Override
						public String getName() {
							return "fly";
						}
					}.init(drone),
			};
		case 4:
			return new NamedJavaFunction[] {
					new NamedJavaFunction() {
						Drone drone;

						public NamedJavaFunction init(Drone drone) {
							this.drone = drone;
							return this;
						}

						@Override
						public int invoke(LuaState luaState) {
							luaState.pushBoolean(drone.getDrone().getNavigator().tryMoveToXYZ(luaState.checkNumber(-4), luaState.checkNumber(-3), luaState.checkNumber(-2), MathHelper.clamp_float((float) luaState.checkNumber(-1), 0f, 1f)));
							return 1;
						}

						@Override
						public String getName() {
							return "move";
						}
					}.init(drone),	
			};
		}
		return null;
	}

	@Override
	public void passiveFunctionTick(ItemStack stack) {
		switch(stack.getItemDamage()) {
			
		}
	}
	
	@Override
	public void getSubItems(int id, CreativeTabs tab, List list) {
		list.add(new ItemStack(id, 1, 0));
		list.add(new ItemStack(id, 1, 1));
		list.add(new ItemStack(id, 1, 2));
		list.add(new ItemStack(id, 1, 3));
		list.add(new ItemStack(id, 1, 4));
	}
	
	@Override
    public String getTextureFile() {
        return "/info/cerios/electrocraft/gfx/items.png";
    }
}
