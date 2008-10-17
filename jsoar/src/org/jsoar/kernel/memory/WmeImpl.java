/*
 * (c) 2008  Dave Ray
 *
 * Created on Aug 15, 2008
 */
package org.jsoar.kernel.memory;

import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.Iterator;

import org.jsoar.kernel.GoalDependencySet;
import org.jsoar.kernel.io.OutputLink;
import org.jsoar.kernel.rete.RightMemory;
import org.jsoar.kernel.rete.Token;
import org.jsoar.kernel.symbols.Identifier;
import org.jsoar.kernel.symbols.IdentifierImpl;
import org.jsoar.kernel.symbols.Symbol;
import org.jsoar.kernel.symbols.SymbolImpl;

import com.google.common.collect.Iterators;

/**
 * Internal implementation of {@link Wme} interface.
 * 
   <p>Fields in a WME:
   <ul>
   <li>id, attr, value:  points to symbols for the wme fields

      acceptable:  TRUE iff this is an acceptable pref. wme

      timetag:  timetag of the wme

      reference count:  (see below)

      rete_next, rete_prev:  pointers in the doubly-linked list of all
         wmes currently known to the rete (header is all_wmes_in_rete)
         (this equals WM except while WM is being changed)

      right_mems:  header of a doubly-linked list of right memory entries
         (in one or more alpha memories containing the wme).  This is used
         only by the Rete, as part of list-based remove.

      tokens:  header of a doubly-linked list of tokens in the Rete.
         This is used only by the Rete, as part of list-based remove.

      next, prev:  pointers in a doubly-linked list of wmes.
         Depending on the wme type, the header of this DLL is:
           - slot.wmes (for ordinary wmes)
           - slot.acceptable_preference_wmes (for acceptable pref. wmes)
           - id.impasse_wmes (for architecture-created goal/impasse wmes)
           - id.input_wmes (for Soar I/O wmes)

      preference:  points to the preference supporting the wme.  For I/O
         wmes and (most) architecture-created wmes, this is NIL.

      output_link:  this is used only for top-state output links.
         It points to an output_link structure used by the I/O routines.

      grounds_tc, potentials_tc, locals_tc:  used by the chunker to indicate
         whether this wme is in the grounds, potentials, and/or locals sets

      chunker_bt_pref: used by the chunker; set to cond->bt.trace when
         a wme is added to either the potentials or locals set

      These are the additions to the WME structure that will be used
         to track dependencies for goals.  Each working memory element
     now includes a pointer  to a gds_struct (defined below) and
     pointers to other WMEs on the same GDS.

      gds: the goal dependency set the wme is in
      gds_next, gds_prev:  used for dll of all wmes in gds

      If a particular working memory element is not dependent for any goal,
     then the values for these pointers will all be NIL. If a WME is
     dependent for more than one goal, then it will point to the GDS
     of the highest goal.

   Reference counts on wmes:
      +1 if the wme is currently in WM
      +1 for each instantiation condition that points to it (bt.wme)
   We deallocate a wme when its reference count goes to 0.

 * 
 * <p>wmem.h:125:wme
 * <p>The following fields or functions were removed because they were unused or
 * unnecessary in Java:
 * <ul>
 * </ul>
 * 
 * @author ray
 */
public class WmeImpl implements Wme, Formattable
{
    public final IdentifierImpl id;
    public final SymbolImpl attr;
    public final SymbolImpl value;
    public final boolean acceptable;
    public final int timetag;
    
    private int reference_count;
    
    private RightMemory right_mems; // used for dll of rm's it's in
    public Token tokens = null; // dll of tokens in rete
    
    /**
     * next/previous pointers for lists this WME is a member of. 
     * 
     * <p>Possible list heads are:
     * <ul>
     * <li>IdentifierImpl.impasse_wmes
     * <li>IdentifierImpl.input_wmes
     * <li>Slot.wmes
     * <li>Slot.acceptable_preference_wmes
     * </ul>
     */
    public WmeImpl next;
    private WmeImpl previous;
    
    public Preference preference;     // pref. supporting it, or null
    public OutputLink output_link;   /* for top-state output commands */
    
    public int grounds_tc;                     /* for chunker use only */
    public int potentials_tc;
    public int locals_tc;
    
    public Preference chunker_bt_pref;
    
    public GoalDependencySet gds;
    private WmeImpl gds_next, gds_prev; // part of dll of wmes in gds
    
    /**
     * @param id
     * @param attr
     * @param value
     * @param acceptable
     * @param timetag
     */
    public WmeImpl(IdentifierImpl id, SymbolImpl attr, SymbolImpl value, boolean acceptable, int timetag)
    {
        this.id = id;
        this.attr = attr;
        this.value = value;
        this.acceptable = acceptable;
        this.timetag = timetag;
    }

    /**
     * <p>wmem.h:160:wme_add_ref
     */
    public void wme_add_ref()
    {
        reference_count++;
    }
    
