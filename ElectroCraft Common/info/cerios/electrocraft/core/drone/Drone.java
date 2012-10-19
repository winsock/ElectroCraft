package info.cerios.electrocraft.core.drone;

import java.util.List;

import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Vec3;
import net.minecraftforge.common.ForgeDirection;
import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.entites.EntityDrone;

public class Drone extends Computer {
	
	private EntityDrone drone;
	
	public Drone(List<EntityPlayer> clients, String script, String baseDirectory, boolean isInternal, int width, int height, int rows, int columns) {
		super(clients, script, baseDirectory, isInternal, width, height, rows, columns);
	}
	
	public void setDrone(EntityDrone drone) {
		this.drone = drone;
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
						luaState.pushBoolean(drone.drone.getNavigator().tryMoveToXYZ(luaState.checkNumber(-4), luaState.checkNumber(-3), luaState.checkNumber(-2), (float) luaState.checkNumber(-1)));
						return 1;
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
						ForgeDirection dir = ForgeDirection.getOrientation(MathHelper.floor_double((double)(drone.drone.rotationYawHead * 4.0F / 360.0F) + 0.5D) & 3);
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
						ForgeDirection dir = ForgeDirection.getOrientation(luaState.checkInteger(-1));
						luaState.pushInteger(drone.drone.worldObj.getBlockId((int)(dir.offsetX + drone.drone.posX), (int)(dir.offsetY + drone.drone.posY), (int)(dir.offsetZ + drone.drone.posZ)));
						return 1;
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
						luaState.pushNumber(MathHelper.floor_double((double)(drone.drone.rotationYawHead * 4.0F / 360.0F) + 0.5D) & 3);
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
						luaState.pushNumber(drone.drone.rotationYawHead);
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
						drone.drone.rotationYawHead = (float) luaState.checkNumber(-1);
						return 0;
					}

					@Override
					public String getName() {
						return "rotate";
					}
				}.init(this),
		};
		this.getLuaState().register("drone", droneAPI);
	}
}
