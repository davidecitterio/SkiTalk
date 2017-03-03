package it.polimi.dima.skitalk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import it.polimi.dima.model.User;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;

import static it.polimi.dima.skitalk.activity.CreateGroup_step2.switchUser;

/**
 * Created by Davide on 02/01/2017.
 */

public class RecyclerAddUserAdapter extends
        RecyclerView.Adapter<RecyclerAddUserAdapter.MyViewHolder> {

    private List<User> userList;


    /**
     * View holder class
     * */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, nickname;
        public CircleImageView picture;
        public Button addUser;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            nickname = (TextView) view.findViewById(R.id.nickname);
            picture = (CircleImageView) view.findViewById(R.id.group_toolbar_picture);
            addUser = (Button) view.findViewById(R.id.add_user);
        }
    }

    public RecyclerAddUserAdapter(List<User> groupList) {

        this.userList = groupList;

    }

    @Override
    public void onBindViewHolder(final RecyclerAddUserAdapter.MyViewHolder holder, int position) {
        User c = userList.get(position);
        holder.name.setText(c.getName()+" "+c.getSurname());
        holder.nickname.setText(c.getNickname());
        holder.picture.setImageBitmap(Utils.getResizedBitmap(c.getPicture(), 100));
        holder.addUser.setTag(c.getId());

        System.out.println("id è: "+ holder.addUser.getTag());

        holder.addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchUser((int)holder.addUser.getTag());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public RecyclerAddUserAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_row, parent, false);
        return new RecyclerAddUserAdapter.MyViewHolder(v);
    }
}
