package info.cerios.electrocraft.core.computer.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import info.luaj.vm2.LuaError;
import info.luaj.vm2.LuaTable;
import info.luaj.vm2.LuaValue;
import info.luaj.vm2.lib.OneArgFunction;
import info.luaj.vm2.lib.ZeroArgFunction;
import info.luaj.vm2.lib.jse.CoerceJavaToLua;
import info.luaj.vm2.lib.jse.JsePlatform;
import info.luaj.vm2.parser.LuaParser;
import info.luaj.vm2.parser.ParseException;

import info.cerios.electrocraft.core.computer.Computer;
import info.cerios.electrocraft.core.computer.ComputerSocket;

public class LuaCommand implements IComputerCommand {
	
	private LuaTable globalTable;
	
	public LuaCommand() {
		globalTable = JsePlatform.standardGlobals();
		globalTable.set("sleep", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				try {
					Thread.sleep(arg.checklong());
				} catch (InterruptedException e) { }
				return LuaValue.NIL;
			}
		});
	}
	
	@Override
	public void onCommand(Computer computer, int argc, String[] argv) {
		globalTable.set("getTerminal", new ZeroArgFunction() {
			Computer computer;
			
			public ZeroArgFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public LuaValue call() {
				return CoerceJavaToLua.coerce(computer.getTerminal());
			}
		}.init(computer));
		
		globalTable.set("getComputer", new ZeroArgFunction() {
			Computer computer;
			
			public ZeroArgFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public LuaValue call() {
				return CoerceJavaToLua.coerce(computer);
			}
		}.init(computer));
		
		globalTable.set("getKeyboard", new ZeroArgFunction() {
			Computer computer;
			
			public ZeroArgFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public LuaValue call() {
				return CoerceJavaToLua.coerce(computer.getKeyboard());
			}
		}.init(computer));
		
		globalTable.set("getVideoCard", new ZeroArgFunction() {
			Computer computer;
			
			public ZeroArgFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public LuaValue call() {
				return CoerceJavaToLua.coerce(computer.getVideoCard());
			}
		}.init(computer));
		
		globalTable.set("createNewSocket", new ZeroArgFunction() {
			Computer computer;
			
			public ZeroArgFunction init(Computer computer) {
				this.computer = computer;
				return this;
			}
			
			@Override
			public LuaValue call() {
				return CoerceJavaToLua.coerce(new ComputerSocket());
			}
		}.init(computer));
		
		try {
			globalTable.get("dofile").call(LuaValue.valueOf(computer.getBaseDirectory().getAbsolutePath() + File.separator + computer.getCurrentDirectory() + File.separator + argv[0]));
		} catch (LuaError e) {
			computer.getTerminal().print("Error running lua script!");
			try {
				LuaParser parser = new LuaParser(new FileInputStream(new File(argv[0])));
				parser.Chunk();
			} catch (ParseException e1) {
				computer.getTerminal().print("Parse error!");
				computer.getTerminal().print(e1.getLocalizedMessage());
			} catch (FileNotFoundException e1) {
				computer.getTerminal().print("Lua file not found!");
			}
		}
		
		// Make sure that we are in terminal mode after running the program
		computer.setGraphicsMode(false);
	}
}