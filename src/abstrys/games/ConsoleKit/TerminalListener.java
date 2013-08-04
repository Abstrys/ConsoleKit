/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.abstrys.consolekit;

/**
 * Provides an interface for listening for typed character input or whole-line
 * input in a terminal panel.
 * @author v-eronh
 */
public interface TerminalListener
{
    /**
     * Called when a character has been typed in a terminal.  The character can be removed from the input stream if this function returns true.  This keeps it from re-appearing in terminalLineTyped().
     * @param ch the character typed.
     * @return true if the character should be consumed (removed from line input)
     */
    public boolean terminalCharTyped(char ch);
    
    /**
     * Called when a newline has been typed in a terminal.
     * @param str a string containing all the characters typed since the last
     * newline.
     */
    public void terminalLineTyped(String str);
}
