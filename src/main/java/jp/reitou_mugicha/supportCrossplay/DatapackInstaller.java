package jp.reitou_mugicha.supportCrossplay;

import org.bukkit.Bukkit;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Properties;
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
        File serverRoot = Bukkit.getWorldContainer();
        String levelName = "world";

        File propertiesFile = new File(serverRoot, "server.properties");
        if (propertiesFile.exists()) {
            try (InputStream is = new FileInputStream(propertiesFile)) {
                Properties props = new Properties();
                props.load(is);
                levelName = props.getProperty("level-name", "world");
            } catch (IOException e) {
                plugin.getLogger().warning("Could not read server.properties, using default 'world'");
            }
        }

        File mainWorldFolder = new File(serverRoot, levelName);
        File datapacksDir = new File(mainWorldFolder, "datapacks");

        if (!datapacksDir.exists()) {
            if (!datapacksDir.mkdirs()) {
                plugin.getLogger().severe("Failed to create directory: " + datapacksDir.getAbsolutePath());
                return;
            }
        }

        try {
            File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

            try (JarFile jar = new JarFile(jarFile)) {
                Set<String> packNames = Collections.list(jar.entries()).stream()
                        .map(JarEntry::getName)
                        .filter(name -> name.startsWith("datapacks/") && name.length() > 10)
                        .map(name -> {
                            String remaining = name.substring(10);
                            int slashIndex = remaining.indexOf("/");
                            return (slashIndex != -1) ? remaining.substring(0, slashIndex) : null;
                        })
                        .filter(name -> name != null)
                        .collect(Collectors.toSet());

                if (packNames.isEmpty()) {
                    plugin.getLogger().warning("No datapacks found in JAR.");
                    return;
                }

                for (String packName : packNames) {
                    File zipFile = new File(datapacksDir, packName + ".zip");
                    plugin.getLogger().info("Installing datapack: " + packName);

                    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                        String rootPath = "datapacks/" + packName + "/";

                        for (JarEntry entry : Collections.list(jar.entries())) {
                            String entryName = entry.getName();

                            if (entryName.startsWith(rootPath) && !entry.isDirectory()) {
                                String zipPath = entryName.substring(rootPath.length());
                                zos.putNextEntry(new ZipEntry(zipPath));

                                try (InputStream is = jar.getInputStream(entry)) {
                                    is.transferTo(zos);
                                }
                                zos.closeEntry();
                            }
                        }
                    } catch (IOException e) {
                        plugin.getLogger().severe("Error writing datapack: " + packName);
                    }
                }

                plugin.getLogger().info("Installation complete at: " + datapacksDir.getAbsolutePath());

            } catch (IOException e) {
                plugin.getLogger().severe("Failed to read JAR: " + e.getMessage());
            }
        } catch (URISyntaxException e) {
            plugin.getLogger().severe("Failed to resolve JAR path.");
            e.printStackTrace();
        }
    }
}