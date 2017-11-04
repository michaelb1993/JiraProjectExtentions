package com.michabond.rest.subscription;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.michabond.ao.accessor.SubscriptionDao;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Named
@Path("/subscription")
public class SubscriptionResource {

    private SubscriptionDao subscriptionDao;

    @Inject
    public SubscriptionResource(SubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    @GET
    @Path("/health")
    @Produces({MediaType.APPLICATION_JSON})
    @AnonymousAllowed
    public Response health() {
        return Response.ok("ok").build();
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @AnonymousAllowed
    public Response addUserSubscription(SubscriptionResourceModel subscriptionParams) {
        try {
            this.subscriptionDao.addSubscription(subscriptionParams);
            return Response.ok("ok").build();
        }
        catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
