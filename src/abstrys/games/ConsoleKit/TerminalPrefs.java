/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.abstrys.consolekit;

import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains preferences for the Console engine.
 * @author Eron Hennessey / Abstrys
 */
public class TerminalPrefs {
    public int displayWidthPx;
    public int displayHeightPx;
    public int displayWidthChars;
    public int displayHeightChars;
    public int bufferWidthChars;
    public int bufferHeightChars;
    public Color fgColor;
    public Color bgColor;
    public Object renderHint;
    public Font font;
    public boolean monospaceOnly;
    private final String PREFS_FILE = ".AbstrysTerminalPanelPrefs";
    private final String PREFS_TAG = "Abstrys Terminal Panel Prefs";

    // if any of the data members above change, increment the prefsVersion and
    // handle the change in the load/save functions.
    private final int PREFS_VERSION = 1;

    public TerminalPrefs()
    {
        displayWidthPx = 560;
        displayWidthPx = 425;
        displayWidthChars = 80;
        displayHeightChars = 25;
        bufferWidthChars = 80;
        bufferHeightChars = 150;
        fgColor = Color.WHITE;
        bgColor = Color.BLACK;
        renderHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
        font = new Font("Monospaced", Font.PLAIN, 12);
        monospaceOnly = true;
    }

    /**
     * Copies contents from another TerminalPrefs object.
     * @param prefs
     */
    public TerminalPrefs(TerminalPrefs prefs)
    {
        setPrefs(prefs);
    }

    public void setPrefs(TerminalPrefs prefs)
    {
        displayWidthPx = prefs.displayWidthPx;
        displayHeightPx = prefs.displayHeightPx;
        displayWidthChars = prefs.displayWidthChars;
        displayHeightChars = prefs.displayHeightChars;
        bufferWidthChars = prefs.bufferWidthChars;
        bufferHeightChars = prefs.bufferHeightChars;
        fgColor = new Color(prefs.fgColor.getRGB());
        bgColor = new Color(prefs.bgColor.getRGB());
        renderHint = prefs.renderHint;
        font = new Font(prefs.font.getAttributes());
        monospaceOnly = prefs.monospaceOnly;
    }

    /**
     * Loads preferences from a file.
     * @param path the full pathname of the file to load preference data from.
     * @return true if preference information could be retrieved, or false if
     * the file does not exist or is corrupt.
     * @throws IOException if an error occured while reading the file.
     */
    public boolean loadPrefs(String path) throws IOException
    {
        // find the user's home directory.
        String homeDir = System.getProperty("user.home");
        if(homeDir == null)
        {
            homeDir = System.getProperty("user.dir");
        }

        FileReader fr = null;
        try
        {
            fr = new FileReader(homeDir + File.separator + PREFS_FILE);
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(TerminalPrefs.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        if(!line.equals(PREFS_TAG))
        {
            return false;
        }
        line = br.readLine();
        int v = Integer.parseInt(line);
        if(v != PREFS_VERSION)
        {
            // TODO: Older versions of the file will have their own legacy reading functions.
            return true;
        }

        // read the prefs and convert the data.
        line = br.readLine();
        displayWidthPx = Integer.parseInt(line);
        line = br.readLine();
        displayHeightPx = Integer.parseInt(line);
        line = br.readLine();
        displayWidthChars = Integer.parseInt(line);
        line = br.readLine();
        displayHeightChars = Integer.parseInt(line);
        line = br.readLine();
        bufferWidthChars = Integer.parseInt(line);
        line = br.readLine();
        bufferHeightChars = Integer.parseInt(line);
        line = br.readLine();
        fgColor = Color.decode(line);
        line = br.readLine();
        bgColor = Color.decode(line);
        line = br.readLine();
        renderHint = null; // TODO: read this!
        line = br.readLine();
        font = Font.getFont(line);
        line = br.readLine();
        monospaceOnly = Boolean.parseBoolean(line);

        return true;
    }

    /**
     * Saves preferences to a file.
     * @param path the full pathname of the file to save preference data to.
     * @return
     */
    public boolean savePrefs(String path)
    {
        // find the user's home directory.
        String homeDir = System.getProperty("user.home");
        if(homeDir == null)
        {
            homeDir = System.getProperty("user.dir");
        }

        // open (or create) the prefs file.
        File saveFile = new File(homeDir, PREFS_FILE);
        if(!saveFile.exists())
        {
            try
            {
                saveFile.createNewFile();
            }
            catch (IOException ex)
            {
                Logger.getLogger(TerminalPrefs.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(homeDir + File.separator + PREFS_FILE);
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(TerminalPrefs.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        PrintStream ps = new PrintStream(fos);
        ps.println(PREFS_TAG);
        ps.println(PREFS_VERSION);
        ps.println(displayWidthPx);
        ps.println(displayHeightPx);
        ps.println(displayWidthChars);
        ps.println(displayHeightChars);
        ps.println(bufferWidthChars);
        ps.println(bufferHeightChars);
        ps.println(fgColor.getRGB());
        ps.println(bgColor.getRGB());
        ps.println(renderHint);
        ps.println(font.getName());
        ps.println(new Boolean(monospaceOnly).toString());
        return true;
    }
}
