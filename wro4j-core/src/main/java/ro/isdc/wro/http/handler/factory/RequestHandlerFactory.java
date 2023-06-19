package ro.isdc.wro.http.handler.factory;

import java.util.Collection;

import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.util.ObjectFactory;


/**
 * Responsible for creating a {@link RequestHandler}'s collection. The {@link RequestHandlerFactory#create()} will be
 * invoked for each request. It is up to implementation to cache the requestHandler's creation.
 * 
 * @author Ivar Conradi Østhus
 * @since 1.4.7
 */
public interface RequestHandlerFactory
    extends ObjectFactory<Collection<RequestHandler>> {
}