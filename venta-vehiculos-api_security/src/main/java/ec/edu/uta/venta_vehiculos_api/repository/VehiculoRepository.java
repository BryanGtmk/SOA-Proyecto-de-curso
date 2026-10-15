// Define el paquete dentro del proyecto donde se ubica la interfaz del repositorio
package ec.edu.uta.venta_vehiculos_api.repository;

// Importa la interfaz List para manejar colecciones de múltiples objetos
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.uta.venta_vehiculos_api.model.Vehiculo;

// Declaración de la interfaz del repositorio para la entidad Vehiculo
public interface VehiculoRepository
        // Extiende JpaRepository indicando la entidad (Vehiculo) y el tipo de dato de su clave primaria (Integer)
        extends JpaRepository<Vehiculo, Integer> {

    // Método de consulta derivado para buscar un vehículo por su placa exacta (retorna Optional para evitar nulos)
    Optional<Vehiculo> findByPlaca(String placa);

    // Método de consulta derivado para buscar un vehículo por su número de chasis exacto
    Optional<Vehiculo> findByChasis(String chasis);

    // Busca coincidencia parcial en el campo marca sin hacer distinción entre mayúsculas y minúsculas
    List<Vehiculo> findByMarcaContainingIgnoreCase(String marca);
}