package jp.reitou_mugicha.supportCrossplay;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.*;

public class DatapackInstaller
{
    private final SupportCrossplay plugin;

    public DatapackInstaller(SupportCrossplay plugin)
    {
        this.plugin = plugin;
    }

    public void installForAllWorlds()
    {
        for (World world : Bukkit.getWorlds())
        {
            installTo(world.getWorldFolder());
        }
    }

    public void installTo(File worldFolder)
    {
        File datapacksDir = new File(worldFolder, "datapacks");
        if (!datapacksDir.exists()) datapacksDir.mkdirs();

        File zipFile = new File(datapacksDir, "SupportCrossplay.zip");

        try
        {
            File tempZip = new File(datapacksDir, "SupportCrossplay_temp.zip");

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZip)))
            {
                copyFolderFromJarToZip("datapack/", zos);
            }

            if (zipFile.exists())
            {
                if (zipEquals(zipFile, tempZip))
                {
                    tempZip.delete();
                    return;
                }
            }

            tempZip.renameTo(zipFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void copyFolderFromJarToZip(String rootPath, ZipOutputStream zos) throws IOException
    {
        File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        try (JarFile jar = new JarFile(jarFile))
        {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.startsWith(rootPath)) continue;
                if (entry.isDirectory()) continue;

                String zipPath = name.substring(rootPath.length());
                zos.putNextEntry(new ZipEntry(zipPath));

                try (InputStream is = jar.getInputStream(entry))
                {
                    is.transferTo(zos);
                }

                zos.closeEntry();
            }
        }
    }

    private boolean zipEquals(File f1, File f2)
    {
        try (ZipFile z1 = new ZipFile(f1); ZipFile z2 = new ZipFile(f2))
        {
            if (z1.size() != z2.size()) return false;

            Enumeration<? extends ZipEntry> e1 = z1.entries();

            while (e1.hasMoreElements())
            {
                ZipEntry entry1 = e1.nextElement();
                ZipEntry entry2 = z2.getEntry(entry1.getName());
                if (entry2 == null) return false;
                if (entry1.getCrc() != entry2.getCrc()) return false;
            }

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}