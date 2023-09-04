package com.example.zeiterfassung.adapter;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zeiterfassung.EditDataActivity;
import com.example.zeiterfassung.ListDataActivity;
import com.example.zeiterfassung.ModernEditActivity;
import com.example.zeiterfassung.R;
import com.example.zeiterfassung.TimeTrackingApp;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import com.example.zeiterfassung.db.WorkTime;

public class WorkTimeDataAdapter extends
        RecyclerView.Adapter<WorkTimeDataAdapter.WorkTimeViewHolder> {
    private ListDataActivity _context;
    private List<WorkTime> _data;
    private DateFormat _dateFormatter;
    private DateFormat _timeFormatter;



    public WorkTimeDataAdapter(ListDataActivity context){
        _context = context;
        //Initializierung Datum /Uhrzeit Formatierung
        _dateFormatter = android.text.format.DateFormat.getDateFormat(_context);
        _timeFormatter = android.text.format.DateFormat.getTimeFormat(_context);
    }
    //Diese Methode wird aufgerufen wenn ein neues ViewHolder_Objekt benötigt wird
    @NonNull
    @Override
    public WorkTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(_context);
        View view = inflater.inflate(R.layout.item_time_data, parent, false);
        return new WorkTimeViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull WorkTimeViewHolder holder, int position) {
        //keine Daten vorhanden
        if (_data == null){
            return;
        }
        //Keine Daten für die angegebene Position
        if (position >= _data.size()){
            return;
        }
        WorkTime currentData = _data.get(position);

        holder.StartTimeView.setText(formatDateTime(currentData.startTime));
        if (currentData.endTime == null){
            holder.EndTimeView.setText("---");
        }else{
            holder.EndTimeView.setText(formatDateTime(currentData.endTime));
        }
        holder.MoreIcon.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(_context, holder.MoreIcon);
            //Layout des Menü
            menu.inflate(R.menu.ctx_menu_data_list);
            //Listener setzen
            menu.setOnMenuItemClickListener(item -> handleContextMenu(item,currentData,position));
            //Anzeige
            menu.show();
        });
        //Bearbeiten auf Klick
        holder.itemView.setOnClickListener(v -> editWorkTime(currentData));

    }

    private boolean handleContextMenu(MenuItem menuItem, WorkTime data, int position) {
        switch (menuItem.getItemId()){
            case R.id.MenuItemListData:
                deleteWorkTime(data, position);
                break;
            case R.id.MenuItemBearbeiten:
                editWorkTime(data);
                break;
        }
        return true;
    }
    private void editWorkTime(WorkTime data){
        Intent editIntent = new Intent(_context, ModernEditActivity.class);
        editIntent.putExtra(EditDataActivity.ID_KEY, data.id);
        _context.startActivity(editIntent);


    }

    private boolean deleteWorkTime(WorkTime workTime,int position){
        //Dialog Konfigurien
        AlertDialog confirmDialog = new AlertDialog.Builder(_context)
                .setTitle(R.string.DialogTitleDelete)
                .setMessage(R.string.DialogMessageDelete)
                .setNegativeButton(R.string.ButtonCancel, null)
                .setPositiveButton(R.string.MenuItemDelete, (dialog, which) -> {
                    //Datensatz löchen
                    TimeTrackingApp app = (TimeTrackingApp) _context.getApplicationContext();
                    app.getExecutors().diskIO().execute(()->{
                        //Löschen in Datenbank
                        app.getDb().workTimeDato().delete(workTime);
                        app.getExecutors().mainThread().execute(()->{
                            //Löschen der internen Liste
                            _data.remove(workTime);
                            //Adapter über Löschung informieren
                            notifyItemRemoved(position);
                            //Dialog schließen
                            dialog.dismiss();

                        });

                    });

                }).create();
          confirmDialog.show();
        //Datensatz nach Bestätigung löschen
        return true;
    }
    private String formatDateTime(Calendar currentTime) {
        if (currentTime == null) {
            return "";
        }
        return String.format(
                "%s %s",//String für Formatierung
                _dateFormatter.format(currentTime.getTime()), //Datum formatierung
                _timeFormatter.format(currentTime.getTime()) // Zeit formatierung
        );

    }


    @Override
    public int getItemCount() {
        if (_data == null){
            return 0;
        }
        return _data.size();

    }
    // Dieses Methode empfängt eine neue Liste und benachrichtigt den Adapter darüber, dass es änderung gibts
    public void swapData(List<WorkTime>data){
        _data = data;
        notifyDataSetChanged();
    }

    static class WorkTimeViewHolder extends RecyclerView.ViewHolder{

        final TextView StartTimeView;
        final TextView EndTimeView;
        final TextView MoreIcon;


        public WorkTimeViewHolder(@NonNull View itemView){
            super(itemView);

            StartTimeView = itemView.findViewById(R.id.StartTimeValue);
            EndTimeView = itemView.findViewById(R.id.EndTimeValue);
            MoreIcon = itemView.findViewById(R.id.ShowMoreCommand);
        }



    }


}
