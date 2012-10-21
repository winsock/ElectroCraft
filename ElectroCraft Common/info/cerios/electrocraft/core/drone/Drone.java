package info.cerios.electrocraft.core.drone;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import cpw.mods.fml.common.network.PacketDispatcher;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Packet32EntityLook;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import info.cerios.electrocraft.core.ElectroCraft;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.entites.EntityDrone;
import info.cerios.electrocraft.core.network.CustomPacket;

public class Drone extends Computer {
	
	private EntityDrone drone;
	private boolean flying = false;
	
	public Drone(List<EntityPlayer> clients, String script, String baseDirectory, boolean isInternal, int width, int height, int rows, int columns) {
		super(clients, script, baseDirectory, isInternal, width, height, rows, columns);
	}
	
	public void setDrone(EntityDrone drone) {
		this.drone = drone;
	}
	
	@Override
	public void loadBios() {
		super.loadBios();
		synchronized (luaStateLock) {
			try {
				luaState.load(this.getClass().getResourceAsStream("/info/cerios/electrocraft/rom/drone/dronebios.lua"), "drone_bios_" + this.getBaseDirectory().getName());
				luaState.newThread();
				luaState.setField(LuaState.REGISTRYINDEX, "electrocraft_coroutine_drone");
			} catch (IOException e) {
				ElectroCraft.instance.getLogger().severe("Error loading drone BIOS!");
			}
		}
	}
	
