//package com.michabond.rest.sse;
//
//import org.glassfish.jersey.media.sse.EventOutput;
//import org.glassfish.jersey.media.sse.OutboundEvent;
//import org.glassfish.jersey.media.sse.SseBroadcaster;
//import org.glassfish.jersey.media.sse.SseFeature;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.inject.Singleton;
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.MediaType;
//
//@Singleton
//@Path("/broadcast")
//public class SSEResource {
//
//    private final Logger LOGGER = LoggerFactory.getLogger(SSEResource.class);
//    private final SseBroadcaster BROADCASTER = new SseBroadcaster();
//
//    /**
//     * Registers a new client connection for broadcasting.
//     * Upon disconnection, the connection is automatically discarded.
//     */
//    @GET
//    @Produces(SseFeature.SERVER_SENT_EVENTS)
//    public EventOutput listenToBroadcast() {
//        this.LOGGER.info("Registered new client");
//        final EventOutput eventOutput = new EventOutput();
//        this.BROADCASTER.add(eventOutput);
//        return eventOutput;
//    }
//
//    @GET
//    @Path("test")
//    @Produces(MediaType.TEXT_PLAIN)
//    public String broadcastMessage(@QueryParam("message") String message) {
//        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
////        message = message + "\n\n";
//        OutboundEvent event = eventBuilder.name("message")
//                .mediaType(MediaType.TEXT_PLAIN_TYPE)
//                .data(String.class, message)
//                .build();
//
//        this.BROADCASTER.broadcast(event);
//        this.LOGGER.info("Broadcasting listen [" + message + "]");
//
//        return "Message was '" + message + "' broadcast.";
//    }
//
//}
