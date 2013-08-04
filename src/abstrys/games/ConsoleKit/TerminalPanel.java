package com.abstrys.consolekit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

/**
 * A class that replicates the functionality of an old-style text terminal.  It
 * defaults to an 80x25 fixed-displayWidthChars display with a black background
 * and white text.  The size, text color, background color and font can be
 * changed.
 * @author Eron Hennessey
 */
public class TerminalPanel extends JPanel implements KeyListener
{
    private TerminalData data;
    private TerminalPrefs prefs;
    private int lineOffset = 0;
    private int charWidth = 0;
    private int lineHeight = 0;
    private int topBaseline = 0;
    private char[] cursorChar = {'|'};
    private Timer cursorTimer;
    private final int cursorBlinkDelay = 500;
    private boolean acceptInput = false;
    private boolean cursorIsVisible = false;
    private TerminalListener listener = null;
    private String keyInput = "";
    private final int WIDTHPADDING = 4;
    private final int HEIGHTPADDING = 4;
    private boolean firstPaint;

    Action updateCursorAction = new AbstractAction()
    {
        boolean shouldDraw = false;
        public void actionPerformed(ActionEvent e)
        {
            cursorIsVisible = !cursorIsVisible;
            repaint();
        }
    };
    
    /**
     * Default constructor.  Initializes the class with all default settings.
     */
    public TerminalPanel()
    {
        prefs = new TerminalPrefs();
        init();
    }
    
    public TerminalPanel(int width, int height)
    {
        prefs = new TerminalPrefs();
        prefs.displayWidthChars = width;
        prefs.bufferWidthChars = width;
        prefs.displayHeightChars = height;
        prefs.bufferHeightChars = height*2;
        init();
    }
    
    /**
     * Constructor that allows each option to be set at start-up.
     * @param width the width of both the window and the underlying buffer.
     * @param height the height of both the window and the underlying buffer.
     * @param font the font to use for displaying text.  This should be a
     * fixed-displayWidthChars font, or output will not appear correctly for
     * most applications.
     * @param fg the foreground color to use when displaying text.
     * @param bg the background color to use when displaying text.
     */
    public TerminalPanel(
            int width, int height, Font font, Color fg, Color bg)
    {
        prefs = new TerminalPrefs();
        
        // set all the options for this panel
        prefs.displayWidthChars = width;
        prefs.displayHeightChars = height;
        prefs.bufferWidthChars = width;
        prefs.bufferHeightChars = height*2;
        prefs.font = font;
        prefs.fgColor = fg;
        prefs.bgColor = bg;
        
        init();
    }
    
    public TerminalPanel(TerminalPrefs prefs)
    {
        this.prefs = prefs;
        init();
    }

    public void setRunnable(Runnable thread)
    {
        Thread procThread = new Thread(thread);
        procThread.start();
    }
    
    private void init()
    {
        // set up the terminal data
		this.data = new TerminalData(
				prefs.bufferWidthChars, prefs.bufferHeightChars);
        this.lineOffset = 0;
        this.setFont(prefs.font);
        this.setBackground(prefs.bgColor);
        this.setForeground(prefs.fgColor);
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(new MouseInputAdapter(){
            @Override
            public void mouseClicked(MouseEvent e)
            {
                requestFocusInWindow();
            }
        });
        firstPaint = true;
    }
    
    public void resetPrefs()
    {
        this.setFont(prefs.font);
        this.setBackground(prefs.bgColor);
        this.setForeground(prefs.fgColor);
        lineHeight = 0;
        firstPaint = true;
    }
    
    private void setTextAttributes(Graphics2D g2)
    {
        FontMetrics fm = g2.getFontMetrics(this.getFont());
        lineHeight = fm.getHeight();
        charWidth = fm.charWidth('0');
        topBaseline = HEIGHTPADDING + fm.getLeading() + fm.getAscent();
    }
    
    private Dimension findOptimumSize()
    {
		return new Dimension(charWidth * prefs.displayWidthChars +
				WIDTHPADDING*2, lineHeight * prefs.displayHeightChars +
				HEIGHTPADDING*2); }
    
