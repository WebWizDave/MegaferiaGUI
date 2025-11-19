
package core.controller;
import core.model.storage.MegaferiaStorage; // Importamos el Singleton de datos
import core.model.*; 
import core.controller.utils.ResponseCodes;
import java.util.ArrayList;
/**
 *
 * @author david
 */
public class MegaferiaController {
    private final MegaferiaStorage storage;

    // 1. Constructor: El controlador se inicializa con acceso al almacenamiento.
    public MegaferiaController() {
        this.storage = MegaferiaStorage.getInstance();
    }
    
    // 2. Método de ejemplo: Registrar un Autor

    public ServiceResponse<Author> registerAuthor(String id, String firstName, String lastName) {
        // --- A. Validación de Entrada (Separación de Responsabilidad de la Vista) ---
        if (id == null || id.isEmpty() || firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Todos los campos de Autor son obligatorios.");
        }

        // --- B. Validación de Lógica de Negocio (Unicidad) ---
        // Revisar si ya existe un autor con ese ID
        boolean exists = storage.getAuthors().stream()
                .anyMatch(a -> a.getId().equals(id));

        if (exists) {
            return new ServiceResponse<>(ResponseCodes.ALREADY_EXISTS, "Ya existe un Autor registrado con el ID: " + id);
        }

        // --- C. Creación y Persistencia (Llama al Modelo) ---
        
        // El constructor de Author debe estar en model/Author.java
        Author newAuthor = new Author(id, firstName, lastName, new ArrayList<>());
        storage.getAuthors().add(newAuthor); // Persistencia simple

        // --- D. Respuesta Exitosa ---
        return new ServiceResponse<>(ResponseCodes.SUCCESS, "Autor " + firstName + " registrado exitosamente.", newAuthor);
    }
    
    public ServiceResponse<Manager> registerManager(String id, String firstName, String lastName) {
        // --- A. Validación de Entrada ---
        if (id == null || id.isEmpty() || firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Todos los campos de Gerente son obligatorios.");
        }

        // --- B. Validación de Lógica de Negocio (Unicidad) ---
        // Debes verificar la unicidad del ID en el storage de Managers
        boolean exists = storage.getManagers().stream()
                .anyMatch(m -> m.getId().equals(Long.parseLong(id))); // Usamos Long.parseLong ya que Manager usa long.

        if (exists) {
            return new ServiceResponse<>(ResponseCodes.ALREADY_EXISTS, "Ya existe un Gerente registrado con el ID: " + id);
        }

        // --- C. Creación y Persistencia ---
        // Convertir ID a long
        long managerId;
        try {
            managerId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El ID debe ser un número válido.");
        }
        
        Manager newManager = new Manager(managerId, firstName, lastName);
        storage.getManagers().add(newManager); 

        // --- D. Respuesta Exitosa ---
        return new ServiceResponse<>(ResponseCodes.SUCCESS, "Gerente " + firstName + " registrado exitosamente.", newManager);
    }
    
    // ... Aquí se agregarán más métodos: registerManager, registerBook, buyStand, etc.
}
