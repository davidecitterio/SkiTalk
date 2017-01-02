package it.polimi.dima.skitalk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import it.polimi.dima.model.Group;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.temp.RecyclerTest;

/**
 * Created by Max on 29/12/2016.
 */

public class RecyclerGroupAdapter extends
        RecyclerView.Adapter<RecyclerGroupAdapter.MyViewHolder> {

    private List<Group> groupList;

    /**
     * View holder class
     * */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView groupName;
        public TextView groupMembers;

        public MyViewHolder(View view) {
            super(view);
            groupName = (TextView) view.findViewById(R.id.groupName);
            groupMembers = (TextView) view.findViewById(R.id.groupMembers);
        }
    }

    public RecyclerGroupAdapter(List<Group> groupList) {
        this.groupList = groupList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Group c = groupList.get(position);
        holder.groupName.setText(c.getName());
        holder.groupMembers.setText(c.getMembersString());
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        return new MyViewHolder(v);
    }
}