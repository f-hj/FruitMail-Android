package fr.fruitice.mail.Objects;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by florian on 05/03/2017.
 */

public class Folders {
    @SerializedName("new") public List<Folder> fresh;
    public List<Folder> read;
    public List<Folder> done;
}
