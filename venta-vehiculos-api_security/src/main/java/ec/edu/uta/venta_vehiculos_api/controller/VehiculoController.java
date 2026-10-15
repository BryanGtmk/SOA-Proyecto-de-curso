// Define el paquete dentro del proyecto donde se ubica el controlador REST
package ec.edu.uta.venta_vehiculos_api.controller;

// Importa la interfaz List para retornar colecciones de objetos
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.uta.venta_vehiculos_api.model.Vehiculo;
import ec.edu.uta.venta_vehiculos_api.service.VehiculoService;

import java.util.Map;

import org.hibernate.annotations.DialectOverride.Version;
import org.springframework.beans.factory.annotation.Value;

// Anotación que indica que la clase es un controlador REST y convierte los retornos automáticamente a JSON
@RestController
// Define la ruta base (/api/vehiculos) para todos los endpoints de este controlador
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    // Variable de instancia inmutable para la inyección del servicio de vehículos
    private final VehiculoService service;

    @Value("${server.port}") // Inyecta el valor de la propiedad 'server.port' desde application.properties
    private int puerto; // Variable para almacenar la versión del servidor

    // Constructor que realiza la inyección de dependencias recomendada del servicio
    public VehiculoController(
            VehiculoService service) {

        // Asigna la instancia inyectada de VehiculoService
        this.service = service;
    }

    // Mapea peticiones GET a la ruta /api/vehiculos/instancia
    @GetMapping ("/instancia") // Mapea peticiones GET a la ruta /api/vehiculos/instancia
    public ResponseEntity<Map<String, Object>> obtenerInstancia() { // Método que retorna un mapa con información de la instancia

        // Crea un mapa con la versión del servidor y el puerto en el que está corriendo
        return ResponseEntity.ok(
                Map.of(
                        "servicio", "venta_vehiculos_api",
                        "instancia", "API - Puerto: " + puerto,
                        "puerto", puerto,
                        "mensaje", "Instancia de la API de venta de vehículos"
                )
        );
    }

    // Mapea peticiones GET a la ruta base /api/vehiculos
    @GetMapping
    public ResponseEntity<List<Vehiculo>>
    listar(
            // Extrae el parámetro opcional de consulta 'marca' desde la URL (?marca=...)
            @RequestParam(required = false)
            String marca) {

        // Evalúa si el parámetro 'marca' fue provisto y no está en blanco
        if (marca != null &&
                !marca.isBlank()) {

            // Retorna respuesta HTTP 200 (OK) con la lista filtrada por marca
            return ResponseEntity.ok(
                    service.buscarPorMarca(marca)
            );
        }

        // Retorna respuesta HTTP 200 (OK) con todos los vehículos registrados
        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    // Mapea peticiones GET con un ID dinámico en la ruta (/api/vehiculos/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo>
    buscar(@PathVariable Integer id) { // Extrae la variable {id} de la URL

        // Invoca el servicio y retorna el vehículo encontrado con estado HTTP 200 (OK)
        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    // Mapea peticiones POST para registrar un nuevo vehículo en la ruta base
    @PostMapping
    public ResponseEntity<Vehiculo>
    crear(@RequestBody Vehiculo vehiculo) { // Transforma el JSON del cuerpo de la petición en un objeto Vehiculo

        // Ejecuta la lógica de creación en la capa de servicio
        Vehiculo creado =
                service.crear(vehiculo);

        // Retorna la respuesta con código de estado HTTP 201 (CREATED) y el vehículo recién creado
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    // Mapea peticiones PUT para actualizar un vehículo existente identificándolo por {id}
    @PutMapping("/{id}")
    public ResponseEntity<Vehiculo>
    actualizar(
            @PathVariable Integer id, // Captura el ID desde la ruta de la URL
            @RequestBody Vehiculo vehiculo) { // Captura los nuevos datos desde el cuerpo JSON

        // Retorna respuesta HTTP 200 (OK) con el objeto actualizado tras procesarlo en el servicio
        return ResponseEntity.ok(
                service.actualizar(
                        id,
                        vehiculo
                )
        );
    }

    // Mapea peticiones DELETE para eliminar un vehículo por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    eliminar(@PathVariable Integer id) { // Captura el ID del vehículo a eliminar desde la URL

        // Llama al servicio para realizar la eliminación del vehículo
        service.eliminar(id);

        // Retorna una respuesta HTTP 200 (OK) sin cuerpo de contenido
        return ResponseEntity.ok().build();
    }
}