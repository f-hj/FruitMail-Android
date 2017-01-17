package fr.fruitice.mail.Objects;

/**
 * Created by florian on 26/12/2016.
 */

public class MailWrited {
    public String subject;
    public String markdown;
    public Address to;
    public Address from;
    public String references;
    public String inReplyTo;

    public MailWrited() {
        this.to = new Address();
        this.from = new Address();
    }

    public MailWrited(String references) {
        this();
        this.references = references;
        this.inReplyTo = references;
    }
}
