/*
 * Copyright (c) 2008  Dave Ray <daveray@gmail.com>
 *
 * Created on Sep 19, 2008
 */
package org.jsoar.kernel;


import static org.junit.Assert.*;

import java.util.List;

import org.jsoar.kernel.rhs.functions.AbstractRhsFunctionHandler;
import org.jsoar.kernel.rhs.functions.RhsFunctionContext;
import org.jsoar.kernel.rhs.functions.RhsFunctionException;
import org.jsoar.kernel.rhs.functions.RhsFunctionHandler;
import org.jsoar.kernel.symbols.Symbol;
import org.jsoar.tcl.SoarTclException;
import org.jsoar.tcl.SoarTclInterface;
import org.jsoar.util.adaptables.Adaptables;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ray
 */
public class WaterfallTests
{
    private Agent agent;
    private DecisionCycle decisionCycle;
    private SoarTclInterface ifc;

    private void sourceTestFile(String name) throws SoarTclException
    {
        ifc.sourceResource("/" + WaterfallTests.class.getName().replace('.', '/')  + "_" + name);
    }
    
    private void runTest(String testName, int expectedDecisions) throws SoarTclException
    {
        sourceTestFile(testName + ".soar");
        
        agent.getTrace().disableAll();
        //agent.trace.setEnabled(Category.TRACE_CONTEXT_DECISIONS_SYSPARAM, true);
        //agent.trace.setEnabled(false);
        final RhsFunctionHandler oldHalt = agent.getRhsFunctions().getHandler("halt");
        assertNotNull(oldHalt);
        
        final boolean halted[] = { false };
        final boolean failed[] = { false };
        
        agent.getRhsFunctions().registerHandler(new AbstractRhsFunctionHandler("halt") {

            @Override
            public Symbol execute(RhsFunctionContext rhsContext, List<Symbol> arguments) throws RhsFunctionException
            {
                halted[0] = true;
                return oldHalt.execute(rhsContext, arguments);
            }});
        agent.getRhsFunctions().registerHandler(new AbstractRhsFunctionHandler("failed") {

            @Override
            public Symbol execute(RhsFunctionContext rhsContext, List<Symbol> arguments) throws RhsFunctionException
            {
                halted[0] = true;
                failed[0] = true;
                return oldHalt.execute(rhsContext, arguments);
            }});
        agent.getRhsFunctions().registerHandler(new AbstractRhsFunctionHandler("succeeded") {

            @Override
            public Symbol execute(RhsFunctionContext rhsContext, List<Symbol> arguments) throws RhsFunctionException
            {
                halted[0] = true;
                failed[0] = false;
                return oldHalt.execute(rhsContext, arguments);
            }});
        
        agent.runForever();
        assertTrue(testName + " functional test did not halt", halted[0]);
        assertFalse(testName + " functional test failed", failed[0]);
        if(expectedDecisions >= 0)
        {
            assertEquals(expectedDecisions, agent.getProperties().get(SoarProperties.D_CYCLE_COUNT).intValue()); // deterministic!
        }
        
        ifc.eval("stats");
    }
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        agent = new Agent();
        this.decisionCycle = Adaptables.adapt(agent, DecisionCycle.class);
        agent.getTrace().enableAll();
        ifc = SoarTclInterface.findOrCreate(agent);
        agent.initialize();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        agent.getPrinter().flush();
        SoarTclInterface.dispose(ifc);
    }

    @Test(timeout=1000)
    public void testWaterfall() throws Exception
    {
        runTest("testWaterfall", 2);
        assertEquals(4, this.decisionCycle.e_cycle_count);
        assertEquals(5, this.decisionCycle.inner_e_cycle_count);
    }
    @Test(timeout=1000)
    public void testWaterfallUnbound() throws Exception
    {
        runTest("testWaterfallUnbound", 2);
        assertEquals(4, this.decisionCycle.e_cycle_count);
        assertEquals(5, this.decisionCycle.inner_e_cycle_count);
    }
    @Test(timeout=1000)
    public void testWaterfallFiveStates() throws Exception
    {
        runTest("testWaterfallFiveStates", 8);
        assertEquals(10, this.decisionCycle.e_cycle_count);
        assertEquals(16, this.decisionCycle.inner_e_cycle_count);
    }
    @Test(timeout=20000)
    public void testWaterfallBlocksWorldHRL() throws Exception
    {
        runTest("testBlocksWorldHRL", -1);
    }
}