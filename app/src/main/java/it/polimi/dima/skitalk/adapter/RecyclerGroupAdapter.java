package it.polimi.dima.skitalk.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.polimi.dima.model.Group;
import it.polimi.dima.skitalk.R;

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
        public ImageView picture;
        public SwitchCompat swtch;

        public MyViewHolder(View view) {
            super(view);
            groupName = (TextView) view.findViewById(R.id.groupName);
            groupMembers = (TextView) view.findViewById(R.id.groupMembers);
            picture = (ImageView) view.findViewById(R.id.picture);
            swtch = (SwitchCompat) view.findViewById(R.id.groupSwitch);
        }
    }

    public RecyclerGroupAdapter(List<Group> groupList) {
        this.groupList = groupList;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final int pos = position;
        Group c = groupList.get(position);
        holder.groupName.setText(c.getName());
        holder.groupMembers.setText(c.getMembersString());
        holder.picture.setImageBitmap(getResizedBitmap(c.getPicture(), 100));
        holder.swtch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                /*SwitchCompat sw = (SwitchCompat) buttonView;
                LinearLayout ll = (LinearLayout) sw.getParent();
                RecyclerView rec = (RecyclerView) ll.getParent();
                if(isChecked)
                    for(int i=0; i < rec.getChildCount(); i++)
                        if(i != pos) {
                            MyViewHolder itemHolder = (MyViewHolder) rec.findViewHolderForAdapterPosition(i);
                            if(itemHolder.swtch.isChecked()) {
                                itemHolder.swtch.setChecked(false);
                                notifyItemChanged(i);

                            }
                        }*/
            }
        });
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

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}