    public void wme_remove_ref(WorkingMemory wm)
    {
      /* There are occaisionally wme's with zero reference counts 
         created in the system. Make sure this function handles them 
         correctly. */
      if (reference_count != 0) { reference_count--; }
      if (reference_count == 0) { wm.deallocate_wme(this); }
    }
    
    /**
     * Retrieve a field by index, 0 = id, 1 = attr, 2 = value.
     * 
     * <p>rete.cpp:273:field_from_wme
     * 
     * @param field The field index
     * @return The field
     * @throws IllegalArgumentException if index is not 0, 1, or 2
     */
    public SymbolImpl getField(int field)
    {
        switch(field)
        {
        case 0: return id;
        case 1: return attr;
        case 2: return value;
        }
        throw new IllegalArgumentException("field_num must be 0, 1, or 2, got" + field);
    } 
    
    /**
     * Test if this WME is a member of a WME list
     * 
     * @param head The head of the list to search using {@link #next}/{@link #previous}
     *      list pointers.
     * @return true if this WME is a member of the given list
     */
    public boolean isMemberOfList(WmeImpl head)
    {
        for(WmeImpl w = head; w != null; w = w.next)
        {
            if(w == this)
            {
                return true;
            }
        }
        return false;
    }
    
    public WmeImpl addToList(WmeImpl head)
    {
        next = head;
        previous = null;
        if(head != null)
        {
            head.previous = this;
        }
        return this;
    }
    
    public WmeImpl removeFromList(WmeImpl head)
    {
        if(next != null)
        {
            next.previous = previous;
        }
        if(previous != null)
        {
            previous.next = next;
        }
        else
        {
            head = next;
        }
        next = null;
        previous = null;
        
        return head;
    }
    
    public WmeImpl addToGds(WmeImpl head)
    {
        gds_next = head;
        gds_prev = null;
        if(head != null)
        {
            head.gds_prev = this;
        }
        return this;
    }
    
    public WmeImpl removeFromGds(WmeImpl head)
    {
        if(gds_next != null)
        {
            gds_next.gds_prev = gds_prev;
        }
        if(gds_prev != null)
        {
            gds_prev.gds_next = gds_next;
        }
        else
        {
            head = gds_next;
        }
        gds_next = null;
        gds_prev = null;
        
        return head;
    }    
    
    /**
     * @return An iterator over the {@link #next} pointer starting at this WME
     */
    public Iterator<Wme> iterator()
    {
        return new WmeIterator(this);
    }
    
    public RightMemory getRightMemories()
    {
        return right_mems;
    }
    
    public void clearnRightMemories()
    {
        right_mems= null;
    }
    
    public void addRightMemory(RightMemory rm)
    {
        right_mems = rm.addToWme(right_mems);
    }
    
    public void removeRightMemory(RightMemory rm)
    {
        right_mems = rm.removeFromWme(right_mems);
    }
    
    /* (non-Javadoc)
     * @see org.jsoar.kernel.memory.Wme#getAttribute()
     */
    @Override
    public Symbol getAttribute()
    {
        return attr;
    }

    /* (non-Javadoc)
     * @see org.jsoar.kernel.memory.Wme#getIdentifier()
     */
    @Override
    public Identifier getIdentifier()
    {
        return id;
    }

    /* (non-Javadoc)
     * @see org.jsoar.kernel.memory.Wme#getTimetag()
     */
    @Override
    public int getTimetag()
    {
        return timetag;
    }

    /* (non-Javadoc)
     * @see org.jsoar.kernel.memory.Wme#getValue()
     */
    @Override
    public Symbol getValue()
    {
        return value;
    }

    /* (non-Javadoc)
     * @see org.jsoar.kernel.memory.Wme#getChildren()
     */
    @Override
    public Iterator<Wme> getChildren()
    {
        Identifier valueAsId = value.asIdentifier();
        return valueAsId != null ? valueAsId.getWmes() : Iterators.<Wme>emptyIterator();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "<" + id + ", " + attr + ", " + value + ">:" + timetag;
    }

    /* (non-Javadoc)
     * @see java.util.Formattable#formatTo(java.util.Formatter, int, int, int)
     */
    @Override
    public void formatTo(Formatter fmt, int f, int width, int precision)
    {
        // print.cpp:981:print_wme
        // print.cpp:981:print_wme_without_timetag
        
        if((f & FormattableFlags.ALTERNATE) == 0)
        {
            // This is the normal print_wme case. It is specified with the 
            // usual %s format string
            fmt.format("(%d: %s ^%s %s%s)\n", timetag, id, attr, value, acceptable ? " +" : "");
        }
        else
        {
            // This is the print_wme_without_timetag case
            // It is specified with the %#s format string.
            fmt.format("(%s ^%s %s%s)\n", id, attr, value, acceptable ? " +" : "");
        }

        // <wme tag="123" id="s1" attr="foo" attrtype="string" val="123" valtype="string"></wme>
        // TODO xml_object( thisAgent, w );
    }
}