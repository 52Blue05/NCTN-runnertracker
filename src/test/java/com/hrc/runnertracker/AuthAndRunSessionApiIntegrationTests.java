package com.hrc.runnertracker;

import com.hrc.runnertracker.entity.User;
import com.hrc.runnertracker.repository.RunSessionRepository;
import com.hrc.runnertracker.repository.UserRepository;
import com.hrc.runnertracker.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndRunSessionApiIntegrationTests {

    private static final String PASSWORD = "secret123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RunSessionRepository runSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        runSessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerValidatesInputHashesPasswordSavesUserAndReturnsProfile() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "ab",
                                "email", "not-an-email",
                                "password", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerPayload("runner_alice", PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.username").value("runner_alice"))
                .andExpect(jsonPath("$.data.email").value("runner_alice@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Runner runner_alice"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        User savedUser = userRepository.findByUsername("runner_alice").orElseThrow();
        assertNotNull(savedUser.getId());
        assertNotEquals(PASSWORD, savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(PASSWORD, savedUser.getPasswordHash()));
    }

    @Test
    void loginAuthenticatesGeneratesJwtAndReturnsAuthResponse() throws Exception {
        register("runner_login", PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "runner_login",
                                "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.username").value("runner_login"))
                .andExpect(jsonPath("$.data.email").value("runner_login@example.com"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andReturn();

        String token = responseBody(result).at("/data/token").asText();
        assertEquals("runner_login", jwtTokenProvider.extractUsername(token));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "runner_login",
                                "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createRunSessionStoresRunAndCalculatesAveragePace() throws Exception {
        String token = registerAndLogin("runner_create");

        JsonNode data = createRun(token, "2026-06-16T06:00:00", "2026-06-16T06:30:00",
                new BigDecimal("5.000"), 1800).at("/data");

        assertNotNull(data.get("id"));
        assertEquals("runner_create", userRepository.findById(data.get("userId").asLong()).orElseThrow().getUsername());
        assertEquals(5.0, data.get("distanceKm").asDouble(), 0.001);
        assertEquals(1800, data.get("durationSeconds").asInt());
        assertEquals(6.0, data.get("avgPace").asDouble(), 0.001);
        assertEquals(5000, data.get("stepCount").asInt());
        assertEquals("COMPLETED", data.get("status").asText());

        BigDecimal persistedPace = runSessionRepository.findById(data.get("id").asLong()).orElseThrow().getAvgPace();
        assertEquals(0, persistedPace.compareTo(new BigDecimal("6.00")));
    }

    @Test
    void getRunSessionsReturnsOnlyCurrentUserRunsWithPagination() throws Exception {
        String token = registerAndLogin("runner_list");
        String otherToken = registerAndLogin("runner_other");

        createRun(token, "2026-06-16T06:00:00", "2026-06-16T06:30:00", new BigDecimal("5.000"), 1800);
        createRun(token, "2026-06-16T07:00:00", "2026-06-16T07:20:00", new BigDecimal("4.000"), 1200);
        createRun(token, "2026-06-16T08:00:00", "2026-06-16T08:50:00", new BigDecimal("10.000"), 3000);
        createRun(otherToken, "2026-06-16T09:00:00", "2026-06-16T09:15:00", new BigDecimal("3.000"), 900);

        MvcResult result = mockMvc.perform(get("/api/v1/runs")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode data = responseBody(result).get("data");
        assertEquals(2, data.get("content").size());
        assertEquals(3, data.get("totalElements").asLong());
        assertEquals(2, data.get("totalPages").asInt());
        assertEquals("2026-06-16T08:00:00", data.get("content").get(0).get("startTime").asText());
        assertEquals("2026-06-16T07:00:00", data.get("content").get(1).get("startTime").asText());
    }

    @Test
    void getRunSessionReturnsDetailForOwnerOnly() throws Exception {
        String token = registerAndLogin("runner_detail");
        String otherToken = registerAndLogin("runner_detail_other");

        long runId = createRun(token, "2026-06-16T06:00:00", "2026-06-16T06:30:00",
                new BigDecimal("5.000"), 1800).at("/data/id").asLong();

        mockMvc.perform(get("/api/v1/runs/{id}", runId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(runId))
                .andExpect(jsonPath("$.data.avgPace").value(6.0));

        mockMvc.perform(get("/api/v1/runs/{id}", runId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    private Map<String, Object> registerPayload(String username, String password) {
        return Map.of(
                "username", username,
                "email", username + "@example.com",
                "password", password,
                "fullName", "Runner " + username,
                "weight", new BigDecimal("65.50"),
                "height", new BigDecimal("170.00"));
    }

    private void register(String username, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(registerPayload(username, password))))
                .andExpect(status().isCreated());
    }

    private String registerAndLogin(String username) throws Exception {
        register(username, PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        return responseBody(result).at("/data/token").asText();
    }

    private JsonNode createRun(String token, String startTime, String endTime,
                               BigDecimal distanceKm, int durationSeconds) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/runs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "startTime", startTime,
                                "endTime", endTime,
                                "distanceKm", distanceKm,
                                "durationSeconds", durationSeconds,
                                "stepCount", 5000,
                                "polylineData", "[[21.028511,105.804817],[21.029,105.805]]",
                                "status", "completed"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        return responseBody(result);
    }

    private JsonNode responseBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
