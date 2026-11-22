package ar.edu.uade.tenisskillgames.infrastructure.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Ejecucion asincrona simulada del despacho de notificaciones (NFR de
 * escalabilidad y colas). En pruebas se desactiva para tener determinismo.
 */
@Component
public class DespachadorAsincrono {

    private final TaskExecutor ejecutor;
    private final boolean asincrono;

    public DespachadorAsincrono(@org.springframework.beans.factory.annotation.Qualifier("applicationTaskExecutor") TaskExecutor ejecutor,
                                @Value("${tenis.notificaciones.asincrono:true}") boolean asincrono) {
        this.ejecutor = ejecutor;
        this.asincrono = asincrono;
    }

    public void ejecutar(Runnable tarea) {
        if (asincrono) {
            ejecutor.execute(tarea);
        } else {
            tarea.run();
        }
    }

    public boolean esAsincrono() {
        return asincrono;
    }
}
