package library.repository;

import library.model.LibraryConfiguration;
import library.persistence.DataStore;

/**
 * Thread-safe repository for global LibraryConfiguration.
 */
public class ConfigurationRepository {

    private final DataStore dataStore;
    private volatile LibraryConfiguration configuration;

    public ConfigurationRepository(DataStore dataStore) {
        this.dataStore = dataStore;
        this.configuration = dataStore.loadConfig();
    }

    public LibraryConfiguration get() {
        return configuration;
    }

    public synchronized void save(LibraryConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null.");
        }
        this.configuration = config;
        dataStore.saveConfig(config);
    }
}