    private void resizeParentToOptimumSize()
    {
        Dimension d = findOptimumSize();

        java.awt.Container c = this;
        while((c = c.getParent()) != null)
        {
            if(c instanceof javax.swing.JFrame)
            {
                javax.swing.JFrame j = (javax.swing.JFrame)c;
                java.awt.Insets i = j.getInsets();
                d.width += i.left + i.right;
                d.height += i.top + i.bottom;
                j.setSize(d);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;

        if(firstPaint)
        {
            setTextAttributes(g2);
            resizeParentToOptimumSize();
            firstPaint = false;
            repaint();
            return;
        }

        super.paintComponent(g);

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				prefs.renderHint);
        g.setColor(prefs.bgColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(prefs.fgColor);        
        
        // draw the visible lines.
		for(int i = this.lineOffset; i < (this.lineOffset +
					prefs.displayHeightChars); i++)
        {
            char[] line = data.getLine(i);
            if(line == null)
            {
                break;
            }
			g2.drawChars(line, 0, line.length, WIDTHPADDING, topBaseline +
					((i-this.lineOffset) * lineHeight));
        }
        
        // draw the cursor (if visible)
        if(cursorIsVisible)
        {
            Point cursorPos = data.getCursor();
			g2.drawChars(cursorChar, 0, 1, (cursorPos.x)*charWidth,
					(cursorPos.y+1 - this.lineOffset)*lineHeight);
        }
    }

    /**
     * Prints text at the current cursor position.
     * @param text the text to print.
     */
    public void print(String text)
    {
        data.print(text);
        if(data.getCursor().y >= (lineOffset+prefs.displayHeightChars))
        {
            lineOffset = (data.getCursor().y-prefs.displayHeightChars)+1;
        }
    }

    /**
     * Prints text, beginning at the provided row and column position.
     * @param text the text to print.
     * @param x the column at which to begin printing.
     * @param y the row at which to begin printing.
     */
    public void print(String text, int x, int y)
    {
        data.print(text, x, y);
        if(data.getCursor().y >= (lineOffset+prefs.displayHeightChars))
        {
            lineOffset = data.getCursor().y-prefs.displayHeightChars+1;
        }
    }

    /**
     * Prints a character at the provided row and column.
     * @param ch the character to print
     * @param x the column at which to display the character
     * @param y the row at which to display the character
     */
    public void setChar(char ch, int x, int y)
    {
        data.setChar(ch, x, y);
    }

    /**
     * Clears the display
     */
    public void clearDisplay()
    {
        data.clear();
    }

    /**
     * Clears a single character position on the display
     * @param x the horizontal position of the character to clear.
     * @param y the vertical position of the character to clear.
     */
    public void clearChar(int x, int y)
    {
        data.setChar(' ', x, y);
    }

    /**
	 * Fills an area of the display with a given character, beginning at the row
	 * and column position given.  The area filled will extend a number of
	 * characters (determined by displayWidthChars and displayHeightChars) down
	 * and to the right of the position given.
     * @param ch tge character to fill the area with.
     * @param x the column at which to begin filling.
     * @param y the row at which to begin filling.
     * @param width the width of the area to fill.
     * @param height the height of the area to fill.
     */
    public void fill(char ch, int x, int y, int width, int height)
    {
        data.fill(ch, x, y, width, height);
    }
    
	/**
	 * Shows, or hides, the blinking cursor.  This function also begins input
	 * mode, in which typing in the panel will send messages to any terminal
	 * listeners.
	 * @param show set to true to show the cursor, or false to hide it.
	 */
    public void showPrompt(boolean show)
    {
        acceptInput = show;
        if(acceptInput)
        {
            // set up a timer to periodically show the cursor.
            cursorTimer = new Timer(cursorBlinkDelay, updateCursorAction);
            cursorTimer.start();
            cursorIsVisible = true;
        }
        else
        {
            // remove the timer task and hide the cursor.
            cursorTimer.stop();
            cursorIsVisible = false;
        }
        repaint();
    }
    
	/**
	 * Sets the terminal listener that will receive notifications when
	 * characters are typed in the panel.
     * 
     * @param l the TerminalListener to set.
     */
    public void setTerminalListener(TerminalListener l)
    {
        listener = l;
    }
    
    //
    // KeyListener methods.
    //
    public void keyTyped(KeyEvent e)
    {
        char ch = e.getKeyChar();
        
        if(ch == '\b')
        {
            int len = keyInput.length();
            if(len > 0)
            {
                keyInput = keyInput.substring(0,len-1);
                Point cpos = data.getCursor();
                data.setChar('\0', --cpos.x, cpos.y);
                data.setCursor(cpos.x, cpos.y);                
            }
            if(listener != null)
            {
				// backspace characters don't appear in the line keyInput, so
				// there's no need to pay attention to the return value of
				// terminalCharTyped in this case.
                listener.terminalCharTyped(ch);
            }
        }
        else if(ch == '\n')
        {
            data.print("\n");
            if(listener != null)
            {
				// in this case, the line will be refreshed.  Again, there's no
				// need to pay attention to the return value of
				// terminalCharTyped.
                listener.terminalCharTyped(ch);
                listener.terminalLineTyped(keyInput);
                keyInput = new String("");
            }
        }
        else
        {
            boolean addToInput = true;
            data.print("" + ch);
            if(listener != null)
            {
                addToInput = !listener.terminalCharTyped(ch);
            }
            if(addToInput)
            {
                keyInput += ch;
            }
        }
        repaint();
    }

    public void keyPressed(KeyEvent e)
    {
        // ignored.
	}

    public void keyReleased(KeyEvent e)
    {
        // ignored.
	}

}
