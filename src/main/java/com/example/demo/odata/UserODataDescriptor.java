package com.example.demo.odata;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserODataDescriptor implements ODataEntityDescriptor<User> {

    private final UserRepository userRepository;

    public UserODataDescriptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String getEntityName() { return "User"; }

    @Override
    public String getEntitySetName() { return "Users"; }

    @Override
    public String getKeyPropertyName() { return "id"; }

    @Override
    public List<CsdlProperty> getODataProperties() {
        return Arrays.asList(
                new CsdlProperty().setName("id").setType(EdmPrimitiveTypeKind.Guid.getFullQualifiedName()).setNullable(false),
                new CsdlProperty().setName("name").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setNullable(false),
                new CsdlProperty().setName("surname").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setNullable(false),
                new CsdlProperty().setName("role").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()).setNullable(false)
        );
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(String id) {
        try {
            return userRepository.findById(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Object getPropertyValue(User user, String propertyName) {
        switch (propertyName) {
            case "id":      return user.getId();
            case "name":    return user.getName();
            case "surname": return user.getSurname();
            case "role":    return user.getRole() != null ? user.getRole().name() : null;
            default:        return null;
        }
    }
}
