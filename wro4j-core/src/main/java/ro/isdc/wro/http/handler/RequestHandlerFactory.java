package ro.isdc.wro.http.handler;

import java.util.Collection;

import ro.isdc.wro.util.ObjectFactory;


/**
 * Responsible for creating a {@link RequestHandler}'s collection.
 * 
 * @author Ivar Conradi Østhus
 * @created 19 May 2012
 * @since 1.4.7
 */
public interface RequestHandlerFactory
    extends ObjectFactory<Collection<RequestHandler>> {
}