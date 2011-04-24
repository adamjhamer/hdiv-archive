/*
 * Copyright 2007 Sun Microsystems, Inc.
 * All rights reserved.  You may not modify, use,
 * reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://developer.sun.com/berkeley_license.html
 */


package com.sun.bookstore6.database;

import java.util.*;
import com.sun.bookstore.exception.*;
import com.sun.bookstore.cart.*;
import com.sun.bookstore.cart.ShoppingCart;
import com.sun.bookstore.cart.ShoppingCartItem;
import com.sun.bookstore.database.Book;
import com.sun.bookstore.exception.BookNotFoundException;
import com.sun.bookstore.exception.BooksNotFoundException;
import com.sun.bookstore.exception.OrderException;
import javax.persistence.*;


public class BookDBAO {
    @PersistenceContext
    private EntityManager em;
    List list = new ArrayList();

    public BookDBAO() throws Exception {
        /*
        list.add(new Book("id","Tolkien","J.R.R.","The Lord of the Rings, The Fellowship of the Ring",new Float(8.3),true,1953,"description",10));
        list.add(new Book("id0","Tolkien","J.R.R.","The Lord of the Rings, The Two Towers",new Float(10.1),true,1954,"description",8));
        list.add(new Book("id1","Tolkien","J.R.R.","The Lord of the Rings, The Return of the King",new Float(15.1),true,1955,"description",10));
        list.add(new Book("id2","Aguilera","Gilberto","La otra cara del fuego",new Float(3.2),true,2000,"description",10));
        list.add(new Book("id3","surname","firstName","Title",new Float(3.2),true,2000,"description",10));
        list.add(new Book("id4","surname","firstName","Title",new Float(3.2),true,2000,"description",10));
        list.add(new Book("id5","surname","firstName","Title",new Float(3.2),true,2000,"description",10));
        list.add(new Book("id6","surname","firstName","Title",new Float(3.2),true,2000,"description",10));
        list.add(new Book("id7","surname","firstName","Title",new Float(3.2),true,2000,"description",10));
         *
         */
    }

    public List getBooks() throws BooksNotFoundException {
        
        
        try {
            return em.createQuery("SELECT bd FROM Book bd ORDER BY bd.bookId")
                     .getResultList();
        } catch (Exception ex) {
            throw new BooksNotFoundException(
                    "Could not get books: " + ex.getMessage());
        }
        
    }

    public Book getBook(String bookId) throws BookNotFoundException {
         Book requestedBook = em.find(Book.class, bookId);

        if (requestedBook == null) {
            throw new BookNotFoundException("Couldn't find book: " + bookId);
        }

        return requestedBook;
        
    }

    public void buyBooks(ShoppingCart cart) throws OrderException {
        Collection items = cart.getItems();
        Iterator i = items.iterator();

        try {
            while (i.hasNext()) {
                ShoppingCartItem sci = (ShoppingCartItem) i.next();
                Book bd = (Book) sci.getItem();
                String id = bd.getBookId();
                int quantity = sci.getQuantity();
                buyBook(id, quantity);
            }
        } catch (Exception ex) {
            throw new OrderException("Commit failed: " + ex.getMessage());
        }
        
    }

    public void buyBook(
        String bookId,
        int quantity) throws OrderException {
        try {
            Book requestedBook = null;
            for(Object obj: list) {
                Book book = (Book) obj;
                if(book.getBookId().equals(bookId)) {
                    requestedBook = book;
                    break;
                }
            }
            if (requestedBook != null) {
                int inventory = requestedBook.getInventory();

                if ((inventory - quantity) >= 0) {
                    int newInventory = inventory - quantity;
                    requestedBook.setInventory(newInventory);
                } else {
                    throw new OrderException(
                            "Not enough of " + bookId
                            + " in stock to complete order.");
                }
            }
        } catch (Exception ex) {
            throw new OrderException(
                    "Couldn't purchase book: " + bookId + ex.getMessage());
        }
    }
}
