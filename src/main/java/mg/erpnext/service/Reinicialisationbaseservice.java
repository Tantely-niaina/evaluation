package mg.erpnext.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;


@Service
public class Reinicialisationbaseservice {
    
    private static final Logger logger = LoggerFactory.getLogger(Reinicialisationbaseservice.class);
    
    public void reinitialiserBaseDonnees(HttpSession session) throws Exception {
        logger.info("Début de la méthode reinitialiserBaseDonnees");
        String sid = (String) session.getAttribute("frappe_sid");
        logger.info("Vérification du frappe_sid dans reinitialiserBaseDonnees : {}", sid);

        if (sid == null || sid.isEmpty()) {
            logger.info("Aucun SID trouvé en session, tentative de connexion à ERPNext...");
            // Connexion à ERPNext avec l'utilisateur Administrator
            sid = seConnecterEtRecupererSid(session);
            if (sid == null) {
                logger.error("Impossible de récupérer le SID ERPNext.");
                throw new RuntimeException("Impossible de récupérer le SID ERPNext.");
            }
            logger.info("SID récupéré et stocké en session : {}", sid);
        }

        String requestUrl = "http://erpnext.localhost:8000/api/method/erpnext.erpnext_integrations.import.import_extract.reinitialiser_les_donner_dans_le_base";
        logger.info("URL de la requête de réinitialisation : {}", requestUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", sid);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            logger.info("Envoi de la requête POST à ERPNext pour réinitialiser la base de données...");
            ResponseEntity<String> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.POST,
                request,
                String.class
            );
            logger.info("Réponse reçue du serveur ERPNext : {}", response.getBody());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode message = root.path("message");

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Statut HTTP OK reçu.");
                if (!message.isMissingNode()) {
                    String status = message.path("status").asText();
                    logger.info("Statut de la réponse ERPNext : {}", status);

                    if ("success".equals(status)) {
                        logger.info("Réinitialisation réussie: {}", message.path("message").asText());
                        System.out.println("✅ Réinitialisation réussie: " + message.path("message").asText());
                    } else {
                        logger.error("Erreur lors de la réinitialisation: {}", message.path("error").asText());
                        throw new RuntimeException("❌ Erreur lors de la réinitialisation: " + message.path("error").asText());
                    }
                } else {
                    logger.error("Réponse inattendue du serveur : champ 'message' manquant.");
                    throw new RuntimeException("❌ Réponse inattendue du serveur.");
                }
            } else {
                logger.error("Échec de la requête, code HTTP : {}", response.getStatusCode());
                throw new RuntimeException("❌ Échec de la requête: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Erreur HTTP lors de la communication avec ERPNext : {}", e.getStatusCode(), e);
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new RuntimeException("Accès refusé - Vérifiez les permissions dans ERPNext");
            }
            throw e;
        } catch (RestClientException e) {
            logger.error("Erreur de communication avec ERPNext : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur de communication avec ERPNext: " + e.getMessage());
        }
    }


    private String seConnecterEtRecupererSid(HttpSession session) {
        logger.info("Début de la méthode seConnecterEtRecupererSid");
        String loginUrl = "http://erpnext.localhost:8000/api/method/login";
        logger.info("URL de connexion ERPNext : {}", loginUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Remplacez par vos vrais identifiants ERPNext
        String username = "Administrator";
        String password = "witty";  // ⚠️ Mettez ici le vrai mot de passe

        logger.info("Tentative de connexion avec l'utilisateur : {}", username);

        MultiValueMap<String, String> loginData = new LinkedMultiValueMap<>();
        loginData.add("usr", username);
        loginData.add("pwd", password);

        HttpEntity<MultiValueMap<String, String>> loginRequest = new HttpEntity<>(loginData, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            logger.info("Envoi de la requête de connexion à ERPNext...");
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(loginUrl, loginRequest, String.class);
            logger.info("Réponse de connexion ERPNext : {}", loginResponse.getBody());

            // Récupérer le sid depuis les cookies
            List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies != null) {
                for (String cookie : cookies) {
                    logger.debug("Cookie reçu : {}", cookie);
                    if (cookie.startsWith("sid=")) {
                        String sid = cookie.split(";")[0].split("=")[1];
                        logger.info("SID ERPNext récupéré : {}", sid);
                        session.setAttribute("frappe_sid", sid); // Mémoriser en session
                        return sid;
                    }
                }
                logger.warn("Aucun cookie SID trouvé dans la réponse.");
            } else {
                logger.warn("Aucun cookie reçu dans la réponse de connexion.");
            }
        } catch (Exception e) {
            logger.error("Échec de connexion à ERPNext : {}", e.getMessage(), e);
            System.err.println("Échec de connexion à ERPNext : " + e.getMessage());
        }

        logger.error("Impossible de récupérer le SID ERPNext après tentative de connexion.");
        return null;
    }

}