package com.epam.gymmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GymManagementApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void contextLoads() {
    }

    @Test
    void traineeProfileRequiresValidJwt() throws Exception {
        Registration firstTrainee = registerTrainee("Jwt", "Owner" + System.nanoTime());
        Registration secondTrainee = registerTrainee("Jwt", "Other" + System.nanoTime());

        mockMvc.perform(get("/api/v1/trainees/{username}", firstTrainee.username()))
                .andExpect(status().isUnauthorized());

        String firstToken = login(firstTrainee.username(), firstTrainee.password());
        String secondToken = login(secondTrainee.username(), secondTrainee.password());

        mockMvc.perform(get("/api/v1/trainees/{username}", firstTrainee.username())
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(firstTrainee.username()));

        mockMvc.perform(get("/api/v1/trainees/{username}", firstTrainee.username())
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/trainees/{username}", firstTrainee.username())
                        .header("Authorization", "Bearer Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(firstTrainee.username()));
    }

    @Test
    void corsPreflightDoesNotRequireJwt() throws Exception {
        mockMvc.perform(options("/api/v1/trainees/some.user")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk());
    }

    @Test
    void adminDashboardRequiresAdminRole() throws Exception {
        Registration trainee = registerTrainee("Admin", "Blocked" + System.nanoTime());
        String traineeToken = login(trainee.username(), trainee.password());

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + traineeToken))
                .andExpect(status().isForbidden());
    }

    private Registration registerTrainee(String firstName, String lastName) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "dateOfBirth", "2000-01-01",
                "address", "Main street"
        ));

        String response = mockMvc.perform(post("/api/v1/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return new Registration(json.get("username").asText(), json.get("password").asText());
    }

    private String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("TRAINEE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private record Registration(String username, String password) {
    }
}
