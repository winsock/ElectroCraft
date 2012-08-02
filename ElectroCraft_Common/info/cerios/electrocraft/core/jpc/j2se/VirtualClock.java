/*
    JPC: A x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.0

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007-2009 Isis Innovation Limited

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

    Details (including contact information) can be found at: 

    www-jpc.physics.ox.ac.uk
*/

package info.cerios.electrocraft.core.jpc.j2se;

import info.cerios.electrocraft.core.jpc.emulator.*;
import info.cerios.electrocraft.core.jpc.support.Clock;
import java.io.*;
import java.util.PriorityQueue;
import java.util.logging.*;

import info.cerios.electrocraft.core.jpc.emulator.processor.Processor;

/**
 * 
 * @author Ian Preston
 */
public class VirtualClock extends AbstractHardwareComponent implements Clock
{
    private static final Logger LOGGING = Logger.getLogger(VirtualClock.class.getName());
    private PriorityQueue<Timer> timers;
    private volatile boolean ticksEnabled;
    private long ticksOffset;
    private long ticksStatic;
    private long currentTime;
    private long totalTicks = 0;
    private static final boolean REALTIME = false; //sync clock with real clock
    private long ips = 50000000; // "Clock Speed" In instructions per second

    public VirtualClock()
    {
        timers = new PriorityQueue<Timer>(20); // initial capacity to be revised
        ticksEnabled = false;
        ticksOffset = 0;
        ticksStatic = 0;
        currentTime = getSystemTimer();
    }
    
    private long getNSPI() {
    	return 10*1000000000L/ips;
    }
    
    // Get the CPU "Clock Speed" in instructions per second
	public long getIPS() {
		return ips;
	}
	
    // Set the CPU "Clock Speed" in instructions per second
	public void setIPS(long ips) {
		this.ips = ips;
	}
    
    public void saveState(DataOutput output) throws IOException
    {
        output.writeBoolean(ticksEnabled);
        output.writeLong(ticksOffset);
        output.writeLong(getTime());
    }

    public void loadState(DataInput input, PC pc) throws IOException
    {
        ticksEnabled = input.readBoolean();
        ticksOffset = input.readLong();
        ticksStatic = input.readLong();
    }

    public synchronized Timer newTimer(TimerResponsive object)
    {
        Timer tempTimer = new Timer(object, this);
        return tempTimer;
    }

    private boolean process()
    {
        Timer tempTimer;
        tempTimer = timers.peek();
        if ((tempTimer == null) || !tempTimer.check(getTime()))
            return false;
        else
            return true;
    }

    public synchronized void update(Timer object)
    {
        timers.remove(object);
        if (object.enabled())
        {
            timers.offer(object);
        }
    }

    public long getTime()
    {
        if (ticksEnabled)
        {
            return this.getRealTime() + ticksOffset;
        } else
        {
            return ticksStatic;
        }
    }

    private long getRealTime()
    {
        return currentTime;
    }

    public long getTickRate()
    {
        return ips*10;
    }

    public long getTicks() {
        return totalTicks;
    }

    public void pause()
    {
        if (ticksEnabled)
        {
            ticksStatic = getTime();
            ticksEnabled = false;
        }
    }

    public void resume()
    {
        if (!ticksEnabled)
        {
            ticksOffset = ticksStatic - getRealTime();
            ticksEnabled = true;
        }
    }

    public void reset()
    {
            this.pause();
            ticksOffset = 0;
            ticksStatic = 0;
    }

    public String toString()
    {
        return "Virtual Clock";
    }

    private long getSystemTimer()
    {
        return System.nanoTime();
    }

    public void updateNowAndProcess() {
        if (REALTIME) {
            currentTime = getSystemTimer();
            if (process())
            {
                return;
            }

            Timer tempTimer;
            synchronized (this)
            {
                tempTimer = timers.peek();
            }
            long expiry = tempTimer.getExpiry();
            try
            {
                Thread.sleep(Math.min((expiry - getTime()) / 1000000, 100));
            } catch (InterruptedException ex)
            {
                Logger.getLogger(VirtualClock.class.getName()).log(Level.SEVERE, null, ex);
            }
            totalTicks += (expiry - ticksOffset - currentTime)/getNSPI();
            currentTime = getSystemTimer();

            tempTimer.check(getTime());
        } else {
            Timer tempTimer;
            synchronized (this)
            {
                tempTimer = timers.peek();
            }
            long expiry = tempTimer.getExpiry();
            try
            {
                Thread.sleep(Math.min((expiry - getTime()) / 1000000, 100));
            } catch (InterruptedException ex)
            {
                Logger.getLogger(VirtualClock.class.getName()).log(Level.SEVERE, null, ex);
            }
            totalTicks += (expiry - ticksOffset - currentTime)/getNSPI();
            currentTime = expiry -ticksOffset;
            //System.out.println("New time during HALT: " + (expiry - ticksOffset));
            tempTimer.check(getTime());
        }
    }

    public void updateAndProcess(int instructions)
    {
        totalTicks += instructions;
        if (REALTIME)
            currentTime = getSystemTimer();
        else
            currentTime += instructions * getNSPI();
        process();
    }
}