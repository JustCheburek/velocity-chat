package me.confor.velocity.chat.storage;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles storage of player data, primarily tracking which players have been seen before
 */
public class PlayerStorage {
    private final Path dataDirectory;
    private final Logger logger;
    private final Path seenPlayersFile;
    private final Set<String> seenPlayers;
    
    public PlayerStorage(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.seenPlayersFile = dataDirectory.resolve("seenPlayers.txt");
        this.seenPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
        
        loadSeenPlayers();
    }
    
    /**
     * Load the set of previously seen players from file
     */
    private void loadSeenPlayers() {
        try {
            if (!Files.exists(seenPlayersFile)) {
                Files.createDirectories(seenPlayersFile.getParent());
                Files.createFile(seenPlayersFile);
                logger.info("Created new seen players file at {}", seenPlayersFile);
            } else {
                Set<String> loadedPlayers = new HashSet<>(Files.readAllLines(seenPlayersFile));
                seenPlayers.addAll(loadedPlayers);
                logger.info("Loaded {} seen players", seenPlayers.size());
            }
        } catch (IOException e) {
            logger.error("Failed to load seen players file", e);
        }
    }
    
    /**
     * Check if a player has been seen before
     * 
     * @param uuid The UUID of the player to check
     * @return true if the player has been seen before, false otherwise
     */
    public boolean hasPlayerBeenSeen(String uuid) {
        return seenPlayers.contains(uuid);
    }
    
    /**
     * Mark a player as seen and save to storage
     * 
     * @param uuid The UUID of the player to mark as seen
     * @return true if this is the first time the player is being marked as seen
     */
    public boolean markPlayerAsSeen(String uuid) {
        if (seenPlayers.add(uuid)) {
            try {
                Files.write(
                    seenPlayersFile, 
                    (uuid + System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND
                );
                return true;
            } catch (IOException e) {
                logger.error("Failed to save seen player {}", uuid, e);
                // Remove from memory if we couldn't save to disk
                seenPlayers.remove(uuid);
            }
        }
        return false;
    }
}