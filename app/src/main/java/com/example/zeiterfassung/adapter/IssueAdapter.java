package com.example.zeiterfassung.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zeiterfassung.R;
import com.example.zeiterfassung.db.CachedIssue;
import com.example.zeiterfassung.models.Issue;

import java.util.List;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueHolder> {

    private final Context _context;
    private List<CachedIssue>_data;

    public IssueAdapter(@NonNull Context context) {
        _context = context;
    }

    @NonNull
    @Override
    public IssueHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(_context);
        View view = inflater.inflate(R.layout.item_issue_data, parent, false);
        return new IssueHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueHolder holder, int position) {
        if(_data == null){
            return;
        }

        if(position >= _data.size()){
            return;
        }

        CachedIssue issue = _data.get(position);
        holder.NumberView.setText(String.format("#%s",issue.id));
        holder.TitleView.setText(issue.title);
        holder.PriorityView.setText(issue.priority);
        holder.StateView.setText(issue.status);
    }

    public void swapData(List<CachedIssue> issues){
        _data = issues;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(_data == null){
            return 0;
        }

        return _data.size();
    }

    static class IssueHolder extends RecyclerView.ViewHolder{
        final TextView NumberView;
        final TextView TitleView;
        final TextView PriorityView;
        final TextView StateView;

        public IssueHolder(@NonNull View itemView) {
            super(itemView);

            NumberView = itemView.findViewById(R.id.NumberValue);
            TitleView = itemView.findViewById(R.id.TitleValue);
            PriorityView = itemView.findViewById(R.id.PriorityValue);
            StateView = itemView.findViewById(R.id.StateValue);
        }
    }
}
