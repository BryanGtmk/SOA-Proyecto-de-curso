package ec.edu.uta.venta_vehiculos_api.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import ec.edu.uta.venta_vehiculos_api.dto.NotificacionRequest;
import ec.edu.uta.venta_vehiculos_api.dto.NotificacionResponse;

@Component
public class NotificacionClient {

    private final RestClient restClient;

    public NotificacionClient(
            @Value("${servicios.notificaciones.url}")
            String url) {

        this.restClient = RestClient
                .builder()
                .baseUrl(url)
                .build();
    }

    public void enviar(
            Integer vehiculoId,
            String tipo,
            String mensaje) {

        NotificacionRequest request =
                new NotificacionRequest(
                        vehiculoId,
                        "javier@uta.edu.ec",
                        tipo,
                        mensaje
                );

        try {

            NotificacionResponse respuesta =
                    restClient
                            .post()
                            .uri("/api/notificaciones")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(request)
                            .retrieve()
                            .body(NotificacionResponse.class);

            if (respuesta != null) {

                System.out.println(
                        "NOTIFICACIÓN: "
                                + respuesta.getMensaje()
                );
            }

        } catch (RestClientException e) {

            System.out.println(
                    "No se pudo contactar con "
                            + "notificaciones-service"
            );
        }
    }
}