package com.humang.script_launcher.excute_script;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.humang.script_launcher.R;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class SubItemAdapter extends RecyclerView.Adapter<SubItemAdapter.RecyViewHolder> {
    private static final String TAG = "SubSettingAdapter";

    private RecyclerView mRecycleView;
    private Context mContext;
    private List<String> cmds;
    private Queue<Integer> checkedQueue = new PriorityQueue<>((o1, o2)->(o2-o1));
    private boolean isEdit;

    public SubItemAdapter(Context mContext, List<String> cmds) {
        this.mContext = mContext;
        this.cmds = cmds;
    }

    @Override
    public RecyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.script_item, parent, false);
        this.mRecycleView = (RecyclerView) parent;
        return new RecyViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyViewHolder holder, int position) {
        holder.setPosition(position);
        holder.order.setText(String.valueOf(position));
        holder.cmd.setText(cmds.get(position));
        holder.delete.setChecked(checkedQueue.contains(position));
        if (isEdit) {
            holder.delete.setVisibility(View.VISIBLE);
        } else {
            holder.delete.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return cmds != null ? cmds.size() : 0;
    }

    class RecyViewHolder extends RecyclerView.ViewHolder{
        TextView order;
        TextView cmd;
        CheckBox delete;
        int position;
        public RecyViewHolder(View item) {
            super(item);
            order = item.findViewById(R.id.order);
            cmd = item.findViewById(R.id.cmd);
            delete = item.findViewById(R.id.delete);
            delete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        checkedQueue.add(position);
                    } else {
                        checkedQueue.remove(position);
                    }
                }
            });
        }

        public void setPosition(int position) {
            this.position = position;
        }

    }

    public Queue<Integer> getCheckedQueue() {
        return checkedQueue;
    }

    public void showCheckBox() {
        isEdit = true;
        notifyDataSetChanged();
    };
    public void hideCheckBox() {
        isEdit = false;
        notifyDataSetChanged();
    };
}
