package com.example.demo.odata;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import java.util.List;
import java.util.Optional;

public interface ODataEntityDescriptor<T> {

    String getEntityName();

    String getEntitySetName();

    String getKeyPropertyName();

    List<CsdlProperty> getODataProperties();

    List<T> findAll();

    Optional<T> findById(String id);

    Object getPropertyValue(T entity, String propertyName);

    default Entity toODataEntity(T entity) {
        Entity odataEntity = new Entity();
        getODataProperties().forEach(prop ->
                odataEntity.addProperty(new Property(null, prop.getName(), ValueType.PRIMITIVE,
                        getPropertyValue(entity, prop.getName()))));
        return odataEntity;
    }

    default Optional<Entity> findByIdAsODataEntity(String id) {
        return findById(id).map(this::toODataEntity);
    }
}
