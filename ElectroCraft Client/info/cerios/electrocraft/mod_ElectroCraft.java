package info.cerios.electrocraft;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import info.cerios.electrocraft.blocks.render.BlockRenderers;
import info.cerios.electrocraft.computer.ComputerClient;
import info.cerios.electrocraft.computer.ComputerHandler;
import info.cerios.electrocraft.core.ConfigHandler;
import info.cerios.electrocraft.core.IElectroCraftMod;
import info.cerios.electrocraft.core.IMinecraftMethods;
import info.cerios.electrocraft.core.blocks.BlockHandler;
import info.cerios.electrocraft.core.blocks.ElectroBlocks;
import info.cerios.electrocraft.core.computer.IComputerHandler;
import info.cerios.electrocraft.core.computer.XECInterface;
import info.cerios.electrocraft.core.computer.XECInterface.AssembledData;
import info.cerios.electrocraft.core.items.ElectroItems;
import info.cerios.electrocraft.core.items.ItemHandler;
import info.cerios.electrocraft.core.network.*;
import info.cerios.electrocraft.core.network.ElectroPacket.Type;
import info.cerios.electrocraft.core.network.GuiPacket.Gui;
import info.cerios.electrocraft.gui.GuiComputerScreen;
import info.cerios.electrocraft.gui.GuiNetworkAddressScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.src.*;
import net.minecraft.src.forge.MinecraftForgeClient;
import net.minecraft.src.forge.NetworkMod;
import net.minecraft.src.forge.oredict.OreDictionary;
import net.minecraft.src.forge.oredict.ShapelessOreRecipe;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class mod_ElectroCraft extends NetworkMod implements IElectroCraftMod {

    public static mod_ElectroCraft instance;
    private ComputerHandler computerHandler;
    private IMinecraftMethods minecraftMethods;
    private boolean lastShiftState = false;
    private XECInterface xECComputer;
    private ComputerClient client;

    public mod_ElectroCraft() {
        instance = this;
    }

    @Override
    public String getName() {
        return "ElectroCraft";
    }

    @Override
    public String getVersion() {
        return "ElectroCraft In-Dev 0.2";

    }

    @Override
    public void load() {
        // Create my minecraft proxy class
        minecraftMethods = new MinecraftMethods();

        // Create and load the config
        ConfigHandler.loadOrCreateConfigFile("default.cfg");

        // Create the item handler
        ItemHandler itemHandler = new ItemHandler();

        // Create and register the ore handler
        BlockHandler oreHandler = new BlockHandler(itemHandler);
        OreDictionary.registerOreHandler(oreHandler);

        // Register our items and blocks
        //*NOTE* THIS POSISTION IS IMPORTANT, IT HAS TO GO AFTER ORE HANDLER REGISTRATION *NOTE*
        oreHandler.registerBlocks();
        itemHandler.registerItems();

        // Register the recipes
        registerBaseRecipes();

        // Preload the Graphics
        MinecraftForgeClient.preloadTexture("/info/cerios/electrocraft/gfx/blocks.png");
        MinecraftForgeClient.preloadTexture("/info/cerios/electrocraft/gfx/items.png");

        // Register for on tick events
        ModLoader.setInGameHook(this, true, false);

        ModLoader.registerPacketChannel(this, "electrocraft");

        // Create our xEC computer
        xECComputer = new XECInterface();
        xECComputer.createCPU();
        String testAssembly = "; A Simple program that changes the display buffer randomly\n" +
                ".code\n" +
                "rand:\n" +
                "mov eax, 0 ; move the lower limit of the random function to eax\n" +
                "randi eax, 2000000 ; call the random function with the range of 0-255 and store the result in eax\n" +
                "mov ecx, [0x8000] ; Get the value at 0x8000 The value there is the size of the display buffer\n" +
                "mov edx, [0x8004] ; Get the display buffer address\n" +
                "add ecx, edx ; Add the lower limit of the diplay buffer\n" +
                "randi edx, ecx ; get a random address between 0x8004 and the value of the size of the display buffer\n" +
                "mov [edx], eax ; set the pixel at the random address\n" +
                "call sleep ; Sleep the program to prevent eating up CPU cycles\n" +
                "jmp rand ; And jump back to the begining and repeat\n" +
                "sleep:\n" +
                "mov cx, 100 ; Loop 100 times\n" +
                "sleeploop:\n" +
                "nop ; No instruction\n" +
                "loop sleeploop ; Loop while cx > 0\n" +
                "ret ; return to the caller\n";

        String testAssembly2 =
                "; Puts random text on the terminal buffer\n" +
                        "mov edx, [0x1010008]\n" +
                        "resetLoop:\n" +
                        "mov ecx, [0x1010000]\n" +
                        "mul ecx, [0x1010004]\n" +
                        "push edx\n" +
                        "main:\n" +
                        "add edx, 0x1\n" +
                        "mov eax, 33\n" +
                        "randi eax, 127\n" +
                        "mov [edx], eax\n" +
                        "loop main\n" +
                        "pop edx\n" +
                        "jmp resetLoop\n";

        String testAssembly3 =
                "main:\n" +
                        "mov ecx, 0\n" +
                        "mov ebx [0x1010000]\n" +
                        "mul ebx [0x1010004]\n" +
                        "input:\n" +
                        "inp eax, 0x122\n" +
                        "cmp eax, 0\n" +
                        "je input\n" +
                        "mov edx, [0x1010008]\n" +
                        "add edx, ecx\n" +
                        "mov [edx], eax\n" +
                        "cmp ecx, ebx\n" +
                        "je main\n" +
                        "add ecx, 1\n" +
                        "jmp input\n";

        String testComplexAssembly =
                ".code\n" +
                        "mov ecx, [0x1010008]\n" +
                        "main:\n" +
                        "push ecx\n" +
                        "call processKey\n" +
                        "pop emc\n" +
                        "cmp emc, 1\n" +
                        "je remove\n" +
                        "cmp emc, 2\n" +
                        "je doEnter\n" +
                        "add ecx, 1\n" +
                        "jmp main\n" +
                        "remove:\n" +
                        "sub ecx, 1\n" +
                        "mov [ecx], 0\n" +
                        "jmp main\n" +
                        "doEnter:\n" +
                        "add ecx, [0x1010000]\n" +
                        "jmp main\n" +

                        "processKey:\n" +
                        "push ebp\n" +
                        "mov ebp, esp\n" +
                        "sub esp, 4 ; Allocate 4 bytes to store result\n" +
                        "call getKey\n" +
                        "pop ebx\n" +
                        "cmp ebx, 8\n" +
                        "je backspace\n" +
                        "cmp ebx, 0xA\n" +
                        "je enter\n" +
                        "cmp ebx, 0xD\n" +
                        "je enter\n" +
                        "cmp ebx, 0\n" +
                        "je end\n" +
                        "push ebx\n" +
                        "push [ebp + 8]\n" +
                        "call displayKey\n" +
                        "add esp, 8\n" +
                        "mov [ebp + 8], 0\n" +
                        "jmp end\n" +
                        "enter:\n" +
                        "mov [ebp + 8] , 2\n" +
                        "jmp end\n" +
                        "backspace:\n" +
                        "mov [ebp + 8], 1\n" +
                        "end:\n" +
                        "mov esp, ebp\n" +
                        "pop ebp\n" +
                        "ret\n" +

                        "getKey:\n" +
                        "push ebp\n" +
                        "mov ebp, esp\n" +
                        "inp [ebp + 8], 0x122\n" +
                        "mov esp, ebp\n" +
                        "pop ebp\n" +
                        "ret\n" +

                        "displayKey:\n" +
                        "push ebp\n" +
                        "mov ebp, esp\n" +
                        "mov [ebp + 8], [ebp + 12]\n" +
                        "mov esp, ebp\n" +
                        "pop ebp\n" +
                        "ret\n";

        String dbTest =
                ".data\n" +
                        "string db \"hello world how is it going today?\", 0, 1, 0\n" +
                        ".code\n" +
                        "mov eax, [0x1010008]\n" +
                        "mov [eax], [string + 4]\n" +
                        "loop:\n" +
                        "nop\n" +
                        "jmp loop\n";

        AssembledData data = xECComputer.assemble(dbTest);
        long baseAddress = xECComputer.loadIntoMemory(data.data, data.length, data.codeStart);
        xECComputer.start(baseAddress);

        // Create the computer handler
        this.computerHandler = new ComputerHandler();
    }

    @Override
    public boolean onTickInGame(float time, Minecraft minecraftInstance) {
        computerHandler.update();

        try {
            if (FMLClientHandler.instance().getClient().isMultiplayerWorld()) {
                // Shift Packet
                if (lastShiftState != (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {
                    lastShiftState = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                    ShiftPacket shiftPacket = new ShiftPacket();
                    shiftPacket.setShiftState(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
                    FMLClientHandler.instance().sendPacket(shiftPacket.getMCPacket());
                }
            }
        } catch (IOException e) {
            FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to send packet!");
        }
        return true;
    }

    @Override
    public void receiveCustomPacket(Packet250CustomPayload packet) {
        ElectroPacket ecPacket;
        try {
            ecPacket = ElectroPacket.readMCPacket(packet);

            if (ecPacket.getType() == Type.GUI) {
                GuiPacket guiPacket = (GuiPacket) ecPacket;
                if (guiPacket.closeWindow())
                    minecraftMethods.closeGui();
                else if (guiPacket.getGui() == Gui.COMPUTER_SCREEN) {
                    FMLClientHandler.instance().getClient().displayGuiScreen(new GuiComputerScreen());
                }
            } else if (ecPacket.getType() == Type.ADDRESS) {
                NetworkAddressPacket networkPacket = (NetworkAddressPacket) ecPacket;
                FMLClientHandler.instance().getClient().displayGuiScreen(new GuiNetworkAddressScreen(networkPacket));
            } else if (ecPacket.getType() == Type.PORT) {
                ServerPortPacket portPacket = (ServerPortPacket) ecPacket;
                try {
                    try {
                        client = new ComputerClient(1337, (SocketAddress) ModLoader.getPrivateValue(NetworkManager.class, (NetworkManager) ModLoader.getPrivateValue(NetClientHandler.class, ((EntityClientPlayerMP) FMLClientHandler.instance().getClient().thePlayer).sendQueue, "netManager"), "remoteSocketAddress"));
                        new Thread(client).start();
                    } catch (RuntimeException e) {
                        client = new ComputerClient(1337, (SocketAddress) ModLoader.getPrivateValue(NetworkManager.class, (NetworkManager) ModLoader.getPrivateValue(NetClientHandler.class, ((EntityClientPlayerMP) FMLClientHandler.instance().getClient().thePlayer).sendQueue, "g"), "i"));
                        new Thread(client).start();
                    }
                } catch (UnknownHostException e) {
                    FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft ComputerClient: Unable to find remote host!");
                } catch (IOException e) {
                    FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft ComputerClient: Unable to connect to remote host!");
                }
            }
        } catch (IOException e) {
            FMLCommonHandler.instance().getFMLLogger().severe("ElectroCraft: Unable to read packet sent on our channel!");
        }
    }

    @Override
    public void generateSurface(World world, Random random, int chunkX, int chunkZ) {
        for (int i = 0; i < 20; i++) { // Rarity
            // Copper
            int randPosX = chunkX + random.nextInt(16);
            int randPosY = random.nextInt(128); // Min Height
            int randPosZ = chunkZ + random.nextInt(16);
            new WorldGenMinable(ElectroBlocks.COPPER_ORE.getBlock().blockID, 10/* Vein Size */).generate(world, random, randPosX, randPosY, randPosZ);
            // Magnetite
            randPosX = chunkX + random.nextInt(16);
            randPosY = random.nextInt(128); // Min Height
            randPosZ = chunkZ + random.nextInt(16);
            new WorldGenMinable(ElectroBlocks.MAGNETITE_ORE.getBlock().blockID, 10/* Vein Size */).generate(world, random, randPosX, randPosY, randPosZ);
        }
    }

    @Override
    public boolean renderWorldBlock(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, int modelID) {
        for (BlockRenderers renderBlock : BlockRenderers.values()) {
            if (renderBlock.getBlock().getBlock().blockID == block.blockID)
                return renderBlock.getRenderer().render(block, x, y, z);
        }
        return false;
    }

    private void registerBaseRecipes() {
        CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(ElectroItems.ELECTRO_DUST.getItem(), 4), Item.redstone, "magnetiteDust", "magnetiteDust", "magnetiteDust"));
        ModLoader.addRecipe(new ItemStack(ElectroBlocks.COMPUTER.getBlock()), "III", "ICI", "IEI", 'E', ElectroItems.ELECTRO_DUST.getItem(), 'I', Item.ingotIron, 'C', Item.compass);
        ModLoader.addRecipe(new ItemStack(ElectroBlocks.REDSTONE_ADAPTER.getBlock()), "III", "ICI", "IRI", 'R', Item.redstone, 'I', Item.ingotIron, 'C', Block.thinGlass);
        ModLoader.addRecipe(new ItemStack(ElectroBlocks.RIBBON_CABLE.getBlock(), 16), "WWW", "III", "WWW", 'W', Block.cloth, 'I', Item.ingotIron);
    }

    public IComputerHandler getComputerHandler() {
        return computerHandler;
    }

    public ComputerClient getClient() {
        return client;
    }

    public XECInterface getComputer() {
        return xECComputer;
    }

    @Override
    public boolean isShiftHeld() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @Override
    public IMinecraftMethods getSidedMethods() {
        return minecraftMethods;
    }
}
