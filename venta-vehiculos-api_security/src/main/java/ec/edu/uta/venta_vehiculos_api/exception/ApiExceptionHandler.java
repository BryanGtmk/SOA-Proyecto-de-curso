// Define el paquete donde se ubica la clase para la gestión global de excepciones
package ec.edu.uta.venta_vehiculos_api.exception;

// Importa la interfaz Map para estructurar las respuestas de error en pares clave-valor
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Anotación que convierte esta clase en un capturador global de excepciones para la API REST
@RestControllerAdvice
public class ApiExceptionHandler {

    // Define que este método interceptará todas las excepciones de tipo IllegalArgumentException
    @ExceptionHandler(
            IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>>
    manejarBadRequest(
            IllegalArgumentException e) { // Recibe el objeto de la excepción capturada

        // Retorna una respuesta HTTP personalizada
        return ResponseEntity
                // Asigna el código de estado HTTP 400 Bad Request
                .status(HttpStatus.BAD_REQUEST)
                // Construye el cuerpo JSON de respuesta con un mapa inmutable
                .body(Map.of(
                        "success", false, // Indicar estado de fallo
                        "mensaje", e.getMessage() // Captura y envía el mensaje explicativo del error
                ));
    }

    // Define que este método interceptará todas las excepciones de tipo IllegalStateException
    @ExceptionHandler(
            IllegalStateException.class)
    public ResponseEntity<Map<String, Object>>
    manejarConflict(
            IllegalStateException e) { // Recibe la excepción lanzada cuando hay conflictos de estado

        // Retorna una respuesta HTTP personalizada
        return ResponseEntity
                // Asigna el código de estado HTTP 409 Conflict
                .status(HttpStatus.CONFLICT)
                // Construye el cuerpo JSON de respuesta indicando fallo y mensaje
                .body(Map.of(
                        "success", false, // Indicar estado de fallo
                        "mensaje", e.getMessage() // Obtiene el mensaje del conflicto
                ));
    }

    // Define que este método interceptará las excepciones genéricas de tipo RuntimeException
    @ExceptionHandler(
            RuntimeException.class)
    public ResponseEntity<Map<String, Object>>
    manejarNotFound(
            RuntimeException e) { // Recibe la excepción enviada al no encontrar registros

        // Retorna una respuesta HTTP personalizada
        return ResponseEntity
                // Asigna el código de estado HTTP 404 Not Found
                .status(HttpStatus.NOT_FOUND)
                // Construye el cuerpo JSON de respuesta con la estructura de error
                .body(Map.of(
                        "success", false, // Indicar estado de fallo
                        "mensaje", e.getMessage() // Retorna el mensaje de recurso no encontrado
                ));
    }
}