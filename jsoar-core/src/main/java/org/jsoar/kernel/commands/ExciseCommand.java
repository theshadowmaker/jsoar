/*
 * Copyright (c) 2009 Dave Ray <daveray@gmail.com>
 */
package org.jsoar.kernel.commands;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jsoar.kernel.Agent;
import org.jsoar.kernel.Production;
import org.jsoar.kernel.ProductionManager;
import org.jsoar.kernel.ProductionType;
import org.jsoar.kernel.SoarException;
import org.jsoar.util.commands.SoarCommand;

/**
 * http://winter.eecs.umich.edu/soarwiki/Excise
 * 
 * @author ray
 */
public final class ExciseCommand implements SoarCommand
{
    private final Agent agent;

    public ExciseCommand(Agent agent)
    {
        this.agent = agent;
    }

    @Override
    public String execute(String[] args) throws SoarException
    {
        final Set<Production> toExcise = new LinkedHashSet<Production>();
        final ProductionManager pm = agent.getProductions();
        boolean doInit = false;
        for(int i = 1; i < args.length; ++i)
        {
            final String arg = args[i];
            if("-a".equals(arg) || "--all".equals(arg))
            {
                toExcise.addAll(pm.getProductions(null));
                doInit = true;
            }
            else if("-c".equals(arg) || "--chunks".equals(arg))
            {
                toExcise.addAll(pm.getProductions(ProductionType.CHUNK));
            }
            else if("-d".equals(arg) || "--default".equals(arg))
            {
                toExcise.addAll(pm.getProductions(ProductionType.DEFAULT));
            }
            else if("-r".equals(arg) || "--rl".equals(arg))
            {
                for(Production p : pm.getProductions(ProductionType.DEFAULT))
                {
                    if(p.rl_rule)
                    {
                        toExcise.add(p);
                    }
                }
            }
            else if("-t".equals(arg) || "--task".equals(arg))
            {
                toExcise.addAll(pm.getProductions(ProductionType.CHUNK));
                toExcise.addAll(pm.getProductions(ProductionType.JUSTIFICATION));
                toExcise.addAll(pm.getProductions(ProductionType.USER));
            }
            else if("-T".equals(arg) || "--templates".equals(arg))
            {
                toExcise.addAll(pm.getProductions(ProductionType.TEMPLATE));
            }
            else if("-u".equals(arg) || "--user".equals(arg))
            {
                toExcise.addAll(pm.getProductions(ProductionType.USER));
            }
            else
            {
                final Production p = pm.getProduction(arg);
                if(p == null)
                {
                    throw new SoarException("No production named '" + arg + "'");
                }
                toExcise.add(p);
            }
        }

        for(Production p : toExcise)
        {
            pm.exciseProduction(p, false);
        }
        
        // If -a is given, we do an init-soar as well
        if(doInit)
        {
            agent.initialize();
        }
        
        agent.getPrinter().startNewLine().print("%d production%s excise.", toExcise.size(), toExcise.size() == 1 ? "" : "s");
        return "";
    }
}