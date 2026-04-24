package com.example.demo.odata;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.uri.*;

import java.util.List;
import java.util.Locale;

/**
 * GenericEntityProcessor — Gestore delle operazioni OData su singole entità
 *
 * Questa classe implementa l'interfaccia {@link EntityProcessor} di Apache Olingo
 * e gestisce le richieste OData che operano su una singola entità identificata
 * dalla sua chiave primaria.
 *
 * Esempio di richieste gestite:
 *   GET /odata/Users(1)      → legge l'utente con ID 1
 *   GET /odata/Products('X') → legge il prodotto con ID "X"
 *
 * Il processor è "generico" perché non è legato a nessuna entità specifica:
 * usa ODataEntityRegistry per trovare il descrittore corretto a runtime
 * in base al nome dell'EntitySet richiesto.
 *
 * @see ODataEntityRegistry
 * @see ODataEntityDescriptor
 * @see EntityProcessor
 */
public class GenericEntityProcessor implements EntityProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private final ODataEntityRegistry registry;

    public GenericEntityProcessor(ODataEntityRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response,
                           UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {

        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        ODataEntityDescriptor<?> descriptor = registry.findByEntitySetName(edmEntitySet.getName())
                .orElseThrow(() -> new ODataApplicationException("Entity set not found",
                        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT));

        List<UriParameter> keyParams = uriResourceEntitySet.getKeyPredicates();
        String id = keyParams.get(0).getText().replace("'", "");

        Entity entity = findEntityById(descriptor, id);

        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).suffix(ContextURL.Suffix.ENTITY).build();
        EntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextUrl).build();

        response.setContent(odata.createSerializer(responseFormat)
                .entity(serviceMetadata, edmEntityType, entity, opts).getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    private <T> Entity findEntityById(ODataEntityDescriptor<T> descriptor, String id) throws ODataApplicationException {
        return descriptor.findById(id)
                .map(entity -> descriptor.toODataEntity(entity))
                .orElseThrow(() -> new ODataApplicationException("Entity not found",
                        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT));
    }

    @Override
    public void createEntity(ODataRequest req, ODataResponse res, UriInfo info, ContentType reqF, ContentType resF) throws ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void updateEntity(ODataRequest req, ODataResponse res, UriInfo info, ContentType reqF, ContentType resF) throws ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void deleteEntity(ODataRequest req, ODataResponse res, UriInfo info) throws ODataApplicationException {
        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
}
