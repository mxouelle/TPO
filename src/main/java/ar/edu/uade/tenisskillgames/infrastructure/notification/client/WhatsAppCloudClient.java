package ar.edu.uade.tenisskillgames.infrastructure.notification.client;

/**
 * Cliente de bajo nivel de WhatsApp Cloud API (Meta). Es la API externa
 * incompatible que el Adapter adapta al contrato Notifier del dominio.
 */
public interface WhatsAppCloudClient {

    /**
     * @param telefono destino en formato E.164
     * @param plantilla nombre de la plantilla aprobada por Meta
     * @param parametros valores posicionales de la plantilla
     */
    void sendTemplateMessage(String telefono, String plantilla, String... parametros);
}
