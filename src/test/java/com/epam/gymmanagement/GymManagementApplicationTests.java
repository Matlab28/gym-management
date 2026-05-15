package com.epam.gymmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.epam.gymmanagement.repository.TraineeRepository;
import com.epam.gymmanagement.repository.TrainingRepository;
import com.epam.gymmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GymManagementApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TraineeRepository traineeRepository;
    @Autowired
    private TrainingRepository trainingRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void contextLoads() {
    }

    @Test
    void traineeProfileRequiresValidJwt() throws Exception {
        Registration firstTrainee = registerTrainee("Jwt", "Owner" + System.nanoTime());
        Registration secondTrainee = registerTrainee("Jwt", "Other" + System.nanoTime());

        mockMvc.perform(get("/api/v1/trainees/profile/{username}", firstTrainee.username()))
                .andExpect(status().isUnauthorized());

        String firstToken = login(firstTrainee.username(), firstTrainee.password());
        String secondToken = login(secondTrainee.username(), secondTrainee.password());

        mockMvc.perform(get("/api/v1/trainees/profile/{username}", firstTrainee.username())
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(firstTrainee.username()));

        mockMvc.perform(get("/api/v1/trainees/profile/{username}", firstTrainee.username())
                        .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/trainees/profile/{username}", firstTrainee.username())
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
    void h2ConsoleIsRegistered() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void openApiDocsLoadWithGlobalExceptionHandlerEnabled() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());
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

    @Test
    void deletingTraineeHardDeletesProfileUserAndRelevantTrainings() throws Exception {
        Registration trainee = registerTrainee("Delete", "Trainee" + System.nanoTime());
        Registration trainer = registerTrainer("Delete", "Trainer" + System.nanoTime(), "Fitness");
        String traineeToken = login(trainee.username(), trainee.password());
        String trainerToken = login(trainer.username(), trainer.password(), "TRAINER");

        assignTrainer(trainee.username(), trainer.username(), traineeToken);
        addTraining(trainee.username(), trainer.username(), trainerToken);

        mockMvc.perform(delete("/api/v1/trainees/delete/{username}", trainee.username())
                        .header("Authorization", "Bearer " + traineeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Trainee profile deleted successfully"));

        assertFalse(userRepository.existsByUsername(trainee.username()));
        assertFalse(traineeRepository.existsByUserEntity_Username(trainee.username()));
        assertFalse(trainingRepository.findTraineeTrainings(
                trainee.username(),
                null,
                null,
                null,
                null
        ).iterator().hasNext());
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

    private Registration registerTrainer(String firstName, String lastName, String specialization) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "specialization", specialization
        ));

        String response = mockMvc.perform(post("/api/v1/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return new Registration(json.get("username").asText(), json.get("password").asText());
    }

    private void assignTrainer(String traineeUsername, String trainerUsername, String traineeToken) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "trainers", new Object[]{
                        Map.of("trainerUsername", trainerUsername)
                }
        ));

        mockMvc.perform(put("/api/v1/trainees/update/{username}/trainers", traineeUsername)
                        .header("Authorization", "Bearer " + traineeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private void addTraining(String traineeUsername, String trainerUsername, String trainerToken) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "traineeUsername", traineeUsername,
                "trainerUsername", trainerUsername,
                "trainingName", "Hibernate cascade check",
                "trainingDate", "2026-01-20",
                "trainingType", "Fitness",
                "trainingDuration", 45
        ));

        mockMvc.perform(post("/api/v1/trainings/add")
                        .header("Authorization", "Bearer " + trainerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private String login(String username, String password) throws Exception {
        return login(username, password, "TRAINEE");
    }

    private String login(String username, String password, String role) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", password
        ));

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value(role))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private record Registration(String username, String password) {
    }
}
