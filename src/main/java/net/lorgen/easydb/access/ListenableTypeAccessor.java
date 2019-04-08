package net.lorgen.easydb.access;

import com.google.common.collect.Lists;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.access.event.AccessorDeleteEvent;
import net.lorgen.easydb.access.event.AccessorDropEvent;
import net.lorgen.easydb.access.event.AccessorFindAllEvent;
import net.lorgen.easydb.access.event.AccessorFindFirstEvent;
import net.lorgen.easydb.access.event.AccessorRespondEvent;
import net.lorgen.easydb.access.event.AccessorSaveEvent;
import net.lorgen.easydb.access.event.AccessorSetUpEvent;
import net.lorgen.easydb.event.EventManager;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.interact.external.External;
import net.lorgen.easydb.interact.external.QueryHelper;
import net.lorgen.easydb.interact.external.QueryHelperListener;
import net.lorgen.easydb.profile.external.ExternalFieldProfile;
import net.lorgen.easydb.profile.external.strategy.StrategyHelper;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.response.Response;

import java.util.List;

/**
 * A {@link DatabaseTypeAccessor} with listenable events using the {@link EventManager}.
 * If you wish to use the {@link External} annotation, using a normal
 * {@link DatabaseTypeAccessor} will not work unless it features some special implementation.
 * This {@link DatabaseTypeAccessor accessor} has built in support for the {@link External}
 * annotation.
 *
 * @param <T> The type handled by this accessor
 */
public abstract class ListenableTypeAccessor<T> implements DatabaseTypeAccessor<T> {

    private final EventManager eventManager;
    private final ItemRepository<T> repository;

    public ListenableTypeAccessor(ItemRepository<T> repository) {
        this.repository = repository;
        this.eventManager = new EventManager();
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ItemRepository<T> getRepository() {
        return repository;
    }

    @Override
    public void setUp() {
        this.setUpInternal();
        this.setUpExternalFields();

        AccessorSetUpEvent event = new AccessorSetUpEvent(this);
        this.getEventManager().callEvent(event);
    }

    private void setUpExternalFields() {
        for (PersistentField<T> field : this.getProfile().getFields()) {
            if (field.getExternalTable() == null) {
                continue;
            }

            assert field.isExternalStore();

            ExternalFieldProfile<T> profile = StrategyHelper.getProfiler(this, field);
            QueryHelper helper = profile.getNewQueryHelper();
            this.getEventManager().registerListener(new QueryHelperListener(this.repository, helper));
        }
    }

    protected abstract void setUpInternal();

    @Override
    public Response<T> findFirst(Query<T> query) {
        Response<T> response = this.findFirstInternal(query);

        AccessorFindFirstEvent findFirstEvent = new AccessorFindFirstEvent(this, query, response);
        this.getEventManager().callEvent(findFirstEvent);

        AccessorRespondEvent respondEvent = new AccessorRespondEvent(this, query, response);
        this.getEventManager().callEvent(respondEvent);

        return response;
    }

    protected abstract Response<T> findFirstInternal(Query<T> query);

    @Override
    public List<Response<T>> findAll(Query<T> query) {
        List<Response<T>> responses = this.findAllInternal(query);

        AccessorFindAllEvent findAllEvent = new AccessorFindAllEvent(this, query, Lists.newArrayList(responses));
        this.getEventManager().callEvent(findAllEvent);

        for (Response<T> response : responses) {
            AccessorRespondEvent respondEvent = new AccessorRespondEvent(this, query, response);
            this.getEventManager().callEvent(respondEvent);
        }

        return responses;
    }

    public abstract List<Response<T>> findAllInternal(Query<T> query);

    @Override
    public void saveOrUpdate(Query<T> query) {
        this.saveOrUpdateInternal(query);

        AccessorSaveEvent event = new AccessorSaveEvent(this, query);
        this.getEventManager().callEvent(event);
    }

    public abstract void saveOrUpdateInternal(Query<T> query);

    @Override
    public void delete(Query<T> query) {
        this.deleteInternal(query);

        AccessorDeleteEvent event = new AccessorDeleteEvent(this, query);
        this.getEventManager().callEvent(event);
    }

    public abstract void deleteInternal(Query<T> query);

    @Override
    public void drop() {
        this.dropInternal();

        AccessorDropEvent event = new AccessorDropEvent(this);
        this.getEventManager().callEvent(event);
    }

    public abstract void dropInternal();
}
