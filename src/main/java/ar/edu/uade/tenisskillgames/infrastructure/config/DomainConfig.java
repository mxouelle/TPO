package ar.edu.uade.tenisskillgames.infrastructure.config;

import ar.edu.uade.tenisskillgames.domain.event.DomainEventBus;
import ar.edu.uade.tenisskillgames.domain.event.DomainEventSubscriber;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Registro de los suscriptores (Observer) en el bus de eventos de dominio.
 * Se usa SmartInitializingSingleton para romper la dependencia circular
 * bus -> suscriptores -> repositorios -> publicador -> bus.
 */
@Configuration
public class DomainConfig {

    @Bean
    public DomainEventBus domainEventBus() {
        return new DomainEventBus();
    }

    @Bean
    public org.springframework.beans.factory.SmartInitializingSingleton registrarSuscriptores(
            DomainEventBus bus, ObjectProvider<List<DomainEventSubscriber>> suscriptores) {
        return () -> {
            List<DomainEventSubscriber> lista = suscriptores.getIfAvailable(List::of);
            lista.forEach(bus::subscribe);
        };
    }
}
