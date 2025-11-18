/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.model;

import core.model.Manager;
import core.model.Book;
import java.util.ArrayList;

/**
 *
 * @author edangulo
 */
public class Publisher {
    
    private final String nit;
    private String name;
    private String address;
    private Manager manager;
    private ArrayList<Book> books;
    private ArrayList<Stand> stands;

    public Publisher(String nit, String name, String address, Manager manager) {
        this.nit = nit;
        this.name = name;
        this.address = address;
        this.manager = manager;
        this.books = new ArrayList<>();
        this.stands = new ArrayList<>();
        
        this.manager.setPublisher(this);
    }

    public String getNit() {
        return nit;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Manager getManager() {
        return manager;
    }
    
    public int getStandQuantity() {
        return this.stands.size();
    }
    
    public void addBook(Book book) {
        this.books.add(book);
    }
    
    public void addStand(Stand stand) {
        this.stands.add(stand);
    }
    
}
