package ec.edu.uta.notificaciones.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de seguridad del servicio de notificaciones.
 *
 * Valida que las solicitudes HTTP incluyan una clave
 * correcta en la cabecera X-Notification-Key.
 */
@Component
public class NotificacionKeyFilter extends OncePerRequestFilter {

    // Almacena la clave configurada para el servicio.
    private final String key;

    /**
     * Constructor:
     * Lee la clave desde application.properties.
     */
    public NotificacionKeyFilter(
            @Value("${servicios.notificaciones.api-key}") String key) {

        // Comprueba que la clave esté configurada.
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "La clave de notificaciones no está configurada");
        }

        this.key = key;
    }

    /**
     * Determina qué rutas necesitan autenticación.
     *
     * false = ejecutar el filtro.
     * true = omitir el filtro.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String ruta = request.getServletPath();

        // Proteger únicamente las rutas de notificaciones.
        return !ruta.equals("/api/notificaciones")
                && !ruta.startsWith("/api/notificaciones/");
    }

    /**
     * Valida la clave recibida en cada solicitud protegida.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain)
            throws ServletException, IOException {

        // 1. Leer la clave enviada desde Postman o la API de vehículos.
        String claveRecibida = req.getHeader("X-Notification-Key");

        // 2. Convertir la clave configurada a bytes.
        byte[] claveEsperada = key.getBytes(StandardCharsets.UTF_8);

        // 3. Comprobar que se envió la clave y que coincide.
        boolean claveValida = claveRecibida != null
                && MessageDigest.isEqual(
                        claveEsperada,
                        claveRecibida.getBytes(StandardCharsets.UTF_8));

        // 4. Rechazar las solicitudes con clave incorrecta.
        if (!claveValida) {

            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/problem+json");
            res.setCharacterEncoding("UTF-8");

            res.getWriter().write(
                    "{\"title\":\"Servicio no autorizado\","
                            + "\"status\":401,"
                            + "\"detail\":\"X-Notification-Key inválida\"}");

            return;
        }

        // 5. Si la clave es correcta, continuar al controlador.
        chain.doFilter(req, res);
    }
}