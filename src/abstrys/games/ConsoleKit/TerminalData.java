package com.abstrys.consolekit;

import java.awt.Point;
import java.util.ArrayList;
import java.nio.CharBuffer;

/**
 * A class that stores character data for the TerminalPanel class.
 * @author Eron Hennessey
 */
public class TerminalData
{
    /**
     * Constants used to define wrap behavior for the TerminalData class.
     */
    public enum WrapBehavior
    {
        NONE, CHAR, WORD
    };
    
    private int width;
    private int height;
    private ArrayList<CharBuffer> lines;
    private int cursorX;
    private int cursorY;
    private int tabSize;
    private WrapBehavior wrapBehavior;

    /**
     * Constructs a new TerminalData object.
     */
    public TerminalData()
    {
        this(80, 100, 4, WrapBehavior.WORD);
    }

    /**
     * Constructs a new TerminalData object.
     * @param w the width of the data field, in characters
     * @param h the height of the data field, in characters
     */
    public TerminalData(int w, int h)
    {
        this(w, h, 4, WrapBehavior.WORD);        
    }
    
    /**
     * Constructs a new TerminalData object.
     * @param w the width of the data field, in characters
     * @param h the height of the data field, in characters
     * @param ts the tab-stop width.
     * @param wb the wrap behavior.
     */
    public TerminalData(int w, int h, int ts, WrapBehavior wb)
    {
        this.width = w;
        this.height = h;
        this.lines = new ArrayList<CharBuffer>();
        this.cursorX = 0;
        this.cursorY = 0;
        this.tabSize = ts;
        this.wrapBehavior = wb;
    }

    private void addLine()
    {
        CharBuffer line = CharBuffer.allocate(this.width);
        lines.add(line);

        // If this new line causes the array to grow larger than the height of
        // the data array, remove the top line.
        while(lines.size() > this.height)
        {
            lines.remove(0);
            if(cursorY > 0)
            {
                cursorY--;
            }
        }
    }

    /**
     * Adds a character array to the end of the data.
     * @param ca the character array to add.  If this is larger in size than
     * this.width, it will be truncated.
     */
    private void addLine(char[] ca)
    {
        addLine();
        lines.get(lines.size() - 1).put(ca, 0, (ca.length < width) ? ca.length : width);
    }

    /**
     * Gets the character at the specified position in the data array.
     * @param x the cartesian x position of the character to get
     * @param y the cartesian y position of the character to get
     * @return the character at the position defined by x, y.  If either x or y
     * are out of bounds, the null character will be returned.
     */
    public char getChar(int x, int y)
    {
        if (lines.size() >= y)
        {
            return lines.get(y).charAt(x);
        }

        return '\0';
    }

    /**
     * Sets the character at the position given.
     * @param ch the character to set.
     * @param x the cartesian x coordinate to set.
     * @param y the cartesian y coordinate to set.
     * @return true if the character could be set, or false if any of the
     * coordinates are out of range.
     */
    public boolean setChar(char ch, int x, int y)
    {
        if (x < 0 || x > width || y < 0 || y > height)
        {
            return false;
        }

        while (y >= lines.size())
        {
            addLine();
        }

        lines.get(y).put(x, ch);
        return true;
    }

    /**
     * Fills a rectangular region with a character.  Out of range values are
     * clipped to the bounds of the data dimensions.
     * @param ch the character to fill the region with
     * @param x the cartesian x coordinate of the top-left portion of the region
     * to fill.
     * @param y the cartesian y coordinate of the top-left portion of the region
     * to fill.
     * @param w the width of the region to fill
     * @param h the height of the region to fill
     * coordinates are invalid or out of range.
     */
    public void fill(char ch, int x, int y, int w, int h)
    {
        if (x >= width || y >= height)
        {
            return;
        }

        if (x < 0)
        {
            x = 0;
        }
        if (y < 0)
        {
            y = 0;
        }

        if (x + w > width)
        {
            w = width - x;
        }
        if (y + h > height)
        {
            h = height - y;
        }

        char[] ca = new char[w];
        for (int i = 0; i < w; i++)
        {
            ca[i] = ch;
        }

        for (int i = y; i < y + h; i++)
        {
            lines.get(i).put(ca, x, w);
        }
    }

    /**
     * Sets a line in the field to the given character array.
     * @param ca the character array to set
     * @param y the cartesian y coordinate of the line to set.
     * @return true if the line could be set, or false if y is out of range.
     */
    public boolean setLine(char[] ca, int y)
    {
        if (y < 0 || y >= height)
        {
            return false;
        }

        while (y >= lines.size())
        {
            addLine();
        }

        CharBuffer cb = CharBuffer.allocate(width);
        cb.put(ca, 0, ((ca.length < width) ? ca.length : width));
        lines.set(y, cb);
        return true;
    }

    /**
     * Gets the character array that represents the line at the position passed
     * in.
     * @param y the cartesian y coordinate of the line to retrieve.
     * @return the character array at position y.  If y is out of range, null
     * is returned.
     */
    public char[] getLine(int y)
    {
        if (y >= height)
        {
            return null;
        }

        while (y >= lines.size())
        {
            addLine();
        }

        return lines.get(y).array();
    }
    
