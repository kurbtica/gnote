package org.openjfx.sio2E4.service;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;


public class ConfigLoader {
    private static final String CONFIG_FILE_PATH = "IpApi.yml";

    public static ConfIpApi loadYamlConfig() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);

        try (InputStream inputStream = new FileInputStream(configFile)) {

            System.out.println("Lecture du fichier de configuration à partir de : " + configFile.getAbsolutePath());

            Yaml yaml = new Yaml();
            return yaml.loadAs(inputStream, ConfIpApi.class);

        } catch (FileNotFoundException e) {
            // --- LOGIQUE DE CRÉATION DU FICHIER PAR DÉFAUT ---
            System.err.println("⚠️ Fichier de configuration non trouvé : " + configFile.getAbsolutePath());
            System.err.println("   Création d'un fichier 'IpApi.yml' avec les valeurs par défaut...");

            // 1. Créer le contenu par défaut
            ConfIpApi defaultConfig = createDefaultConfig();

            // 2. Sauvegarder ce contenu sur le disque
            saveConfigToFile(defaultConfig, configFile);

            System.out.println("✅ Fichier créé. Utilisation des valeurs par défaut.");

            // 3. Retourner l'objet par défaut pour continuer le démarrage
            return defaultConfig;

        }
    }

    /**
     * Génère un objet ConfIpApi avec des valeurs par défaut.
     */
    private static ConfIpApi createDefaultConfig() {
        ConfIpApi config = new ConfIpApi();
        ConfIpApi.ApiConfig apiConfig = new ConfIpApi.ApiConfig();

        // VALEURS PAR DÉFAUT
        apiConfig.setBase_url("http://localhost:8080/api");

        config.setApi(apiConfig);
        return config;
    }

    /**
     * Sérialise l'objet de configuration en YAML et l'écrit sur le disque.
     */
    private static void saveConfigToFile(ConfIpApi config, File file) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Formatage lisible
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);

        // La classe Map/Object est nécessaire pour sérialiser notre modèle Java en YAML
        try (FileWriter writer = new FileWriter(file)) {
            // Serialise l'objet ConfIpApi en YAML et l'écrit
            yaml.dump(config, writer);
        }
    }}