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
    void deleteHorse_AndExpect_204() throws Exception {
        String jsonString =
                """
                            {
                                "id": "",
                                "name": "Hansi",
                                "owner": null,
                                "consumptionList": []
                            }
                        """;
        String postResponse = mockMvc.perform(put("/horses/")
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
                .andExpect(status().reason("Kein Eintrag für die gegebene ID gefunden"));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putHorse_AndExpectErrorMessage_400() throws Exception {
        String jsonString =
                """
                            {
                              "id": "",
                              "name": "",
                              "owner": null,
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
                              "owner": null,
                              "consumptionList": []
                            }
                        """;
        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                )
                .andExpect(status().is(201));
    }

    @Test
    @DirtiesContext
    @WithMockUser(roles = "Basic")
    void putHorse_withDuplicatedConsumption_AndExpect_400() throws Exception {
        String jsonString =
                """
                            {
                            "id": "",
                            "name": "Hansi",
                            "owner": null,
                            "consumptionList": [
                                {
                                "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                "name": "Hafer",
                                "dailyConsumption": "10"
                                }
                            ]
                            }
                        """;
        mockMvc.perform(put("/horses/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)).andReturn().getResponse().getContentAsString();

        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                      {
                                      "id": "<ID>",
                                      "name": "Lord Voldemort",
                                      "owner": null,
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
                              "id": "",
                              "name": "Hansi",
                              "owner": null,
                              "consumptionList": [
                                {
                                "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                "name": "Hafer",
                                "dailyConsumption": "10"
                                }
                               ]
                            }
                        """;
        mockMvc.perform(put("/horses/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)).andReturn().getResponse().getContentAsString();


        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                      {
                                      "id": "<ID>",
                                      "name": "Lord Voldemort",
                                      "owner": null,
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
                              "id": "",
                              "name": "Hansi",
                              "owner": null,
                              "consumptionList": [
                              {
                                "id": "43279367-20b8-4b7e-891f-0c8d2a2428d2",
                                "name": "Hafer",
                                "dailyConsumption": "10"
                               }
                                    ]
                            }
                        """;
        mockMvc.perform(put("/horses/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)).andReturn().getResponse().getContentAsString();


        mockMvc.perform(put("/horses/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                      {
                                      "id": "<ID>",
                                      "name": "Lord Voldemort",
                                      "owner": null,
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
                .andExpect(content().string("{\"errorMessage\":\"Der Wert muss grÃ¶Ã\u009Fer als 0 sein\",\"fieldName\":\"dailyConsumption\"}"));
    }
}
