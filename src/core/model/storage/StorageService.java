
package core.model.storage;

import core.model.*;
import java.util.ArrayList;

public interface StorageService {
    
    ArrayList<Stand> getStands();
    ArrayList<Author> getAuthors();
    ArrayList<Manager> getManagers();
    ArrayList<Narrator> getNarrators();
    ArrayList<Publisher> getPublishers();
    ArrayList<Book> getBooks();
}
