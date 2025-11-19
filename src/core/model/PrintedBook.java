
package core.model;

import core.model.Book;
import core.model.Author;
import core.model.interfaces.Printable;
import java.util.ArrayList;

public class PrintedBook extends Book implements Printable {
    
    private int pages;
    private int copies;

    public PrintedBook(String title, ArrayList<Author> authors, String isbn, String genre, String format, double value, Publisher publisher, int pages, int copies) {
        super(title, authors, isbn, genre, format, value, publisher);
        this.pages = pages;
        this.copies = copies;
    }
    @Override
    public int getPages() {
        return pages;
    }
    @Override
    public int getCopies() {
        return copies;
    }
    
}
