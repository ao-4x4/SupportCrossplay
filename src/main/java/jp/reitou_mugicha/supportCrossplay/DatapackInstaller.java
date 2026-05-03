package jp.reitou_mugicha.supportCrossplay;

import org.bukkit.Bukkit;
import org.bukkit.World;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DatapackInstaller {
    private final SupportCrossplay plugin;

    public DatapackInstaller(SupportCrossplay plugin) {
        this.plugin = plugin;
    }

    public void installServer() {
        for (World world : Bukkit.getWorlds()) {
            installWorld(world.getWorldFolder());
        }
    }

    public void installWorld(File worldFolder) {
        File datapacksDir = new File(worldFolder, "datapacks");
        if (!datapacksDir.exists()) datapacksDir.mkdirs();

        try {
            File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

            try (JarFile jar = new JarFile(jarFile)) {
                Set<String> packNames = Collections.list(jar.entries()).stream()
                        .map(JarEntry::getName)
                        .filter(name -> name.startsWith("datapacks/") && name.length() > 10)
                        .map(name -> name.substring(10).split("/")[0])
                        .collect(Collectors.toSet());

                if (packNames.isEmpty()) {
                    plugin.getLogger().warning("Missing datapacks folder in jar.");
                    return;
                }

                for (String packName : packNames) {
                    File zipFile = new File(datapacksDir, packName + ".zip");
                    plugin.getLogger().info("Installing datapack: " + packName);

                    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                        String rootPath = "datapacks/" + packName + "/";

                        Collections.list(jar.entries()).forEach(entry -> {
                            String entryName = entry.getName();
                            if (entryName.startsWith(rootPath) && !entry.isDirectory()) {
                                try {
                                    String zipPath = entryName.substring(rootPath.length());
                                    zos.putNextEntry(new ZipEntry(zipPath));
                                    try (InputStream is = jar.getInputStream(entry)) {
                                        is.transferTo(zos);
                                    }
                                    zos.closeEntry();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
                plugin.getLogger().info("All datapacks have been installed to: " + worldFolder.getName());
            }
        } catch (URISyntaxException | IOException e) {
            plugin.getLogger().severe("Failed to install datapacks!");
            e.printStackTrace();
        }
    }
}