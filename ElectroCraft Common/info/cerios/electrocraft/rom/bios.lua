local tArgs = {...}
debug = nil
pluto = nil
os.getTerminal():clear()

local nativeYield = coroutine.yield or nativeYeild
local events = {}
function os.waitForEvent(...)
	local eventTypes = {...}
	if #eventTypes > 0 then
		for i, v in ipairs(events) do
			for n, e in ipairs(eventTypes) do
				if e == v[1] then
					table.remove(events, i)
					return unpack(v)
				end
			end
		end
	end
	local event, param1, param2, param3, param4, param5 = nativeYield("safe")
	if event and event == "kill" then
		print("Killed!")
		error()
	end
	if (#eventTypes <= 0) and event then
		return event, param1, param2, param3, param4, param5
	end
	while true do
		for n, e in ipairs(eventTypes) do
			if e == event then
				return event, param1, param2, param3, param4, param5
			end
		end
		if event and (event ~= "killyield" or event ~= "start") then
			table.insert(events, { event, param1, param2, param3, param4, param5 })
		end
		event, param1, param2, param3, param4, param5 = nativeYield("safe")
		if event == "kill" then
			print("Killed!")
			error()
		end
	end
	return event, param1, param2, param3, param4, param5
end

function coroutine.yield(...)
	return os.waitForEvent(...)
end

local nativeGetMetatable = getmetatable
function getmetatable(t)
	if t and type(t) == "string" then
		error("Tried to acesses the string metatable!")
		return nil
	end
	return nativeGetMetatable(t)
end

function loadfile(f)
	local fileHandle = file.createNewFileHandle(f)
	if not fileHandle then
		return false, nil, "Error making file handle!"
	end
	if not fileHandle:exists() then
		return false, nil, "File does not exist!"
	end
	local reader = fileHandle:open("r")
	local func, err = loadstring(reader:readAll(), fileHandle:getName())
	return true, func, err
end

function dofile(file, ...)
	local found, func, err = loadfile(file)
	if err then
		return false, nil
	else
		return os.run(func, ...)
	end
end

function write(string)
	os.getTerminal():print(string)
end

function print(...)
	local strings = {...}
	for i=1,#strings do
		os.getTerminal():printLine(strings[i])
	end
end

local saveFuncs = {}

function os.registerSaveHandler(func)
	table.insert(saveFuncs, func)
end

function os.shutdown()
	os.getComputer():shutdown()
end

function os.run(func, ...)
	local co = coroutine.create(func)
	local ok, param = coroutine.resume(co, ...)
	while coroutine.status(co) ~= "dead" do
		if not param then
			ok, param = coroutine.resume(co, nativeYield())
		else
			ok, param = coroutine.resume(co, nativeYield(param))
		end
		if not ok then
			break
		end
	end
	if not ok and param then
		return ok, param
	end
	return ok, nil
end

if drone ~= nil then
	drone.forgeDir = {
		up = 1, down = 0, north = 2, south = 3, east = 5, west = 4, unknown = 6
	}

	drone.dir = {
		north = 0, west = 1, south = 2, east = 3, unknown = 4
	}

	drone.turnDir = {
		left = 0, right = 1, arround = 2
	}

	local nativeUseTool = drone.useTool
	function drone.useTool(...)
		nativeUseTool(...)
		event, result = os.waitForEvent("tool")
		return result
	end

	function drone.turn(turn)
		if gyro == nil then
			return
		end
		gyro.face(gyro.getDir())
		if turn == drone.turnDir.left then
			gyro.rotate(gyro.getRotation() - 90)
		elseif turn == drone.turnDir.right then
			gyro.rotate(gyro.getRotation() + 90)
		elseif turn == drone.turnDir.arround then
			gyro.rotate(gyro.getRotation() - 180)
		end
		gyro.face(gyro.getDir())
	end
end

function onSave(storage)
	for i, v in ipairs(saveFuncs) do
		v(storage)
	end
end

kbrd = {}
kbrd.keycodes = {
	up = os.getKeyboard().upScanCode, down = os.getKeyboard().downScanCode, left = os.getKeyboard().leftScanCode, right = os.getKeyboard().rightScanCode,
	ctrl = os.getKeyboard().ctrlScanCode, backspace = os.getKeyboard().backspaceScanCode
}

local nativeSaveCallback = os.saveCallback
saveCallback = nil
nativeSaveCallback("onSave")

local runningProgram
local currentPath

local found, func, err = loadfile("/rom/programs/shell")
if err then
	error(err)
end

if #tArgs > 1 and tArgs[1] == "resume" then
	local ok, err = os.run(func, tArgs[1], tArgs[2])
	if not ok and err then
		print(err)
	end
else
	write("Booting up Cerios!")
	for i = 1, 20 do
		write(".")
		os.sleep(20)
	end
	write("\n")
	print("Welcome to Cerios!")
	print("To get started type help.")
	local ok, err = os.run(func)
	if not ok and err then
		print(err)
	end
end

write("Shutting down!")
for i = 1, 20 do
	write(".")
	os.sleep(20)
end
print("\nGoodbye!")

os.shutdown()