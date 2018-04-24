package fr.fruitice.mail;

import android.database.Observable;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import fr.fruitice.mail.Objects.Mail;

/**
 * Created by florian on 25/12/2016.
 */

public class MailAdapter extends RecyclerView.Adapter<MailAdapter.MyViewHolder> {

    private List<Mail> mailList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView subject, from, text, date, read;

        public MyViewHolder(View view) {
            super(view);
            subject = (TextView) view.findViewById(R.id.subject);
            from = (TextView) view.findViewById(R.id.from);
            date = (TextView) view.findViewById(R.id.date);
        }
    }

    public MailAdapter(List<Mail> l) {
        this.mailList = l;
    }

    @Override
    public MailAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mail_list_row, parent, false);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MailAdapter.MyViewHolder holder, int position) {
        final Mail mail = mailList.get(position);
        holder.subject.setText(mail.subject);
        holder.from.setText(mail.from.get(0).name);
        holder.date.setText(android.text.format.DateUtils.getRelativeTimeSpanString(mail.date));
    }

    @Override
    public int getItemCount() {
        if (mailList == null) {
            return 0;
        } else {
            return mailList.size();
        }
    }
}
