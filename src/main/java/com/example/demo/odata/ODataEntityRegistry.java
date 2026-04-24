package com.example.demo.odata;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ODataEntityRegistry {

    private final List<ODataEntityDescriptor<?>> descriptors;

    public ODataEntityRegistry(List<ODataEntityDescriptor<?>> descriptors) {
        this.descriptors = descriptors;
    }

    public List<ODataEntityDescriptor<?>> getAll() {
        return descriptors;
    }

    public Optional<ODataEntityDescriptor<?>> findByEntitySetName(String name) {
        return descriptors.stream().filter(d -> d.getEntitySetName().equals(name)).findFirst();
    }

    public Optional<ODataEntityDescriptor<?>> findByEntityName(String name) {
        return descriptors.stream().filter(d -> d.getEntityName().equals(name)).findFirst();
    }
}
