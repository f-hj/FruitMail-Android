package fr.fruitice.mail.Objects;

import java.io.Serializable;
import java.util.List;

/**
 * Created by florian on 23/12/2016.
 */

public class Mail implements Serializable {
    public String text;
    public String html;
    public String subject;
    public Long date;
    public List<Address> from;
    public List<Address> to;
    public String folder;
    public List<Attachment> attachments;
    public String id;
    public Boolean read;
    public String messageId;
}