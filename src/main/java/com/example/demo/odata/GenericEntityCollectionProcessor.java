package com.example.demo.odata;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.*;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.*;
import org.apache.olingo.server.api.uri.queryoption.expression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GenericEntityCollectionProcessor implements EntityCollectionProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private final ODataEntityRegistry registry;

    public GenericEntityCollectionProcessor(ODataEntityRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response,
                                     UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {

        EdmEntitySet edmEntitySet = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        ODataEntityDescriptor<?> descriptor = registry.findByEntitySetName(edmEntitySet.getName())
                .orElseThrow(() -> new ODataApplicationException("Entity set not found",
                        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT));

        EntityCollection collection = buildEntityCollection(descriptor, uriInfo);

        SelectOption selectOption = uriInfo.getSelectOption();
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet)
                .selectList(odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption))
                .build();

        SerializerResult result = odata.createSerializer(responseFormat)
                .entityCollection(serviceMetadata, edmEntityType, collection,
                        EntityCollectionSerializerOptions.with().contextURL(contextUrl).select(selectOption).build());

        response.setContent(result.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    private <T> EntityCollection buildEntityCollection(ODataEntityDescriptor<T> descriptor, UriInfo uriInfo)
            throws ODataApplicationException {

        List<T> entities = new ArrayList<>(descriptor.findAll());
        GenericExpressionVisitor visitor = new GenericExpressionVisitor();

        // $filter
        FilterOption filterOption = uriInfo.getFilterOption();
        if (filterOption != null) {
            Expression expression = filterOption.getExpression();
            entities = entities.stream().filter(entity -> {
                visitor.setPropertyAccessor(name -> descriptor.getPropertyValue(entity, name));
                try {
                    return Boolean.TRUE.equals(expression.accept(visitor));
                } catch (ExpressionVisitException | ODataApplicationException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }

        // $orderby
        OrderByOption orderByOption = uriInfo.getOrderByOption();
        if (orderByOption != null) {
            for (int i = orderByOption.getOrders().size() - 1; i >= 0; i--) {
                OrderByItem item = orderByOption.getOrders().get(i);
                boolean desc = item.isDescending();
                entities.sort((a, b) -> {
                    try {
                        visitor.setPropertyAccessor(name -> descriptor.getPropertyValue(a, name));
                        Object val1 = item.getExpression().accept(visitor);
                        visitor.setPropertyAccessor(name -> descriptor.getPropertyValue(b, name));
                        Object val2 = item.getExpression().accept(visitor);
                        if (val1 instanceof Comparable && val2 instanceof Comparable) {
                            @SuppressWarnings("unchecked")
                            int cmp = ((Comparable<Object>) val1).compareTo(val2);
                            return desc ? -cmp : cmp;
                        }
                        return 0;
                    } catch (Exception e) {
                        return 0;
                    }
                });
            }
        }

        // $skip
        SkipOption skipOption = uriInfo.getSkipOption();
        if (skipOption != null) {
            int skip = skipOption.getValue();
            entities = entities.subList(Math.min(skip, entities.size()), entities.size());
        }

        // $top
        TopOption topOption = uriInfo.getTopOption();
        if (topOption != null) {
            int top = topOption.getValue();
            entities = entities.subList(0, Math.min(top, entities.size()));
        }

        EntityCollection collection = new EntityCollection();
        for (T entity : entities) {
            Entity odataEntity = descriptor.toODataEntity(entity);
            collection.getEntities().add(odataEntity);
        }
        return collection;
    }
}
