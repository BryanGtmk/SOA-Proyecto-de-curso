// Define el paquete donde se encuentra la clase de servicio
package ec.edu.uta.venta_vehiculos_api.service;

// Importa BigDecimal para comparaciones numéricas de dinero de alta precisión
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.uta.venta_vehiculos_api.model.Vehiculo;
import ec.edu.uta.venta_vehiculos_api.repository.VehiculoRepository;

import ec.edu.uta.venta_vehiculos_api.client.NotificacionClient;

// Anotación que marca la clase como un servicio gestionado por el contenedor de Spring
@Service
public class VehiculoService {

    // Variable final para inyección del repositorio de vehículos (inmutable)
    private final VehiculoRepository repository;

    private final NotificacionClient notificacionClient;

    // Constructor para inyección de dependencias recomendada por Spring
    public VehiculoService(VehiculoRepository repository, 
                           NotificacionClient notificacionClient 
    ) {
        // Asigna la instancia del repositorio recibida por parámetro
        this.repository = repository;
        this.notificacionClient = notificacionClient;
    }

    // Método para consultar y obtener la lista completa de vehículos
    public List<Vehiculo> listarTodos() {
        // Llama al método findAll del repositorio para traer todos los registros
        return repository.findAll();
    }

    // Método para obtener un vehículo por su clave primaria (ID)
    public Vehiculo buscarPorId(Integer id) {

        // Busca el vehículo por ID en la base de datos
        return repository.findById(id)
                // Si no encuentra el registro, lanza una excepción de tiempo de ejecución
                .orElseThrow(() ->
                        new RuntimeException(
                                "Vehículo no encontrado"
                        )
                );
    }

    // Método para filtrar vehículos por nombre de marca
    public List<Vehiculo> buscarPorMarca(String marca) {

        // Invoca la búsqueda del repositorio ignorando diferencias entre mayúsculas y minúsculas
        return repository
                .findByMarcaContainingIgnoreCase(marca);
    }

    // Método para registrar un nuevo vehículo en el sistema
    public Vehiculo crear(Vehiculo vehiculo) {

        // Ejecuta la validación de campos obligatorios y reglas de negocio del vehículo
        validarDatos(vehiculo);

        // Verifica si la placa proporcionada ya se encuentra registrada en la base de datos
        if (repository.findByPlaca(
                vehiculo.getPlaca()).isPresent()) {

            // Lanza una excepción de argumento no válido si la placa ya existe
            throw new IllegalArgumentException(
                    "La placa ya se encuentra registrada"
            );
        }

        // Verifica si el número de chasis proporcionado ya se encuentra registrado
        if (repository.findByChasis(
                vehiculo.getChasis()).isPresent()) {

            // Lanza una excepción si el número de chasis está duplicado
            throw new IllegalArgumentException(
                    "El chasis ya se encuentra registrado"
            );
        }

        // Si no se asignó un estado o se envió un texto en blanco
        if (vehiculo.getEstado() == null ||
                vehiculo.getEstado().isBlank()) {

            // Asigna por defecto el estado DISPONIBLE
            vehiculo.setEstado("DISPONIBLE");
        }

        // Guarda el nuevo vehículo en la base de datos y lo retorna
        //return repository.save(vehiculo);
        Vehiculo creado = repository.save(vehiculo);
        notificacionClient.enviar(
                        creado.getIdVehiculo(),
                        "REGISTRO",
                        "Vehiculo resitrado correctamente: " + creado.getPlaca()
        );

        return creado;
    }

