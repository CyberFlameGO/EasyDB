package net.lorgen.easydb.interact.external;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.access.event.AccessorDeleteEvent;
import net.lorgen.easydb.access.event.AccessorRespondEvent;
import net.lorgen.easydb.access.event.AccessorSaveEvent;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.external.ExternalFieldProfile;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.response.Response;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ExternalCollectionQueryHelper implements QueryHelper {

    private ExternalFieldProfile profile;

    public ExternalCollectionQueryHelper(ExternalFieldProfile profile) {
        this.profile = profile;
    }

    @Override
    public void handle(ItemRepository repository, AccessorRespondEvent event) {
        Collection collection = this.newInstance();
        if (collection == null) {
            return;
        }

        List<Response> responses = this.profile.newQuery(repository, event.getEntity()).findAll();
        collection.addAll(responses.stream().map(response -> this.profile.extractValue(response)).collect(Collectors.toList()));

        event.getEntity().getOrCreateValue(this.getField().getName()).setValue(collection);
    }

    @Override
    public void handle(ItemRepository repository, AccessorDeleteEvent event) {
        if (!this.getField().isTransient()) {
            return;
        }

        Query baseQuery = event.getQuery();
        this.profile.newQuery(repository, baseQuery).delete();
    }

    @Override
    public void handle(ItemRepository repository, AccessorSaveEvent event) {
        if (this.getField().isTransient()) {
            return;
        }

        Query baseQuery = event.getQuery();

        this.profile.newQuery(repository, baseQuery).delete(); // Removes any elements that have been removed from the list

        Collection<?> collection = (Collection<?>) baseQuery.getValue(this.getField()).getValue();
        // Minor speed-up
        if (!this.profile.needsToProvideIndex()) {
            collection.forEach(value -> this.profile.save(repository, baseQuery, null, value));
            return;
        }

        Iterator<?> iterator = collection.iterator();
        AtomicInteger index = new AtomicInteger(0);
        iterator.forEachRemaining(value -> this.profile.save(repository, baseQuery, index.getAndIncrement(), value));
    }

    // Internals

    private PersistentField getField() {
        return this.profile.getField();
    }

    private Collection<?> newInstance() {
        Class<? extends Collection> typeClass = (Class<? extends Collection>) this.getField().getTypeClass();
        if (!typeClass.isInterface()) {
            try {
                return typeClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (List.class.isAssignableFrom(typeClass)) {
            return Lists.newLinkedList();
        }

        if (Set.class.isAssignableFrom(typeClass)) {
            return Sets.newHashSet();
        }

        if (Queue.class.isAssignableFrom(typeClass)) {
            return Lists.newLinkedList();
        }

        throw new IllegalStateException("Unknown collection " + typeClass.getSimpleName() + "!");
    }
}
