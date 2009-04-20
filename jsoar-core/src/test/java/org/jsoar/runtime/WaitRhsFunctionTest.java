/*
 * (c) 2009  Soar Technology, Inc.
 *
 * Created on Apr 19, 2009
 */
package org.jsoar.runtime;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jsoar.kernel.Agent;
import org.jsoar.kernel.RunType;
import org.jsoar.kernel.SoarProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WaitRhsFunctionTest
{
    private ThreadedAgent agent;
    
    @Before
    public void setUp()
    {
        this.agent = ThreadedAgent.attach(new Agent());
        this.agent.initialize();
    }
    
    @After
    public void tearDown()
    {
        this.agent.detach();
    }

    @Test(timeout=10000)
    public void testDoesNotWaitIfAsynchInputIsReadyAndInputPhaseHasntRunYet() throws Exception
    {
        this.agent.getAgent().getProductions().loadProduction("test (state <s> ^superstate nil) --> (wait)");
        this.agent.getAgent().getInputOutput().asynchronousInputReady();
        final Object signal = new String("testDoesNotWaitIfAsynchInputIsReadyAndInputPhaseHasntRunYet");
        agent.execute(new Callable<Void>() {

            @Override
            public Void call() throws Exception
            {
                agent.runFor(2, RunType.DECISIONS);
                return null;
            }}, new CompletionHandler<Void>(){

                @Override
                public void finish(Void result)
                {
                    synchronized(signal)
                    {
                        signal.notifyAll();
                    }
                }});
        
        synchronized(signal)
        {
            signal.wait();
        }
    }
    
    @Test(timeout=10000)
    public void testWaitingPropertyIsSetAndWaitExitsWhenAsynchInputIsReady() throws Exception
    {
        this.agent.getAgent().getProductions().loadProduction("test (state <s> ^superstate nil) --> (wait)");
        
        assertFalse(this.agent.getAgent().getProperties().get(SoarProperties.WAITING));
        
        agent.runFor(2, RunType.DECISIONS);
        
        // Wait for the wait to start
        while(!this.agent.getAgent().getProperties().get(SoarProperties.WAITING))
        {
            Thread.sleep(50);
        }
        
        // Knock it out of wait with asynch input
        agent.getAgent().getInputOutput().asynchronousInputReady();
        
        // Wait for wait to stop
        while(this.agent.getAgent().getProperties().get(SoarProperties.WAITING))
        {
            Thread.sleep(50);
        }
    }
    
    @Test(timeout=10000)
    public void testWaitIsInterruptedIfAgentIsStopped() throws Exception
    {
        this.agent.getAgent().getProductions().loadProduction("test (state <s> ^superstate nil) --> (wait)");
        
        assertFalse(this.agent.getAgent().getProperties().get(SoarProperties.WAITING));
        
        agent.runForever();
        
        // Wait for the wait to start
        while(!this.agent.getAgent().getProperties().get(SoarProperties.WAITING))
        {
            Thread.sleep(50);
        }
        
        // Ask the agent to stop
        agent.stop();
        
        // Wait for wait to stop
        while(this.agent.getAgent().getProperties().get(SoarProperties.WAITING))
        {
            Thread.sleep(50);
        }
    }
    
    @Test(timeout=10000)
    public void testNoWaitIfAgentHalts() throws Exception
    {
        // The (halt) should trump the (wait)
        this.agent.getAgent().getProductions().loadProduction("test (state <s> ^superstate nil) --> (wait) (halt)");
        
        assertFalse(this.agent.getAgent().getProperties().get(SoarProperties.WAITING));
        
        final AtomicBoolean signalled = new AtomicBoolean(false);
        final Object signal = new String("testNoWaitIfAgentHalts");
        agent.execute(new Callable<Void>() {

            @Override
            public Void call() throws Exception
            {
                agent.runForever();
                return null;
            }}, new CompletionHandler<Void>(){

                @Override
                public void finish(Void result)
                {
                    synchronized(signal)
                    {
                        signal.notifyAll();
                        signalled.set(true);
                    }
                }});
        
        if(!signalled.get())
        {
            synchronized(signal)
            {
                signal.wait();
            }
        }
    }

}
