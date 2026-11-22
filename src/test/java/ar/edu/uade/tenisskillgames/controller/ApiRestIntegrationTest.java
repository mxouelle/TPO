package ar.edu.uade.tenisskillgames.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato REST: registro, verificacion, login con JWT y creacion de partido.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiRestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private JsonNode postJson(String url, String body, String token) throws Exception {
        var request = post(url).contentType(MediaType.APPLICATION_JSON).content(body);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        MvcResult result = mockMvc.perform(request).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Registro, verificacion de email, login y creacion de partido por API")
    void flujoRestCompleto() throws Exception {
        String email = "api.rest." + System.nanoTime() + "@test.com";
        String registro = """
                {
                  "nombre": "Jugador API",
                  "email": "%s",
                  "password": "Password123",
                  "nivelNTRP": 3.5,
                  "zona": "Núñez",
                  "superficieFavorita": "POLVO_DE_LADRILLO",
                  "manoHabil": "DIESTRO",
                  "golpePreferido": "DRIVE",
                  "latitud": -34.5453,
                  "longitud": -58.4498,
                  "telefono": "+5491155550001",
                  "pushToken": "push-api-test",
                  "franjasHorarias": ["LUN_18-22"]
                }
                """.formatted(email);

        MvcResult respuestaRegistro = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.estadoVerificacion").value("PENDIENTE"))
                .andExpect(jsonPath("$.tokenVerificacion").isNotEmpty())
                .andReturn();

        String tokenVerificacion = mapper.readTree(respuestaRegistro.getResponse().getContentAsString())
                .get("tokenVerificacion").asText();

        mockMvc.perform(post("/api/auth/verificar-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + tokenVerificacion + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoVerificacion").value("VERIFICADO"));

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = mapper.readTree(login.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        String fecha = LocalDateTime.now().plusDays(3)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        MvcResult creacion = mockMvc.perform(post("/api/partidos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formato": "SINGLES",
                                  "superficie": "POLVO_DE_LADRILLO",
                                  "clubSede": "Club API",
                                  "direccion": "Calle Falsa 123",
                                  "latitud": -34.5453,
                                  "longitud": -58.4498,
                                  "ntrpMin": 3.0,
                                  "ntrpMax": 4.5,
                                  "fechaHora": "%s",
                                  "duracionMinutos": 90,
                                  "modalidad": "AMISTOSO_RECREATIVO",
                                  "radioMaxKm": 15,
                                  "estrategia": "NTRP"
                                }
                                """.formatted(fecha)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BUSCANDO_JUGADORES"))
                .andReturn();

        String partidoId = mapper.readTree(creacion.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/partidos").param("superficie", "POLVO_DE_LADRILLO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        mockMvc.perform(get("/api/partidos/" + partidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubSede").value("Club API"));
    }

    @Test
    @DisplayName("Sin token los endpoints protegidos responden 401")
    void seguridadSinToken() throws Exception {
        mockMvc.perform(post("/api/partidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("El registro con email duplicado responde 409")
    void emailDuplicado() throws Exception {
        String email = "duplicado." + System.nanoTime() + "@test.com";
        String body = """
                {"nombre":"Dup","email":"%s","password":"Password123","nivelNTRP":3.0,
                 "zona":"Centro","latitud":-34.6,"longitud":-58.4}
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Un nivel NTRP fuera de rango se rechaza con 400 por validacion de entrada")
    void nivelNtrpInvalido() throws Exception {
        String body = """
                {"nombre":"Mal Nivel","email":"mal.%d@test.com","password":"Password123",
                 "nivelNTRP":9.0,"zona":"Centro","latitud":-34.6,"longitud":-58.4}
                """.formatted(System.nanoTime());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.nivelNTRP").exists());
    }
}
