/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.abstrys.consolekit;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eron
 */
public class TerminalInput implements TerminalListener
{
    char ch;
    boolean removeCharFromInput;
    String line;
    TerminalPanel panel;

    public TerminalInput(TerminalPanel panel)
    {
        this.panel = panel;
        panel.setTerminalListener(this);
        removeCharFromInput = false;
    }
    
    public boolean terminalCharTyped(char ch)
    {
        this.ch = ch;
        return removeCharFromInput;
    }
    
    public void terminalLineTyped(String line)
    {
        this.line = line;
    }
    
    /**
     * This function blocks until a character has been typed.
     * @param remove remove character from the input stream?
     * @return the character entered.
     */
    public char getChar(boolean remove)
    {
        removeCharFromInput = remove;
        panel.showPrompt(true);
        this.ch = '\0';
        while(this.ch == '\0')
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(TerminalInput.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
        panel.showPrompt(false);
        return this.ch;
    }

    public char getChar()
    {
        return getChar(true);
    }
    
    /**
     * This function blocks until a line has been typed.
     * @return the string entered.
     */
    public String getLine()
    {
        removeCharFromInput = false;
        panel.showPrompt(true);
        this.line = null;
        while(this.line == null)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(TerminalInput.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
        panel.showPrompt(false);
        return this.line;
    }    
}
