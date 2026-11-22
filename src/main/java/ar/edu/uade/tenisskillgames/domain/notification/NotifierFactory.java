package ar.edu.uade.tenisskillgames.domain.notification;

/**
 * Patron Abstract Factory: produce la familia coherente de notificadores
 * segun el entorno de ejecucion (RF7).
 */
public interface NotifierFactory {

    Notifier createPushNotifier();

    Notifier createEmailNotifier();

    Notifier createChatNotifier();
}
