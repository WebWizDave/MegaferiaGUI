
package core.model;

import java.util.ArrayList;

/**
 *
 * @author edangulo
 */
public class Author extends Person {
    
    private ArrayList<Book> books;
    /**
     * Crea un autor con identificador y nombres respectivos,
     * inicializando la colección de libros asociados.
     */
    public Author(long id, String firstname, String lastname) {
        super(id, firstname, lastname);
        this.books = new ArrayList<>();
    }

    public ArrayList<Book> getBooks() {
        return books;
    }
    // * Retorna la cantidad de libros que el autor tiene registrados.
    public int getBookQuantity() {
        return this.books.size();
    }
    
    public void addBook(Book book) {
        this.books.add(book);
    }
        /**
     * Retorna la cantidad de editoriales distintas para las que ha trabajado el autor.
     */
    public int getPublisherQuantity() {
        ArrayList<Publisher> publishers = new ArrayList<>();
        for (Book book : this.books) {
            if (!publishers.contains(book.getPublisher())) {
                publishers.add(book.getPublisher());
            }
        }
        return publishers.size();
    }
    
}
