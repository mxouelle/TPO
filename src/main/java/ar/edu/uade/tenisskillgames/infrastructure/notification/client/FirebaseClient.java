package ar.edu.uade.tenisskillgames.infrastructure.notification.client;

/**
 * Cliente de notificaciones Push (Firebase Cloud Messaging).
 */
public interface FirebaseClient {

    void sendPush(String token, String titulo, String cuerpo);
}
