//----------------------------------------------------------------------------------
//
// MMF JAVA RUNTIME LAUNCHER
//
//----------------------------------------------------------------------------------
package javaruntime;

import Application.*;
import java.io.*;
import java.lang.String;
import Services.*;
import javax.swing.JFileChooser;

public class Main implements FilenameFilter
{
    CRunApp app;
    CFile file;
    String filePath = null;
    String extPath = null;

    public Main()
    {
    }

    public static void main(String[] args)
    {
        new Main().run(args);
    }

    public void run(String[] args)
    {
        // An application object
        app = new CRunApp();

        // Extracts the parameters
        int n;
        int nArgs = 0;
        int argPath = -1;
        int argExt = -1;
        for (n = 0; n < args.length; n++)
        {
            if (args[n].equalsIgnoreCase("/f") && args.length > n + 1)
            {
                argPath = n;
                n++;
                filePath = args[n].trim().replace("\"", "");
            }
            else if (args[n].equalsIgnoreCase("/e") && args.length > n + 1)
            {
                argExt = n;
                n++;
                extPath = args[n].trim().replace("\"", "");
            }
            else
            {
                nArgs++;
            }
        }
        String[] newArgs = new String[nArgs];
        int count = 0;
        for (n = 0; n < args.length; n++)
        {
            if (n != argPath && n != argPath + 1 && n != argExt && n != argExt + 1)
            {
                newArgs[count++] = args[n];
            }
        }

        // If no file, search in the directory.
        if (filePath == null)
        {
            filePath = getProgramFile();
        }
        if (filePath == null)
        {
            System.exit(-1);
        }

        // Loading application
        app.setFilenames(filePath, extPath, null, null, null);
        app.setArgs(newArgs);

        // App Launch
        try
        {
            app.load();
            if (app.startApplication())
            {
                while (true)
                {
                    if (!app.playApplication(false))
                        break;
                }
                app.endApplication();
            }
        }
        catch (IOException e)
        {
            System.out.println("File error");
        }

        // End of Program
        try
        {
            file.close();
        }
        catch (IOException e)
        {
            System.out.println("File error");
        }
        System.exit(-1);
    }

    public String getProgramFile()
    {
        File dir = new File(".");
        File[] list = dir.listFiles(this);

        // A single file?
        if (list != null && list.length == 1)
        {
            return list[0].getAbsolutePath();
        }

        // Zero or more files; open file selector
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new CLoadAppFilter());
        int returnVal = fc.showOpenDialog(null);
        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
            return null;
        }

        File file = fc.getSelectedFile();
        return file.getAbsolutePath();
    }

    public boolean accept(File dir, String name)
    {
        String extension = CServices.getExtension(name);
        return extension != null && extension.compareTo("ccj") == 0;
    }
}
