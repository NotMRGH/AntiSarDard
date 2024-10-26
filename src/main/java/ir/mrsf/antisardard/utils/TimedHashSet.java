package ir.mrsf.antisardard.utils;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is used like normal HashSets, with timed keys.
 * After time expires, the key will be removed.
 *
 * @param <Key> Key of the Set.
 */
public class TimedHashSet<Key> {

    private final Set<TimedKey<Key>> set = new HashSet<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Add the key to the HashSet with given time to remove.
     *
     * @param key      key to add
     * @param duration given duration to remove
     * @param timeUnit unit of the time
     */
    public void add(Key key, long duration, @NotNull TimeUnit timeUnit) {
        val expirationTime = Instant.now().plusMillis(timeUnit.toMillis(duration));
        val timedKey = new TimedKey<>(key, expirationTime);
        this.set.add(timedKey);

        scheduleCleanup(duration, timeUnit);
    }

    /**
     * Check if the given key exists in the set.
     *
     * @param key key to search for.
     * @return true if the key exists in the set; otherwise false.
     */
    public boolean contains(@NotNull Key key) {
        cleanupExpiredEntries();
        return this.set.stream().anyMatch(timedKey -> timedKey.key().equals(key));
    }

    /**
     * Removes the given key in the set.
     *
     * @param key key to remove.
     */
    public void remove(@NotNull Key key) {
        this.set.removeIf(timedKey -> timedKey.key().equals(key));
    }

    public Set<Key> keys() {
        cleanupExpiredEntries();
        Set<Key> keys = new HashSet<>();
        for (TimedKey<Key> timedKey : this.set) {
            keys.add(timedKey.key());
        }
        return keys;
    }

    /**
     * Cleans up the expired entries if there are any.
     */
    private void cleanupExpiredEntries() {
        val instant = Instant.now();
        this.set.removeIf(timedKey -> timedKey.expirationTime().isBefore(instant));
    }

    /**
     * Schedules to clean up the expired entries if there are any in the given time.
     *
     * @param duration duration of the key to be removed.
     * @param timeUnit given time unit
     */
    private void scheduleCleanup(long duration, @NotNull TimeUnit timeUnit) {
        this.scheduler.schedule(this::cleanupExpiredEntries, duration, timeUnit);
    }

    public record TimedKey<K>(@NotNull K key, @NotNull Instant expirationTime) {
    }

}