    // Método para actualizar la información de un vehículo existente
    public Vehiculo actualizar(
            Integer id,
            Vehiculo datos) {

        // Recupera el registro actual de la base de datos o lanza excepción si no existe
        Vehiculo actual = buscarPorId(id);

        // Valida la integridad y restricciones de los nuevos datos proporcionados
        validarDatos(datos);

        // Busca si existe algún registro con la nueva placa ingresada
        repository.findByPlaca(datos.getPlaca())
                .ifPresent(encontrado -> {

                    // Si la placa existe y pertenece a un vehículo diferente al que se modifica
                    if (!encontrado.getIdVehiculo()
                            .equals(id)) {

                        // Lanza excepción evitando duplicidad de placa entre vehículos distintos
                        throw new IllegalArgumentException(
                                "La placa pertenece a otro vehículo"
                        );
                    }
                });

        // Busca si existe algún registro con el nuevo chasis ingresado
        repository.findByChasis(datos.getChasis())
                .ifPresent(encontrado -> {

                    // Si el chasis existe y pertenece a otro vehículo en la base de datos
                    if (!encontrado.getIdVehiculo()
                            .equals(id)) {

                        // Lanza excepción evitando duplicidad de chasis entre vehículos distintos
                        throw new IllegalArgumentException(
                                "El chasis pertenece a otro vehículo"
                        );
                    }
                });

        // Sobrescribe los atributos del vehículo con los nuevos valores recibidos
        actual.setMarca(datos.getMarca());
        actual.setModelo(datos.getModelo());
        actual.setPlaca(datos.getPlaca());
        actual.setChasis(datos.getChasis());
        actual.setAnio(datos.getAnio());
        actual.setColor(datos.getColor());
        actual.setPrecio(datos.getPrecio());

        // Actualiza el estado únicamente si fue proporcionado un valor no nulo
        if (datos.getEstado() != null) {
            actual.setEstado(datos.getEstado());
        }

        // Persiste los cambios realizados en la base de datos y los retorna
        return repository.save(actual);
    }

    // Método para eliminar un vehículo por su ID
   

      
 
    
    public void eliminar(Integer id) {

    Vehiculo vehiculo =
            buscarPorId(id);

    repository.delete(vehiculo);
}
   

    // Método privado interno para centralizar la validación de datos de entrada
    private void validarDatos(
            Vehiculo vehiculo) {

        // Comprueba si la marca es nula o solo contiene espacios en blanco
        if (vehiculo.getMarca() == null ||
                vehiculo.getMarca().isBlank()) {

            // Lanza excepción indicando campo obligatorio faltante
            throw new IllegalArgumentException(
                    "La marca es obligatoria"
            );
        }

        // Comprueba si el modelo es nulo o está en blanco
        if (vehiculo.getModelo() == null ||
                vehiculo.getModelo().isBlank()) {

            // Lanza excepción indicando campo obligatorio faltante
            throw new IllegalArgumentException(
                    "El modelo es obligatorio"
            );
        }

        // Comprueba si la placa es nula o está en blanco
        if (vehiculo.getPlaca() == null ||
                vehiculo.getPlaca().isBlank()) {

            // Lanza excepción indicando campo obligatorio faltante
            throw new IllegalArgumentException(
                    "La placa es obligatoria"
            );
        }

        // Comprueba si el chasis es nulo o no cumple con la longitud requerida de 17 caracteres
        if (vehiculo.getChasis() == null ||
                vehiculo.getChasis().length() != 17) {

            // Lanza excepción por formato o longitud de chasis inválido
            throw new IllegalArgumentException(
                    "El chasis debe tener exactamente 17 caracteres"
            );
        }

        // Comprueba si el precio es nulo o menor/igual a cero
        if (vehiculo.getPrecio() == null ||
                vehiculo.getPrecio()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            // Lanza excepción exigiendo un precio monetario mayor que cero
            throw new IllegalArgumentException(
                    "El precio debe ser mayor que cero"
            );
        }

        // Calcula el límite de año permitido (año actual del sistema + 1 para vehículos del año posterior)
        int anioMaximo =
                Year.now().getValue() + 1;

        // Valida que el año no sea nulo, sea superior a 1900 y no exceda el límite permitido
        if (vehiculo.getAnio() == null ||
                vehiculo.getAnio() < 1900 ||
                vehiculo.getAnio() > anioMaximo) {

            // Lanza excepción por valor de año fuera del rango permitido
            throw new IllegalArgumentException(
                    "El año del vehículo no es válido"
            );
        }

        // Valida que si se especifica un estado, este corresponda únicamente a DISPONIBLE o VENDIDO
        if (vehiculo.getEstado() != null &&
                !vehiculo.getEstado()
                        .equals("DISPONIBLE") &&
                !vehiculo.getEstado()
                        .equals("VENDIDO")) {

            // Lanza excepción por estado no permitido
            throw new IllegalArgumentException(
                    "El estado debe ser DISPONIBLE o VENDIDO"
            );
        }
    }
}