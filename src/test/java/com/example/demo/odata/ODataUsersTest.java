package com.example.demo.odata;

import com.example.demo.kafka.KafkaProducerService;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ODataUsersTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    // Mocka Kafka — nei test non si invia nulla a Kafka reale
    @MockBean
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private User createUser(String name, String surname, Role role) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setRole(role);
        return userRepository.save(user);
    }

    @Test
    void metadata_shouldReturnXmlContainingUserEntityType() {
        ResponseEntity<String> response = restTemplate.getForEntity("/odata/$metadata", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).contains("application/json");
        assertThat(response.getBody()).contains("User");
        assertThat(response.getBody()).contains("name");
        assertThat(response.getBody()).contains("surname");
        assertThat(response.getBody()).contains("role");
    }

    @Test
    void getUsers_shouldReturnAllUsers() {
        createUser("Mario", "Rossi", Role.ADMIN);
        createUser("Luigi", "Verdi", Role.USER);

        ResponseEntity<String> response = restTemplate.getForEntity("/odata/Users", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Mario");
        assertThat(response.getBody()).contains("Luigi");
    }

    @Test
    void getUsers_withFilterByName_shouldReturnOnlyMatchingUsers() {
        createUser("Mario", "Rossi", Role.ADMIN);
        createUser("Luigi", "Verdi", Role.USER);

        URI uri = UriComponentsBuilder.fromPath("/odata/Users")
                .queryParam("$filter", "name eq 'Mario'")
                .build().toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Mario");
        assertThat(response.getBody()).doesNotContain("Luigi");
    }

    @Test
    void getUsers_withTop_shouldReturnLimitedResults() {
        createUser("Mario", "Rossi", Role.ADMIN);
        createUser("Luigi", "Verdi", Role.USER);
        createUser("Giovanni", "Bianchi", Role.GUEST);

        URI uri = UriComponentsBuilder.fromPath("/odata/Users")
                .queryParam("$top", "2")
                .build().toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        int count = response.getBody().split("\"name\"").length - 1;
        assertThat(count).isEqualTo(2);
    }

    @Test
    void getUsers_withSkip_shouldSkipResults() {
        createUser("Mario", "Rossi", Role.ADMIN);
        createUser("Luigi", "Verdi", Role.USER);
        createUser("Giovanni", "Bianchi", Role.GUEST);

        URI uri = UriComponentsBuilder.fromPath("/odata/Users")
                .queryParam("$skip", "2")
                .build().toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        int count = response.getBody().split("\"name\"").length - 1;
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getUsers_withOrderBySurnameDesc_shouldReturnOrderedResults() {
        createUser("Mario", "Rossi", Role.ADMIN);
        createUser("Luigi", "Bianchi", Role.USER);

        URI uri = UriComponentsBuilder.fromPath("/odata/Users")
                .queryParam("$orderby", "surname desc")
                .build().toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();
        assertThat(body.indexOf("Rossi")).isLessThan(body.indexOf("Bianchi"));
    }

    @Test
    void getUsers_withSelect_shouldReturnOnlySelectedFields() {
        createUser("Mario", "Rossi", Role.ADMIN);

        URI uri = UriComponentsBuilder.fromPath("/odata/Users")
                .queryParam("$select", "id,name")
                .build().toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\"");
        assertThat(response.getBody()).doesNotContain("\"surname\"");
    }
}