	@Override
	public void loadLuaDefaults() {
		super.loadLuaDefaults();
		
		NamedJavaFunction[] droneAPI = new NamedJavaFunction[] {
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						ForgeDirection dir;
						if (luaState.isNumber(-1)) {
							dir = ForgeDirection.getOrientation(luaState.checkInteger(-1));
						} else {
							dir = getDirection(drone.drone.rotationYaw);
						}
						drone.drone.moveEntity(dir.offsetX, dir.offsetY, dir.offsetZ);
						return 0;
					}

					@Override
					public String getName() {
						return "move";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						double dx = luaState.checkNumber(-3) - drone.drone.posX;
						double dy = luaState.checkNumber(-2) - drone.drone.posY;
						double dz = luaState.checkNumber(-1) - drone.drone.posZ;
						drone.drone.moveEntity(dx, dy, dz);
						return 0;
					}

					@Override
					public String getName() {
						return "moveTo";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						flying = luaState.checkBoolean(-1);
						return 0;
					}

					@Override
					public String getName() {
						return "fly";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						luaState.pushBoolean(drone.drone.getNavigator().tryMoveToXYZ(luaState.checkNumber(-4), luaState.checkNumber(-3), luaState.checkNumber(-2), MathHelper.clamp_float((float) luaState.checkNumber(-1), 0f, 1f)));
						return 1;
					}

					@Override
					public String getName() {
						return "aiMove";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						if (drone.drone.getRotationTicks() <= 0) {
							if (luaState.isNumber(-1)) {
								drone.drone.setDigDirection(ForgeDirection.getOrientation(luaState.checkInteger(-1)));
							}
							drone.drone.setRotationTicks(60);
							CustomPacket packet = new CustomPacket();
							packet.id = 5;
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							DataOutputStream dos = new DataOutputStream(bos);
							try {
								dos.writeInt(drone.drone.entityId);
								dos.writeInt(drone.drone.getRotationTicks());
								packet.id = 5;
								packet.data = bos.toByteArray();
								PacketDispatcher.sendPacketToAllAround(drone.drone.posX, drone.drone.posY, drone.drone.posZ, 20, drone.drone.worldObj.provider.dimensionId, packet.getMCPacket());
							} catch (IOException e) {
								ElectroCraft.instance.getLogger().fine("Error sending tool use update to entity!");
							}
						}
						return 0;
					}

					@Override
					public String getName() {
						return "useTool";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						ForgeDirection dir = getDirection(drone.drone.rotationYaw);
						luaState.pushInteger(drone.drone.worldObj.getBlockId((int)(dir.offsetX + drone.drone.posX), (int)(dir.offsetY + drone.drone.posY), (int)(dir.offsetZ + drone.drone.posZ)));
						return 1;
					}

					@Override
					public String getName() {
						return "sampleFront";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						ForgeDirection dir = getDirection(drone.drone.rotationYaw);
						luaState.pushInteger(dir.ordinal());
						return 1;
					}

					@Override
					public String getName() {
						return "getForgeDir";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						luaState.pushInteger(getDir(drone.drone.rotationYaw));
						return 1;
					}

					@Override
					public String getName() {
						return "getDir";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						ForgeDirection dir = ForgeDirection.getOrientation(luaState.checkInteger(-1));
						int x = (int)(Math.floor(drone.drone.posX) + dir.offsetX);
						int y = (int)(Math.floor(drone.drone.posY) + dir.offsetY);
						int z = (int)(Math.floor(drone.drone.posZ) + dir.offsetZ);
						luaState.pushInteger(drone.drone.worldObj.getBlockId(x, y, z));
						luaState.pushInteger(drone.drone.worldObj.getBlockMetadata(x, y, z));
						return 2;
					}

					@Override
					public String getName() {
						return "sample";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						luaState.pushNumber(drone.drone.rotationYaw);
						return 1;
					}

					@Override
					public String getName() {
						return "getRotation";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						drone.drone.setPositionAndRotation2(drone.drone.posX, drone.drone.posY, drone.drone.posZ, (float) luaState.checkNumber(-1), drone.drone.rotationPitch, 10);
						return 0;
					}

					@Override
					public String getName() {
						return "rotate";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						if (luaState.isBoolean(-1) && luaState.checkBoolean(-1, false)) {
							drone.drone.setPositionAndRotation2(drone.drone.posX, drone.drone.posY, drone.drone.posZ, getRotation(ForgeDirection.getOrientation(luaState.checkInteger(-2))), drone.drone.rotationPitch, 10);
						} else {
							drone.drone.setPositionAndRotation2(drone.drone.posX, drone.drone.posY, drone.drone.posZ, getRotation(getDirection(luaState.checkInteger(-1))), drone.drone.rotationPitch, 10);
						}
						return 0;
					}

					@Override
					public String getName() {
						return "face";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						luaState.pushNumber(Math.floor(drone.drone.posX));
						luaState.pushNumber(Math.floor(drone.drone.posY));
						luaState.pushNumber(Math.floor(drone.drone.posZ));
						return 3;
					}

					@Override
					public String getName() {
						return "getLocation";
					}
				}.init(this),
				new NamedJavaFunction() {
					Drone drone;

					public NamedJavaFunction init(Drone drone) {
						this.drone = drone;
						return this;
					}

					@Override
					public int invoke(LuaState luaState) {
						luaState.pushNumber(drone.drone.posX - Math.floor(drone.drone.posX));
						luaState.pushNumber(drone.drone.posY - Math.floor(drone.drone.posY));
						luaState.pushNumber(drone.drone.posZ - Math.floor(drone.drone.posZ));
						return 3;
					}

					@Override
					public String getName() {
						return "getOffset";
					}
				}.init(this),
		};
		this.getLuaState().register("drone", droneAPI);
		luaState.pop(1);
	}
	
	public boolean getFlying() {
		return flying;
	}
	
	public int getDir(float rotation) {
		return MathHelper.floor_double((double)((rotation <= 0 ? (Math.abs(rotation) + 180) : (rotation)) * 4.0F / 360.0F) + 0.5D) & 3;
	}
	
	public ForgeDirection getDirection(float rotation) {
		return getDirection(getDir(rotation));
	}
	
	public ForgeDirection getDirection(int direction) {
		switch (direction) {
		case 0:
			return ForgeDirection.NORTH;
		case 1:
			return ForgeDirection.WEST;
		case 2:
			return ForgeDirection.SOUTH;
		case 3:
			return ForgeDirection.EAST;
		default:
			return ForgeDirection.UNKNOWN;
		}
	}
	
	public float getRotation(ForgeDirection direction) {
		switch (direction) {
		case NORTH:
			return 0f;
		case WEST:
			return 90f;
		case SOUTH:
			return 180f;
		case EAST:
			return 270f;
		default:
			return 0f;
		}
	}
}
