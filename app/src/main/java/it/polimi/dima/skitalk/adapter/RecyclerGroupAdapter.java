package it.polimi.dima.skitalk.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import it.polimi.dima.model.Group;
import it.polimi.dima.skitalk.R;
import it.polimi.dima.skitalk.util.Utils;

/**
 * Created by Max on 29/12/2016.
 */

public class RecyclerGroupAdapter extends
        RecyclerView.Adapter<RecyclerGroupAdapter.MyViewHolder> implements Filterable{

    private List<Group> originalGroupList;
    private List<Group> groupList;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                groupList = (List<Group>) results.values;
                RecyclerGroupAdapter.this.notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Group> filteredResults = null;
                if (constraint.length() == 0) {
                    filteredResults = originalGroupList;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
        };
    }

    protected List<Group> getFilteredResults(String constraint) {
        List<Group> results = new ArrayList<>();

        for (Group item : originalGroupList) {
            if (item.getName().toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * View holder class
     * */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView groupName;
        public TextView groupMembers;
        public CircleImageView picture;
        public SwitchCompat swtch;
        public LinearLayout myBackground;

        public MyViewHolder(View view) {
            super(view);
            groupName = (TextView) view.findViewById(R.id.groupName);
            groupMembers = (TextView) view.findViewById(R.id.groupMembers);
            picture = (CircleImageView) view.findViewById(R.id.group_toolbar_picture);
            swtch = (SwitchCompat) view.findViewById(R.id.groupSwitch);
            myBackground = (LinearLayout) view.findViewById(R.id.groupRow);
        }
    }

    public RecyclerGroupAdapter(List<Group> groupList) {

        this.groupList = groupList;
        this.originalGroupList = groupList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final int pos = position;
        Group c = groupList.get(position);
        holder.groupName.setText(c.getName());
        holder.groupMembers.setText(c.getMembersString());
        holder.picture.setImageBitmap(Utils.getResizedBitmap(c.getPicture(), 200));
        holder.swtch.setChecked(c.isActive());
        holder.myBackground.setSelected(false);
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