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

    public void installServer()
    {
        for (World world : Bukkit.getWorlds())
        {
            installWorld(world.getWorldFolder());
        }
        plugin.getLogger().info("The datapack has been installed.");
    }

    public void installWorld(File worldFolder)
    {
        File datapacksDir = new File(worldFolder, "datapacks");
        if (!datapacksDir.exists()) datapacksDir.mkdirs();

        try
        {
            File jarFile = new File(
                    getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
            );

            try (JarFile jar = new JarFile(jarFile))
            {
                jar.stream()
                        .map(JarEntry::getName)
                        .filter(name -> name.startsWith("datapacks/"))
                        .map(name -> name.substring("datapacks/".length()))
                        .filter(name -> name.contains("/"))
                        .map(name -> name.substring(0, name.indexOf("/")))
                        .distinct()
                        .forEach(packName ->
                        {
                            File zipFile = new File(datapacksDir, packName + ".zip");
                            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile)))
                            {
                                copyFolder("datapacks/" + packName + "/", zos);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void copyFolder(String rootPath, ZipOutputStream zos) throws IOException
    {
        File jarFile = new File(
                getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
        );

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