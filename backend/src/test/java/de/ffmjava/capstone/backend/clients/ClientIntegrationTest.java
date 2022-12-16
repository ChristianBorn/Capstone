package de.ffmjava.capstone.backend.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ffmjava.capstone.backend.clients.model.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class ClientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "Basic")
    void getAllClients_AndExpectEmptyList_200() throws Exception {
        mockMvc.perform(get
                        ("/clients/"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void addNewClient_AndExpectClient_201() throws Exception {
        String jsonString =
                """
                            {
                              "name": "Einstaller",
                              "ownsHorse": []
                            }
                        """;
        mockMvc.perform(post("/clients/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                )
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").isNotEmpty())
                .andExpect(jsonPath("$.ownsHorse").isEmpty());
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void addNewClient_AndExpectErrorMessage_400() throws Exception {
        String jsonString =
                """
                            {
                              "name": "",
                              "ownsHorse": []
                            }
                        """;
        mockMvc.perform(post("/clients/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                )
                .andExpect(status().is(400))
                .andExpect(content().string("{\"errorMessage\":\"Feld \\\"Name\\\" darf nicht leer sein\",\"fieldName\":\"name\"}"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void deleteClient_AndExpect_204() throws Exception {
        String jsonString =
                """
                            {
                              "name": "Name",
                              "ownsHorse": []
                            }
                        """;
        String postResponse = mockMvc.perform(post("/clients/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)
        ).andReturn().getResponse().getContentAsString();


        String idToDelete = objectMapper.readValue(postResponse, Client.class).id();

        mockMvc.perform(delete
                        ("/clients/" + idToDelete))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "Basic")
    void deleteClient_AndExpect_404() throws Exception {
        mockMvc.perform(delete
                        ("/clients/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putClient_AndExpect_200() throws Exception {
        String jsonString =
                """
                            {
                              "name": "Name",
                              "ownsHorse": []
                            }
                        """;
        String postResponse = mockMvc.perform(post("/clients/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)).andReturn().getResponse().getContentAsString();

        Client createdClient = objectMapper.readValue(postResponse, Client.class);

        mockMvc.perform(put("/clients/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postResponse.replace("Name", "Lord Voldemort"))
                )
                .andExpect(status().is(200))
                .andExpect(content().json("""
                            {
                              "id": "<ID>",
                              "name": "Lord Voldemort",
                              "ownsHorse": []
                            }
                        """
                        .replace("<ID>", createdClient.id())));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putClient_AndExpect_DuplicateOwnsHorse_400() throws Exception {
        String jsonString =
                """
                            {
                               "id": "6b5a9ae3-4edf-4c5f-9095-3e6f031cb732",
                               "name": "Client",
                               "ownsHorse": [
                                 {
                                   "id": "b88bc0b4-0638-488a-8b3d-3990dd61bd3c",
                                   "name": "test",
                                   "owner": "test",
                                   "consumptionList": [
                                     {
                                       "id": "b40bd21b-6fbe-4291-9c23-7c80039b1eaf",
                                       "name": "Hafer",
                                       "dailyConsumption": "1"
                                     },
                                     {
                                       "id": "e9c949de-252a-4e9e-86df-abd64314b4fa",
                                       "name": "Müsli",
                                       "dailyConsumption": "1123123"
                                     }
                                   ]
                                 },
                                 {
                                   "id": "b88bc0b4-0638-488a-8b3d-3990dd61bd3c",
                                   "name": "test",
                                   "owner": "test",
                                   "consumptionList": [
                                     {
                                       "id": "b40bd21b-6fbe-4291-9c23-7c80039b1eaf",
                                       "name": "Hafer",
                                       "dailyConsumption": "1"
                                     },
                                     {
                                       "id": "e9c949de-252a-4e9e-86df-abd64314b4fa",
                                       "name": "Müsli",
                                       "dailyConsumption": "1123123"
                                     }
                                   ]
                                 }
                               ]
                             }
                        """;

        mockMvc.perform(put("/clients/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                )
                .andExpect(status().is(400));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putClient_AndExpect_EmptyName_400() throws Exception {
        String jsonString =
                """
                            {
                               "id": "6b5a9ae3-4edf-4c5f-9095-3e6f031cb732",
                               "name": "",
                               "ownsHorse": []
                             }
                        """;

        mockMvc.perform(put("/clients/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                )
                .andExpect(status().is(400));
    }
}
