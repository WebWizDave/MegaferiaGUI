
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
    
    // ... Aquí se agregarán más métodos: registerManager, registerBook, buyStand, etc.
}
