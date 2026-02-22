package com.example.habittracker.adapters;

import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittracker.R;
import com.example.habittracker.models.Habit;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habits;

    public HabitAdapter(List<Habit> habits) {
        this.habits = habits;
    }

    @Override
    public HabitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.title.setText(habit.getTitle());
        holder.description.setText(habit.getDescription());
        holder.frequency.setText(habit.getFrequency());
        holder.checkBox.setChecked(habit.getCompleted());
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {

        TextView title, description, frequency;
        CheckBox checkBox;

        public HabitViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            description = itemView.findViewById(R.id.tvDescription);
            frequency = itemView.findViewById(R.id.tvFrequency);
            checkBox = itemView.findViewById(R.id.cbCompleted);
        }
    }
}