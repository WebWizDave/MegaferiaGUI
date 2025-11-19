
package core.model;

import core.model.interfaces.Audible;
import java.util.ArrayList;

public class Audiobook extends Book implements Audible {
    
    private int duration;
    private Narrator narrador;

    public Audiobook(String title, ArrayList<Author> authors, String isbn, String genre, String format, double value, Publisher publisher, int duration, Narrator narrator) {
        super(title, authors, isbn, genre, format, value, publisher);
        this.duration = duration;
        this.narrador = narrator;
        
        this.narrador.addBook(this);
    }
    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public Narrator getNarrator() {
        return narrador;
    }   
    
}
