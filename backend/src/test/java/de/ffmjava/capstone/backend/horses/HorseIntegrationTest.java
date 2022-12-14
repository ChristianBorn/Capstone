package de.ffmjava.capstone.backend.horses;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ffmjava.capstone.backend.horses.model.Horse;
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
class HorseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "Basic")
    void getAllHorses() throws Exception {
        mockMvc.perform(get
                        ("/horses/"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void addNewHorse_AndExpectSuccessMessage_201() throws Exception {
        String jsonString =
                """
                            {
                              "name": "Hansi",
                              "owner": "Peter Pan",
                              "consumptionList": []
                            }
                        """;
        mockMvc.perform(post("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                )
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Hansi"))
                .andExpect(jsonPath("$.owner").value("Peter Pan"))
                .andExpect(jsonPath("$.consumptionList").isEmpty());
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void addNewHorse_AndExpectErrorMessage_400() throws Exception {
        String jsonString =
                """
                            {
                              "name": "",
                              "owner": "Peter Pan",
                              "consumptionList": []
                            }
                        """;
        mockMvc.perform(post("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                )
                .andExpect(status().is(400))
                .andExpect(content().string("{\"errorMessage\":\"Feld \\\"Name\\\" darf nicht leer sein\",\"fieldName\":\"name\"}"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void deleteHorse_AndExpect_204() throws Exception {
        String jsonString =
                """
                            {
                              "name": "Hansi",
                              "owner": "Peter Pan",
                              "consumptionList": []
                            }
                        """;
        String postResponse = mockMvc.perform(post("/horses/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)
        ).andReturn().getResponse().getContentAsString();


        String idToDelete = objectMapper.readValue(postResponse, Horse.class).id();

        mockMvc.perform(delete
                        ("/horses/" + idToDelete))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "Basic")
    void deleteHorse_AndExpect_404() throws Exception {
        mockMvc.perform(delete
                        ("/horses/1"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Kein Eintrag f??r die gegebene ID gefunden"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putHorse_AndExpectErrorMessage_400() throws Exception {
        String jsonString =
                """
                            {
                              "name": "",
                              "owner": "Peter Pan",
                              "consumptionList": []
                            }
                        """;
        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                )
                .andExpect(status().is(400))
                .andExpect(content().string("{\"errorMessage\":\"Feld \\\"Name\\\" darf nicht leer sein\",\"fieldName\":\"name\"}"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putHorse_AndExpect_201() throws Exception {
        String jsonString =
                """
                            {
                              "id": "1",
                              "name": "Hansi",
                              "owner": "Peter Pan",
                              "consumptionList": []
                            }
                        """;
        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                )
                .andExpect(status().is(201))
                .andExpect(content().json("""
                            {
                              "id": "1",
                              "name": "Hansi",
                              "owner": "Peter Pan",
                              "consumptionList": []
                            }
                        """));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putHorse_AndExpect_200() throws Exception {
        String jsonString =
                """
                            {
                              "name": "Hansi",
                              "owner": "Peter Pan",
                              "consumptionList": []
                            }
                        """;
        String postResponse = mockMvc.perform(post("/horses/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)).andReturn().getResponse().getContentAsString();

        Horse createdHorse = objectMapper.readValue(postResponse, Horse.class);

        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postResponse.replace("Hansi", "Lord Voldemort"))
                )
                .andExpect(status().is(200))
                .andExpect(content().json("""
                            {
                              "id": "<ID>",
                              "name": "Lord Voldemort",
                              "owner": "Peter Pan",
                              "consumptionList": []
                            }
                        """
                        .replace("<ID>", createdHorse.id())));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putHorse_withDuplicatedConsumption_AndExpect_400() throws Exception {
        String jsonString =
                """
                            {
                              "name": "Hansi",
                              "owner": "Peter Pan",
                              "consumptionList": [
                              {
                                "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                "name": "Hafer",
                                "dailyConsumption": "10"
                                }
                                            ]
                            }
                        """;
        mockMvc.perform(post("/horses/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)).andReturn().getResponse().getContentAsString();


        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                      {
                                      "id": "<ID>",
                                      "name": "Lord Voldemort",
                                      "owner": "Peter Pan",
                                      "consumptionList": [
                                      {
                                        "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                        "name": "Hafer",
                                        "dailyConsumption": "10"
                                      },
                                      {
                                        "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                        "name": "Hafer",
                                        "dailyConsumption": "10"
                                      }
                                                    ]
                                    }
                                """)
                )
                .andExpect(status().is(400))
                .andExpect(status().reason("IDs of consumptionItems must be unique for every horse"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putHorse_withNoMatchingStockItem_AndExpect_400() throws Exception {
        String jsonString =
                """
                            {
                              "name": "Hansi",
                              "owner": "Peter Pan",
                              "consumptionList": [
                              {
                                "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                "name": "Hafer",
                                "dailyConsumption": "10"
                                }
                                            ]
                            }
                        """;
        mockMvc.perform(post("/horses/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)).andReturn().getResponse().getContentAsString();


        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                      {
                                      "id": "<ID>",
                                      "name": "Lord Voldemort",
                                      "owner": "Peter Pan",
                                      "consumptionList": [
                                      {
                                        "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                        "name": "Hafer",
                                        "dailyConsumption": "10"
                                      }
                                                    ]
                                    }
                                """)
                )
                .andExpect(status().is(400))
                .andExpect(status().reason("Consumption item not in stock"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putHorse_withNegativeDailyConsumption_AndExpect_400() throws Exception {
        String jsonString =
                """
                            {
                              "name": "Hansi",
                              "owner": "Peter Pan",
                              "consumptionList": [
                              {
                                "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                "name": "Hafer",
                                "dailyConsumption": "10"
                                }
                                            ]
                            }
                        """;
        mockMvc.perform(post("/horses/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)).andReturn().getResponse().getContentAsString();


        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                      {
                                      "id": "<ID>",
                                      "name": "Lord Voldemort",
                                      "owner": "Peter Pan",
                                      "consumptionList": [
                                      {
                                        "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                        "name": "Hafer",
                                        "dailyConsumption": "-10"
                                      }
                                                    ]
                                    }
                                """)
                )
                .andExpect(status().is(400))
                .andExpect(content().string("{\"errorMessage\":\"Der Wert muss gr??????\u009Fer als 0 sein\",\"fieldName\":\"dailyConsumption\"}"));
    }
}
