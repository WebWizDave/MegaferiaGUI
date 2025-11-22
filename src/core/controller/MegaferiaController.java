
package core.controller;// Importamos el Singleton de datos
import core.controller.utils.Observable;
import core.controller.utils.Observer;
import core.model.*; 
import core.controller.utils.ResponseCodes;
import core.model.storage.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MegaferiaController implements Observable {
    private final StorageService storage;
    private final List<Observer> observers = new ArrayList<>();
    // 1. Inicializa el controlador utilizando la instancia centralizada de almacenamiento.
    
    public MegaferiaController() {
        this.storage = MegaferiaStorage.getInstance();
    }
    
    /**2.
     * Registra un nuevo autor validando campos obligatorios,
     * formato del ID y unicidad dentro del sistema.
     */

    public ServiceResponse<Author> registerAuthor(String id, String firstName, String lastName) {
        // --- A. Validación de Entrada (Separación de Responsabilidad de la Vista) ---
        if (id == null || id.isEmpty() || firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Todos los campos de Autor son obligatorios.");
        }
        // Validar y parsear el ID de String a long (asumiendo que Author.id es long)
        long authorId;
        try {
            authorId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El ID del Autor debe ser un número válido.");
        }

        // --- B. Validación de Lógica de Negocio (Unicidad) ---
        boolean exists = storage.getAuthors().stream()
            // CORRECCIÓN 1: Comparamos el long (authorId) con el long de la clase (a.getId())
            .anyMatch(a -> a.getId() == authorId); 

        if (exists) {
            return new ServiceResponse<>(ResponseCodes.ALREADY_EXISTS, "Ya existe un Autor registrado con el ID: " + id);
        }

        // --- C. Creación y Persistencia (Llama al Modelo) ---

        // CORRECCIÓN 2: El constructor ahora solo toma los parámetros directos, sin la lista vacía.
        // Asumiendo la firma: new Author(long id, String firstName, String lastName)
        Author newAuthor = new Author(authorId, firstName, lastName); 

        storage.getAuthors().add(newAuthor); // Persistencia simple

        // --- D. Respuesta Exitosa ---
        // NOTA: Recuerda llamar a notifyObservers() aquí si el registro es exitoso.
        notifyObservers();
        return new ServiceResponse<>(ResponseCodes.SUCCESS, "Autor " + firstName + " registrado exitosamente.", newAuthor);
    }
        // ---------------------------------------------------------------------
    // REGISTRO DE GERENTES
    // ---------------------------------------------------------------------

    /**
     * Registra un gerente validando unicidad y formato de ID.
     */
    public ServiceResponse<Manager> registerManager(String id, String firstName, String lastName) {
        // --- A. Validación de Entrada ---
        if (id == null || id.isEmpty() || firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Todos los campos de Gerente son obligatorios.");
        }

        // Validar y parsear el ID de String a long
        long managerId;
        try {
            managerId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El ID del Gerente debe ser un número válido.");
        }

        // --- B. Validación de Lógica de Negocio (Unicidad) ---
        boolean exists = storage.getManagers().stream()
            // CORRECCIÓN 3: Cambiamos .equals() por == (asumiendo que m.getId() retorna un long primitivo)
            .anyMatch(m -> m.getId() == managerId); 

        if (exists) {
            return new ServiceResponse<>(ResponseCodes.ALREADY_EXISTS, "Ya existe un Gerente registrado con el ID: " + id);
        }

        // --- C. Creación y Persistencia ---
        // El constructor de Manager debe recibir el ID como long
        Manager newManager = new Manager(managerId, firstName, lastName); // Asumiendo el constructor corregido
        storage.getManagers().add(newManager); 

        // --- D. Respuesta Exitosa ---
        notifyObservers();
        return new ServiceResponse<>(ResponseCodes.SUCCESS, "Gerente " + firstName + " registrado exitosamente.", newManager);
    }
    
         // ---------------------------------------------------------------------
    // REGISTRO DE NARRADORES
    // ---------------------------------------------------------------------

    /**
     * Registra un narrador mediante validación de campos básicos
     * y verificación de unicidad.
     */  
    public ServiceResponse<Narrator> registerNarrator(String id, String firstName, String lastName) {
        // --- A. Validación de Entrada ---
        if (id == null || id.isEmpty() || firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Todos los campos de Narrador son obligatorios.");
        }

        // --- B. Validación de Lógica de Negocio (Unicidad) ---
        // Chequear si el ID ya existe en el storage de Narrators
        boolean exists = storage.getNarrators().stream()
                .anyMatch(n -> Long.toString(n.getId()).equals(id));

        if (exists) {
            return new ServiceResponse<>(ResponseCodes.ALREADY_EXISTS, "Ya existe un Narrador registrado con el ID: " + id);
        }

        // --- C. Creación y Persistencia ---
        long narratorId;
        try {
            narratorId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El ID debe ser un número válido.");
        }

        Narrator newNarrator = new Narrator(narratorId, firstName, lastName);
        storage.getNarrators().add(newNarrator); 

        // --- D. Respuesta Exitosa ---
        return new ServiceResponse<>(ResponseCodes.SUCCESS, "Narrador " + firstName + " registrado exitosamente.", newNarrator);
        }
    
     // ---------------------------------------------------------------------
    // REGISTRO DE STANDS
    // ---------------------------------------------------------------------

    /**
     * Registra un nuevo stand verificando campos obligatorios, formato numérico
     * y unicidad del identificador.
     */
    public ServiceResponse<Stand> registerStand(String id, String price) {
        // --- A. Validación de Entrada ---
        if (id == null || id.isEmpty() || price == null || price.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El ID y el Precio del Stand son obligatorios.");
        }

        // --- B. Validación de Lógica de Negocio (Unicidad) ---
        // Chequear si el ID ya existe en el storage de Stands
        boolean exists = storage.getStands().stream()
                .anyMatch(s -> Long.toString(s.getId()).equals(id));

        if (exists) {
            return new ServiceResponse<>(ResponseCodes.ALREADY_EXISTS, "Ya existe un Stand registrado con el ID: " + id);
        }

        // --- C. Creación y Persistencia ---
        long standId;
        double standPrice;
        try {
            standId = Long.parseLong(id);
            standPrice = Double.parseDouble(price);
        } catch (NumberFormatException e) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El ID y el Precio deben ser números válidos.");
        }

        Stand newStand = new Stand(standId, standPrice);
        storage.getStands().add(newStand);

        // --- D. Respuesta Exitosa ---
        return new ServiceResponse<>(ResponseCodes.SUCCESS, "Stand N° " + standId + " registrado exitosamente.", newStand);
        }
    
     // ---------------------------------------------------------------------
    // REGISTRO DE EDITORIALES
    // ---------------------------------------------------------------------

    /**
     * Registra una editorial asociándola a un gerente existente.
     * Verifica unicidad del NIT y consistencia de la relación.
     */
    public ServiceResponse<Publisher> registerPublisher(String nit, String name, String address, String managerIdString) {
        // --- A. Validación de Entrada ---
        if (nit.isEmpty() || name.isEmpty() || address.isEmpty() || managerIdString == null || managerIdString.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Todos los campos de Editorial y la asignación del Gerente son obligatorios.");
        }

        // --- B. Búsqueda y Validación del Gerente ---
        Manager manager = null;
        try {
            long managerId = Long.parseLong(managerIdString);
            // El Manager debe existir en la lista de Managers del Storage
            manager = storage.getManagers().stream()
                    .filter(m -> m.getId() == managerId)
                    .findFirst()
                    .orElse(null);
        } catch (NumberFormatException e) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El ID del Gerente no es válido.");
        }

        if (manager == null) {
            return new ServiceResponse<>(ResponseCodes.NOT_FOUND, "El Gerente con ID " + managerIdString + " no fue encontrado. Regístrelo primero.");
        }

        // --- C. Validación de Lógica de Negocio (Unicidad del NIT) ---
        boolean nitExists = storage.getPublishers().stream()
                .anyMatch(p -> p.getNit().equals(nit));

        if (nitExists) {
            return new ServiceResponse<>(ResponseCodes.ALREADY_EXISTS, "Ya existe una Editorial registrada con el NIT: " + nit);
        }

        // --- D. Creación y Persistencia ---
        Publisher newPublisher = new Publisher(nit, name, address, manager);
        storage.getPublishers().add(newPublisher);

        // --- E. Respuesta Exitosa ---
        return new ServiceResponse<>(ResponseCodes.SUCCESS, "Editorial " + name + " registrada exitosamente.", newPublisher);
        }
        // ---------------------------------------------------------------------
    // REGISTRO DE LIBROS
    // ---------------------------------------------------------------------

    /**
     * Registra un libro aplicando validaciones comunes
     * y construcción polimórfica según el tipo especificado.
     */
    public ServiceResponse<Book> registerBook(
        String title, String authorIdsString, String isbn, String genre, 
        String valueString, String publisherNit, String bookType, 
        String pagesString, String copiesString, String hyperlink, 
        String durationString, String narratorIdString) 
        {
        // --- A. Validaciones Iniciales Comunes ---
        if (title.isEmpty() || authorIdsString.isEmpty() || isbn.isEmpty() || genre.isEmpty() || valueString.isEmpty() || publisherNit.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Los campos principales (Título, Autores, ISBN, Género, Valor, Editorial) son obligatorios.");
        }

        // --- B. Conversión y Búsqueda de Objetos ---

        // 1. Valor (Value)
        double value;
        try {
            value = Double.parseDouble(valueString);
        } catch (NumberFormatException e) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El Valor del libro debe ser un número válido.");
        }

        // 2. Editorial (Publisher)
        Publisher publisher = storage.getPublishers().stream()
            .filter(p -> p.getNit().equals(publisherNit))
            .findFirst().orElse(null);

        if (publisher == null) {
            return new ServiceResponse<>(ResponseCodes.NOT_FOUND, "Editorial no encontrada. Por favor, regístrela primero.");
        }

        // 3. Autores (Authors)
        ArrayList<Author> authors = new ArrayList<>();
        String[] authorIds = authorIdsString.split("\\n"); // Separa IDs por nueva línea
        if (authorIds.length == 0) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Debe agregar al menos un autor.");
        }

        for (String id : authorIds) {
            Author author = storage.getAuthors().stream()
                .filter(a -> Long.toString(a.getId()).equals(id.trim()))
                .findFirst().orElse(null);

            if (author == null) {
                return new ServiceResponse<>(ResponseCodes.NOT_FOUND, "Autor con ID: " + id.trim() + " no encontrado.");
            }
            authors.add(author);
        }

        // 4. Validación de Unicidad de ISBN
        boolean isbnExists = storage.getBooks().stream()
            .anyMatch(b -> b.getIsbn().equals(isbn));
        if (isbnExists) {
            return new ServiceResponse<>(ResponseCodes.ALREADY_EXISTS, "Ya existe un libro registrado con el ISBN: " + isbn);
        }

        // --- C. Creación Polimórfica (Switch por Tipo de Libro) ---
        Book newBook = null;

        switch (bookType) {
            case "IMPRESO":
                int pages, copies;
                try {
                    pages = Integer.parseInt(pagesString);
                    copies = Integer.parseInt(copiesString);
                } catch (NumberFormatException e) {
                    return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Páginas y Copias deben ser números enteros.");
                }
                newBook = new PrintedBook(title, authors, isbn, genre, bookType, value, publisher, pages, copies);
                break;

            case "DIGITAL":
                if (hyperlink.isEmpty()) {
                    return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El Hipervínculo es obligatorio para Libros Digitales.");
                }
                newBook = new DigitalBook(title, authors, isbn, genre, bookType, value, publisher, hyperlink);
                break;

            case "AUDIO":
                int duration;
                try {
                    duration = Integer.parseInt(durationString);
                } catch (NumberFormatException e) {
                    return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "La Duración debe ser un número entero (minutos).");
                }

                // Buscar Narrador
                Narrator narrator = null;
                try {
                    long narratorId = Long.parseLong(narratorIdString);
                    narrator = storage.getNarrators().stream()
                        .filter(n -> n.getId() == narratorId)
                        .findFirst().orElse(null);
                } catch (NumberFormatException e) {
                    return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "El ID del Narrador no es válido.");
                }

                if (narrator == null) {
                    return new ServiceResponse<>(ResponseCodes.NOT_FOUND, "Narrador no encontrado. Por favor, regístrelo primero.");
                }

                newBook = new Audiobook(title, authors, isbn, genre, bookType, value, publisher, duration, narrator);
                break;

            default:
                return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Tipo de libro no válido.");
        }

        // --- D. Persistencia y Respuesta Final ---
        storage.getBooks().add(newBook);
        return new ServiceResponse<>(ResponseCodes.SUCCESS, "Libro '" + title + "' registrado exitosamente como " + bookType + ".", newBook);
        }
    
    
    // Búsqueda de libros por autor
    public ServiceResponse<List<Book>> searchBooksByAuthor(String authorIdString) {
        if (authorIdString == null || authorIdString.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Debe seleccionar un autor.");
        }

        long authorId;
        try {
            authorId = Long.parseLong(authorIdString);
        } catch (NumberFormatException e) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "ID de autor no válido.");
        }

        // Filtra los libros cuya lista de autores contenga al autor con el ID dado
        List<Book> result = storage.getBooks().stream()
            .filter(book -> book.getAuthors().stream()
                .anyMatch(author -> author.getId() == authorId))
            .collect(Collectors.toList());

        if (result.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.NOT_FOUND, "No se encontraron libros para el autor seleccionado.");
        }

        return new ServiceResponse<>(ResponseCodes.SUCCESS, result.size() + " libros encontrados.", result);
    }

