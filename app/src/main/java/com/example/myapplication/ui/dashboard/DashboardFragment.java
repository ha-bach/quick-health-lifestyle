package com.example.myapplication.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentDashboardBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.sql.Array;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends Fragment {

private FragmentDashboardBinding binding;

    FirebaseAuth auth;
    TextView textViewInsightsTitle;
    FirebaseUser user;
    String userID;
    FirebaseFirestore firestore;
    LineChart sleepLineChart, hydrationLineChart, exerciseLineChart;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textViewInsightsTitle = binding.insightsWelcome;

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = user.getUid();
        firestore = FirebaseFirestore.getInstance();

        sleepLineChart = binding.insightsSleepLinechart;
        sleepLineChart.setTouchEnabled(true);
        sleepLineChart.setDragEnabled(true);
        sleepLineChart.setScaleEnabled(false);
        sleepLineChart.setPinchZoom(false);
        sleepLineChart.setDrawGridBackground(false);
        sleepLineChart.setExtraLeftOffset(15);
        sleepLineChart.setExtraRightOffset(15);

        hydrationLineChart = binding.insightsHydrationLinechart;
        hydrationLineChart.setTouchEnabled(true);
        hydrationLineChart.setDragEnabled(true);
        hydrationLineChart.setScaleEnabled(false);
        hydrationLineChart.setPinchZoom(false);
        hydrationLineChart.setDrawGridBackground(false);
        hydrationLineChart.setExtraLeftOffset(15);
        hydrationLineChart.setExtraRightOffset(15);

        exerciseLineChart = binding.insightsExerciseLinechart;
        exerciseLineChart.setTouchEnabled(true);
        exerciseLineChart.setDragEnabled(true);
        exerciseLineChart.setScaleEnabled(false);
        exerciseLineChart.setPinchZoom(false);
        exerciseLineChart.setDrawGridBackground(false);
        exerciseLineChart.setExtraLeftOffset(15);
        exerciseLineChart.setExtraRightOffset(15);

        XAxis sleepXAxis = sleepLineChart.getXAxis();
        sleepXAxis.setGranularity(1f);
        sleepXAxis.setCenterAxisLabels(true);
        sleepXAxis.setEnabled(true);
        sleepXAxis.setDrawGridLines(false);
        sleepXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        XAxis hydrationXAxis = hydrationLineChart.getXAxis();
        hydrationXAxis.setGranularity(1f);
        hydrationXAxis.setCenterAxisLabels(true);
        hydrationXAxis.setEnabled(true);
        hydrationXAxis.setDrawGridLines(false);
        hydrationXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        XAxis exerciseXAxis = exerciseLineChart.getXAxis();
        exerciseXAxis.setGranularity(1f);
        exerciseXAxis.setCenterAxisLabels(true);
        exerciseXAxis.setEnabled(true);
        exerciseXAxis.setDrawGridLines(false);
        exerciseXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);



        DocumentReference documentReference = firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (getActivity() != null && documentSnapshot != null) {
                    textViewInsightsTitle.setText(getString(R.string.insights_intro, documentSnapshot.getString("firstName")));

                    DateFormat dateFormat = new SimpleDateFormat("MM-dd");
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -6);
                    ArrayList<String> xAxisValues = new ArrayList<>();
                    for(int i = 0; i < 7; i++) {
                        xAxisValues.add(dateFormat.format(cal.getTime()));
                        cal.add(Calendar.DATE, +1);
                    }


                    ArrayList<ILineDataSet> sleepDataSets = new ArrayList<>();
                    List<Long> sleepHistory = (List<Long>)documentSnapshot.get("sleepHistory");
                    ArrayList<Entry> sleepEntries = new ArrayList<>();
                    for(int i = 1; i < 8; i++) {
                        sleepEntries.add(new Entry(i, sleepHistory.get(7 - i)));
                    }
                    List<Entry> sleepEntriesList = sleepEntries.subList(0,7);
                    sleepDataSets = new ArrayList<>();

                    LineDataSet sleepSet1;
                    sleepSet1 = new LineDataSet(sleepEntriesList, "Hours Slept");
                    sleepSet1.setColor(Color.rgb(65, 168, 121));
                    sleepSet1.setValueTextColor(Color.rgb(55, 70, 73));
                    sleepSet1.setValueTextSize(10f);
                    sleepSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    sleepDataSets.add(sleepSet1);

                    sleepLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));

                    LineData sleepData = new LineData(sleepDataSets);
                    sleepLineChart.setData(sleepData);
                    sleepLineChart.animateX(1000);
                    sleepLineChart.invalidate();
                    sleepLineChart.getLegend().setEnabled(false);
                    sleepLineChart.getDescription().setEnabled(false);


                    ArrayList<ILineDataSet> hydrationDataSets = new ArrayList<>();
                    List<Long> hydrationHistory = (List<Long>)documentSnapshot.get("hydrationHistory");
                    ArrayList<Entry> hydrationEntries = new ArrayList<>();
                    for(int i = 1; i < 8; i++) {
                        hydrationEntries.add(new Entry(i, hydrationHistory.get(7 - i)));
                    }
                    List<Entry> hydrationEntriesList = hydrationEntries.subList(0,7);
                    hydrationDataSets = new ArrayList<>();

                    LineDataSet hydrationSet1;
                    hydrationSet1 = new LineDataSet(hydrationEntriesList, "Cups Drank");
                    hydrationSet1.setColor(Color.rgb(65, 168, 121));
                    hydrationSet1.setValueTextColor(Color.rgb(55, 70, 73));
                    hydrationSet1.setValueTextSize(10f);
                    hydrationSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    hydrationDataSets.add(hydrationSet1);

                    hydrationLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));

                    LineData hydrationData = new LineData(hydrationDataSets);
                    hydrationLineChart.setData(hydrationData);
                    hydrationLineChart.animateX(1000);
                    hydrationLineChart.invalidate();
                    hydrationLineChart.getLegend().setEnabled(false);
                    hydrationLineChart.getDescription().setEnabled(false);


                    ArrayList<ILineDataSet> exerciseDataSets = new ArrayList<>();
                    List<Long> exerciseHistory = (List<Long>)documentSnapshot.get("exerciseHistory");
                    ArrayList<Entry> exerciseEntries = new ArrayList<>();
                    for(int i = 1; i < 8; i++) {
                        exerciseEntries.add(new Entry(i, exerciseHistory.get(7 - i)));
                    }
                    List<Entry> exerciseEntriesList = exerciseEntries.subList(0,7);
                    exerciseDataSets = new ArrayList<>();

                    LineDataSet exerciseSet1;
                    exerciseSet1 = new LineDataSet(exerciseEntriesList, "Hours Exercised");
                    exerciseSet1 .setColor(Color.rgb(65, 168, 121));
                    exerciseSet1 .setValueTextColor(Color.rgb(55, 70, 73));
                    exerciseSet1 .setValueTextSize(10f);
                    exerciseSet1 .setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    exerciseDataSets.add(exerciseSet1);

                    exerciseLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));

                    LineData data = new LineData(exerciseDataSets);
                    exerciseLineChart.setData(data);
                    exerciseLineChart.animateX(1000);
                    exerciseLineChart.invalidate();
                    exerciseLineChart.getLegend().setEnabled(false);
                    exerciseLineChart.getDescription().setEnabled(false);
                }
            }
        });

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}