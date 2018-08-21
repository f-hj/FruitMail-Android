package fr.fruitice.mail.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by florian on 26/12/2016.
 */

public class MailWrited {
    public String subject;
    public String markdown;
    public List<Address> to;
    public Address from;
    public String references;
    public String inReplyTo;

    public MailWrited() {
        this.to = new ArrayList<Address>();
        this.from = new Address();
    }

    public MailWrited(String references) {
        this();
        this.references = references;
        this.inReplyTo = references;
    }
}
