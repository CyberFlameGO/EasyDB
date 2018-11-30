package net.lorgen.easydb;

/**
 * A special case of @{@link ItemRepository}, where this repository
 * stores the data in a (and only one) database.
 *
 * @param <T> The type stored
 */
public interface DatabaseRepository<T> extends ItemRepository<T> {

    /**
     * @return The {@link DatabaseTypeAccessor accessor} used for accessing
     * the database for various operations
     */
    DatabaseTypeAccessor<T> getDatabaseAccessor();

    /**
     * @return The database {@link DatabaseType type}
     */
    DatabaseType getDatabaseType();
}

