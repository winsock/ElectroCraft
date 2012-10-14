local tArgs = {...}
os = {}
local realDebug = debug
debug = nil

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
	if event == "kill" then
		print("Killed!")
		error()
	end
	if #eventTypes <= 0 then
		return event, param1, param2, param3, param4, param5
	end
	while true do
		for n, e in ipairs(eventTypes) do
			if e == event then
				return event, param1, param2, param3, param4, param5
			end
		end
		table.insert(events, { event, param1, param2, param3, param4, param5 })
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

function print(string)
	getTerminal():print(string)
end

local saveFuncs = {}

function os.registerSaveHandler(func)
	table.insert(saveFuncs, func)
end

function os.shutdown()
	getComputer():shutdown()
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

function onSave(storage)
	for i, v in ipairs(saveFuncs) do
		v(storage)
	end
end

local nativeSaveCallback = saveCallback
saveCallback = nil
nativeSaveCallback("onSave")

local runningProgram
local currentPath

local found, func, err = loadfile("/rom/programs/shell")
if err then
	error(err)
end

if #tArgs > 1 then
	local ok, err = os.run(func, tArgs[2])
	if not ok and err then
		print(err)
	end
else
	print("Booting up Cerios!\n")
	for i = 1, 20 do
		print(".")
		sleep(20)
	end
	print("\n")
	print("Welcome to Cerios!\n")
	print("To get started type help.\n")
	local ok, err = os.run(func)
	if not ok and err then
		print(err)
	end
end
os.shutdown()