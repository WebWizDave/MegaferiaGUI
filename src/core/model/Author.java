
package core.model;

import java.util.ArrayList;

/**
 *
 * @author edangulo
 */
public class Author extends Person {
    
    private ArrayList<Book> books;

    public Author(long id, String firstname, String lastname) {
        super(id, firstname, lastname);
        this.books = new ArrayList<>();
    }

    public ArrayList<Book> getBooks() {
        return books;
    }
    
    public int getBookQuantity() {
        return this.books.size();
    }
    
    public void addBook(Book book) {
        this.books.add(book);
    }
    
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
