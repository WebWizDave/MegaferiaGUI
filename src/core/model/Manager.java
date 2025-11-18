/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.model;

/**
 *
 * @author edangulo
 */
public class Manager extends Person {
    
    private Publisher publisher;

    public Manager(long id, String firstname, String lastname) {
        super(id, firstname, lastname);
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }
    
}