    /**
     * Returns the number of lines currently in the data.
     * @return the line count
     */
    public int getLineCount()
    {
        return lines.size();
    }

    /**
     * Clears all lines in the buffer.
     */
    public void clear()
    {
        for (CharBuffer cb : lines)
        {
            cb.clear();
        }
    }

    /**
     * Clears a range of lines in the buffer.
     * @param y0 the first line in the range to clear.  This must be less than
     * the value of y1.
     * @param y1 the last line in the range to clear.  This must be greater
     * than the value of y0.
     */
    public void clear(int y0, int y1)
    {
        if (y0 < 0 || y0 > y1 || y0 > lines.size())
        {
            return;
        }

        if (y1 >= lines.size())
        {
            y1 = lines.size() - 1;
        }

        for (int i = y0; i <= y1; i++)
        {
            lines.get(i).clear();
        }
    }

    /**
     * Sets the wrap behavior for printing.
     * @param wb the type of wrapping behavior that should be used when printed
     * text extends beyond the end of the current line.
     */
    public void setWrapBehavior(WrapBehavior wb)
    {
        wrapBehavior = wb;
    }

    public Point getCursor()
    {
        return new Point(this.cursorX, this.cursorY);
    }
    
    /**
     * Sets the current cursor position.  All printing will continue from this
     * position.  If the position provided is out of the bounds of the width and
     * height of the data set, it will be constrained to the dimensions of the
     * data.
     * @param x the cartesian x coordinate of the location to begin printing.
     * @param y the cartesian y coordinate of the location to begin printing.
     */
    public void setCursor(int x, int y)
    {
        if(x < 0)
        {
            x = 0;
        }
        if(y < 0)
        {
            y = 0;
        }
        if(x >= width)
        {
            x = width-1;
        }
        if(y >= height)
        {
            y = height-1;
        }
        
        this.cursorX = x;
        this.cursorY = y;
    }

    /**
     * Prints text beginning at the current cursor position.  If the cursor is
     * not at the end of the data, existing data will be over-written by the
     * new text. If the text is too long to fit on the current line, it will
     * wrap according to the wrap behavior set by setWrapBehavior.  The cursor
     * position will be updated to point to the position after the last printed
     * character.  If the last character is a newline, the cursor will be placed
     * at the beginning of the following line.
     * @param s a string containing the text to print.
     */
    public void print(String s)
    {
        int spos = 0;
        
        while(spos < s.length())
        {
            // scroll down as much as needed
            while(cursorY >= lines.size())
            {
                addLine();
            }

            // is the cursor at the end of the line?
            if(cursorX >= width)
            {
                if(wrapBehavior == WrapBehavior.NONE)
                {
                    // Check for a newline character at the current position.
                    // If one exists, advance the cursor to the next line.
                    // Otherwise, ignore the current character.
                    if(s.charAt(spos) == '\n')
                    {
                        cursorX = 0;
                        cursorY++;
                    }
                    else
                    {
                        spos++;
                        continue;
                    }
                }
                else if(wrapBehavior == WrapBehavior.CHAR)
                {
                    // Advance the cursor to the beginning of the next line
                    // regardless of what type of character it is.
                    cursorX = 0;
                    cursorY++;
                }
                else // wrapBehavior == WrapBehavior.WORD
                {
                    int origSpos = spos;
                    
                    // Rewind the pointer to the last whitespace character,
                    // then advance the cursor to the beginning of the next
                    // line.
                    while((spos > 0) && (cursorX > 0) && !Character.isWhitespace(s.charAt(spos)))
                    {
                        spos--;
                        cursorX--;
                    }
                    
                    if(spos == 0 || cursorX == 0)
                    {
                        // There are more non-whitespace characters than will
                        // fit on one line.  Force a break in the word at the
                        // current position by putting the string position back
                        // to its original position and causing the cursor to
                        // advance to the beginning of the next line.
                        spos = origSpos;
                        cursorX = 0;
                        cursorY++;
                    }
                    else
                    {
                        // The character at spos is whitespace. move one
                        // character past it.
                        spos++;
                        
                        // Blank to the end of the current line, and advance to
                        // the next.
                        while(cursorX < width)
                        {
                            lines.get(cursorY).put(cursorX++, '\0');
                        }
                        cursorX = 0;
                        cursorY++;
                    }
                }
            }
            
            if(cursorY >= lines.size())
            {
                addLine();
            }
            
            char ch = s.charAt(spos++);
            if(ch == '\n')
            {
                // if the current character is a return character, don't
                // bother printing it, just advance the cursor to the next line.
                cursorX = 0;
                cursorY++;
            }
            else
            {
                // print the character at the current position and advance the
                // position.
                lines.get(cursorY).put(cursorX++, ch);
            }
        }
    }

    /**
     * Moves the cursor to the position given by the x and y parameters, and
     * begins printing text at this position.  Otherwise, the function behaves
     * exactly as the standard print function.
     * @param s the string to print
     * @param x the cartesian x coordinate to move the cursor before printing.
     * @param y the cartesian y coordinate to move the cursor before printing.
     */
    public void print(String s, int x, int y)
    {
        setCursor(x, y);
        print(s);
    }
}
