
package core.model.storage;

/**
 *
 * @author david
 */
import java.util.ArrayList;
import core.model.*; // Importa todas las clases del modelo

// Esta clase es un Singleton para centralizar el almacenamiento
public class MegaferiaStorage {
    // 1. Instancia estática (la única que existirá)
    private static MegaferiaStorage instance;

    // 2. Colecciones de datos (movidas desde MegaferiaFrame)
    private final ArrayList<Stand> stands;
    private final ArrayList<Author> authors;
    private final ArrayList<Manager> managers;
    private final ArrayList<Narrator> narrators;
    private final ArrayList<Publisher> publishers;
    private final ArrayList<Book> books;

    // 3. Constructor privado para evitar instanciación externa (Singleton)
    private MegaferiaStorage() {
        this.stands = new ArrayList<>();
        this.authors = new ArrayList<>();
        this.managers = new ArrayList<>();
        this.narrators = new ArrayList<>();
        this.publishers = new ArrayList<>();
        this.books = new ArrayList<>();
    }

    // 4. Método estático público para obtener la única instancia (Punto de acceso)
    public static MegaferiaStorage getInstance() {
        if (instance == null) {
            instance = new MegaferiaStorage();
        }
        return instance;
    }

    // 5. Métodos Getters para cada colección

    public ArrayList<Stand> getStands() {
        return stands;
    }

    public ArrayList<Author> getAuthors() {
        return authors;
    }

    public ArrayList<Manager> getManagers() {
        return managers;
    }

    public ArrayList<Narrator> getNarrators() {
        return narrators;
    }

    public ArrayList<Publisher> getPublishers() {
        return publishers;
    }

    public ArrayList<Book> getBooks() {
        return books;
    }
    
}
