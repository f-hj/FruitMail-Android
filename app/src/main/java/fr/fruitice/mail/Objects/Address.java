package fr.fruitice.mail.Objects;

import java.io.Serializable;

/**
 * Created by florian on 23/12/2016.
 */

public class Address implements Serializable {
    public String address;
    public String name;

    public Address() {}

    public Address(String addr) {
        this.address = addr;
    }
}