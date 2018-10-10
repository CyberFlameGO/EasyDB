package net.lorgen.easydb;

import net.lorgen.easydb.query.Query;

import java.util.List;

/**
 * Accessor responsible for executing a given query on a specific database
 * type (e. g. Redis or MongoDB).
 *
 * @param <T> The type this accessor handles
 */
public interface DatabaseTypeAccessor<T extends StoredItem> {

    /**
     * Looks for the first T value to match the given parameters.
     *
     * @param query The {@link Query query}
     * @return The first T value to match the given parameters
     */
    T findFirst(Query<T> query);

    /**
     * Looks for all T values matching the given parameters
     *
     * @param query The {@link Query query}
     * @return All T values to match the given parameters
     */
    List<T> findAll(Query<T> query);

    /**
     * Saves or updates any T value matching the given parameters
     * using the given values. If no requirement is passed, it will
     * use values in the query, assuming you are saving an instance
     * of T in its entirety (i. e. using the key values as parameters).
     *
     * @param query The {@link Query query}
     */
    void saveOrUpdate(Query<T> query);

    /**
     * Deletes any T value matching the given parameters.
     *
     * @param query The {@link Query query}
     */
    void delete(Query<T> query);
}
