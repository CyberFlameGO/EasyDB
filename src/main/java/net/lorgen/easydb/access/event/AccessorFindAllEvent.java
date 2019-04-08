package net.lorgen.easydb.access.event;

import net.lorgen.easydb.access.DatabaseTypeAccessor;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.response.Response;

import java.util.List;

public class AccessorFindAllEvent extends QueryEvent {

    private List<Response<?>> responses;

    public AccessorFindAllEvent(DatabaseTypeAccessor<?> accessor, Query<?> query, List<Response<?>> responses) {
        super(accessor, query);
        this.responses = responses;
    }

    public List<Response<?>> getResponses() {
        return responses;
    }
}
