/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.model;

import core.model.Audiobook;
import java.util.ArrayList;

/**
 *
 * @author edangulo
 */
public class Narrator extends Person {
    
    private ArrayList<Audiobook> books;

    public Narrator(long id, String firstname, String lastname) {
        super(id, firstname, lastname);
        this.books = new ArrayList<>();
    }
    
    public int getBookQuantity() {
        return this.books.size();
    }
    
    public void addBook(Audiobook book) {
        this.books.add(book);
    }
    
}
