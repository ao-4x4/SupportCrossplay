package jp.reitou_mugicha.supportCrossplay;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile)))
        {
            copyFolderFromJarToZip("datapack/", zos);
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
}