// Búsqueda de libros por formato
    public ServiceResponse<List<Book>> searchBooksByFormat(String format) {
        if (format == null || format.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.INVALID_ARGUMENT, "Debe seleccionar un formato (IMPRESO, DIGITAL, AUDIO).");
        }

        List<Book> result = storage.getBooks().stream()
            .filter(book -> book.getFormat().equalsIgnoreCase(format))
            .collect(Collectors.toList());

        if (result.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.NOT_FOUND, "No se encontraron libros en el formato: " + format);
        }

        return new ServiceResponse<>(ResponseCodes.SUCCESS, result.size() + " libros encontrados.", result);
    }

    public ServiceResponse<Publisher> assignStandsToPublisher(String publisherNit, List<Long> standIds) {
        // --- A. Validación y Búsqueda de Editorial ---
        Publisher publisher = storage.getPublishers().stream().filter(p -> p.getNit().equals(publisherNit))
        .findFirst().orElse(null); // Retorna null si no la encuentra
                

        // --- B. Validación y Búsqueda de Stands (¡SIMPLIFICADA!) ---

        // Ya no chequeamos si el stand está 'alreadyAssigned', porque la regla lo permite.
        // Solo chequeamos si existe.
        List<Long> notFound = new ArrayList<>();
        List<Stand> standsToAssign = new ArrayList<>();

        for (Long standId : standIds) {
            Stand stand = storage.getStands().stream()
                    .filter(s -> s.getId() == standId)
                    .findFirst()
                    .orElse(null);

            if (stand == null) {
                notFound.add(standId);
            } else {
                standsToAssign.add(stand);
            }
        }

        // --- C. Manejo de Errores Múltiples ---
        if (!notFound.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.NOT_FOUND, "Los siguientes Stands no fueron encontrados: " + notFound.toString());
        }

        // --- D. Lógica de Asignación y Persistencia (¡EL CAMBIO CLAVE!) ---
        for (Stand stand : standsToAssign) {
            // Vinculación bidireccional:

            // 1. Asigna la Editorial al Stand (usando el método addPublisher)
            stand.addPublisher(publisher); 

            // 2. Asigna el Stand a la Editorial 
            publisher.addStand(stand);
        }

        double totalCost = standsToAssign.stream().mapToDouble(Stand::getPrice).sum();

        // --- E. Respuesta Final ---
        return new ServiceResponse<>(
            ResponseCodes.SUCCESS, 
            standsToAssign.size() + " Stands vinculados a la editorial " + publisher.getName() + " por un costo total de " + totalCost + ". Ahora el stand tiene " + standsToAssign.get(0).getPublisherQuantity() + " editoriales asignadas.",
            publisher
        );
    }   

    public ServiceResponse<List<Author>> searchAuthorsByPublisherDiversity() {
        // Mapea cada autor a la cantidad de editoriales diferentes en las que tiene libros
        Map<Author, Long> authorDiversityMap = storage.getBooks().stream()
            .flatMap(book -> book.getAuthors().stream().map(author -> new AbstractMap.SimpleEntry<>(author, book.getPublisher())))
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.collectingAndThen(
                    Collectors.toSet(), // Obtiene un Set de Publishers (elimina duplicados)
                    Set::size // Cuenta el tamaño del Set
                ))
            )).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().longValue())); // Vuelve a mapear a Long

        if (authorDiversityMap.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.NOT_FOUND, "No hay autores o libros registrados.");
        }

        // Encuentra el máximo de diversidad (máximo número de editoriales)
        long maxDiversity = authorDiversityMap.values().stream()
            .max(Long::compare)
            .orElse(0L);

        // Filtra y devuelve la lista de autores que cumplen con la máxima diversidad
        List<Author> topAuthors = authorDiversityMap.entrySet().stream()
            .filter(entry -> entry.getValue() == maxDiversity)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (topAuthors.isEmpty()) {
            return new ServiceResponse<>(ResponseCodes.NOT_FOUND, "No se encontraron autores con alta diversidad de editoriales.");
        }

        return new ServiceResponse<>(ResponseCodes.SUCCESS, 
            topAuthors.size() + " autores empatan con el máximo de " + maxDiversity + " editoriales diferentes.", 
            topAuthors
        );
        }   
    
    //getters 
    public List<Author> getAuthors() {
    return storage.getAuthors();
    }

    public List<Manager> getManagers() {
        return storage.getManagers();
    }

    public List<Narrator> getNarrators() {
        return storage.getNarrators();
    }

    public List<Publisher> getPublishers() {
        return storage.getPublishers();
    }

    public List<Stand> getStands() {
        return storage.getStands();
    }

    public List<Book> getBooks() {
        return storage.getBooks();
    }
    // getter para la consulta compleja
    public ServiceResponse<List<Author>> getTopAuthorsByPublisherDiversity() {
        return searchAuthorsByPublisherDiversity(); // Llama a la consulta compleja
    }
    // Metodos de la interfaz Observable
    @Override
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : this.observers) {
            observer.update(); // Llama al método update() sin argumentos
        }
    }

    
}
