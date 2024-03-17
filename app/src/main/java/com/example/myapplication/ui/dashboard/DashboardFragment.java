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

// Dashboard fragment = insights page
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

        // Initializing line charts
        sleepLineChart = binding.insightsSleepLinechart;
        sleepLineChart.setTouchEnabled(true);
        sleepLineChart.setDragEnabled(true);
        sleepLineChart.setScaleEnabled(false);
        sleepLineChart.setPinchZoom(false);
        sleepLineChart.setExtraLeftOffset(20);
        sleepLineChart.setExtraRightOffset(20);
        sleepLineChart.getAxisRight().setEnabled(false);

        hydrationLineChart = binding.insightsHydrationLinechart;
        hydrationLineChart.setTouchEnabled(true);
        hydrationLineChart.setDragEnabled(true);
        hydrationLineChart.setScaleEnabled(false);
        hydrationLineChart.setPinchZoom(false);
        hydrationLineChart.setExtraLeftOffset(20);
        hydrationLineChart.setExtraRightOffset(20);
        hydrationLineChart.getAxisRight().setEnabled(false);

        exerciseLineChart = binding.insightsExerciseLinechart;
        exerciseLineChart.setTouchEnabled(true);
        exerciseLineChart.setDragEnabled(true);
        exerciseLineChart.setScaleEnabled(false);
        exerciseLineChart.setPinchZoom(false);
        exerciseLineChart.setExtraLeftOffset(20);
        exerciseLineChart.setExtraRightOffset(20);
        exerciseLineChart.getAxisRight().setEnabled(false);

        XAxis sleepXAxis = sleepLineChart.getXAxis();
        sleepXAxis.setDrawGridLines(false);
        sleepXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        XAxis hydrationXAxis = hydrationLineChart.getXAxis();
        hydrationXAxis.setDrawGridLines(false);
        hydrationXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        XAxis exerciseXAxis = exerciseLineChart.getXAxis();
        exerciseXAxis.setDrawGridLines(false);
        exerciseXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        DocumentReference documentReference = firestore.collection("users").document(userID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (getActivity() != null && documentSnapshot != null) {
                    // Personalize title comment of insights page with user's first name
                    textViewInsightsTitle.setText(getString(R.string.insights_intro, documentSnapshot.getString("firstName")));


                    // Dates for the x-axes of the graphs
                    DateFormat dateFormat = new SimpleDateFormat("MM-dd");
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -7);
                    ArrayList<String> xAxisValues = new ArrayList<>();
                    for(int i = 0; i < 8; i++) {
                        xAxisValues.add(dateFormat.format(cal.getTime()));
                        cal.add(Calendar.DATE, +1);
                    }


                    // Line graph for sleep
                    ArrayList<ILineDataSet> sleepDataSets = new ArrayList<>();
                    List<Long> sleepHistory = (List<Long>)documentSnapshot.get("sleepHistory");
                    ArrayList<Entry> sleepEntries = new ArrayList<>();
                    for(int i = 1; i < 8; i++) {
                        sleepEntries.add(new Entry(i, sleepHistory.get(7 - i)));
                    }
                    List<Entry> sleepEntriesList = sleepEntries.subList(0,7);
                    sleepDataSets = new ArrayList<>();

                    LineDataSet sleepSet;
                    sleepSet = new LineDataSet(sleepEntriesList, "Hours Slept");
                    sleepSet.setColor(Color.rgb(3, 172, 19));
                    sleepSet.setCircleColor(Color.rgb(3, 172, 19));
                    sleepSet.setValueTextColor(Color.rgb(0, 0, 0));
                    sleepSet.setValueTextSize(10f);
                    sleepSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    sleepDataSets.add(sleepSet);

                    sleepLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));
                    sleepLineChart.getXAxis().setAxisMaximum(sleepSet.getXMax() + 0.5f);
                    sleepLineChart.getXAxis().setAxisMinimum(sleepSet.getXMin() - 0.5f);
                    sleepLineChart.getAxisLeft().setSpaceTop(20f);
                    sleepLineChart.getAxisLeft().setSpaceBottom(20f);

                    LineData sleepData = new LineData(sleepDataSets);
                    sleepLineChart.setData(sleepData);
                    sleepLineChart.animateX(500);
                    sleepLineChart.invalidate();
                    sleepLineChart.getLegend().setEnabled(false);
                    sleepLineChart.getDescription().setEnabled(false);


                    // Line graph for hydration
                    ArrayList<ILineDataSet> hydrationDataSets = new ArrayList<>();
                    List<Long> hydrationHistory = (List<Long>)documentSnapshot.get("hydrationHistory");
                    ArrayList<Entry> hydrationEntries = new ArrayList<>();
                    for(int i = 1; i < 8; i++) {
                        hydrationEntries.add(new Entry(i, hydrationHistory.get(7 - i)));
                    }
                    List<Entry> hydrationEntriesList = hydrationEntries.subList(0,7);
                    hydrationDataSets = new ArrayList<>();

                    LineDataSet hydrationSet;
                    hydrationSet = new LineDataSet(hydrationEntriesList, "Cups Drank");
                    hydrationSet.setColor(Color.rgb(32, 127, 194));
                    hydrationSet.setCircleColor(Color.rgb(32, 127, 194));
                    hydrationSet.setValueTextColor(Color.rgb(0, 0, 0));
                    hydrationSet.setValueTextSize(10f);
                    hydrationSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    hydrationDataSets.add(hydrationSet);

                    hydrationLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));
                    hydrationLineChart.getXAxis().setAxisMaximum(hydrationSet.getXMax() + 0.5f);
                    hydrationLineChart.getXAxis().setAxisMinimum(hydrationSet.getXMin() - 0.5f);
                    hydrationLineChart.getAxisLeft().setSpaceTop(20f);
                    hydrationLineChart.getAxisLeft().setSpaceBottom(20f);

                    LineData hydrationData = new LineData(hydrationDataSets);
                    hydrationLineChart.setData(hydrationData);
                    hydrationLineChart.animateX(500);
                    hydrationLineChart.invalidate();
                    hydrationLineChart.getLegend().setEnabled(false);
                    hydrationLineChart.getDescription().setEnabled(false);


                    // Line graph for exercise
                    ArrayList<ILineDataSet> exerciseDataSets = new ArrayList<>();
                    List<Long> exerciseHistory = (List<Long>)documentSnapshot.get("exerciseHistory");
                    ArrayList<Entry> exerciseEntries = new ArrayList<>();
                    for(int i = 1; i < 8; i++) {
                        exerciseEntries.add(new Entry(i, exerciseHistory.get(7 - i)));
                    }
                    List<Entry> exerciseEntriesList = exerciseEntries.subList(0,7);
                    exerciseDataSets = new ArrayList<>();

                    LineDataSet exerciseSet;
                    exerciseSet = new LineDataSet(exerciseEntriesList, "Hours Exercised");
                    exerciseSet.setColor(Color.rgb(186, 53, 37));
                    exerciseSet.setCircleColor(Color.rgb(186, 53, 37));
                    exerciseSet.setValueTextColor(Color.rgb(0, 0, 0));
                    exerciseSet.setValueTextSize(10f);
                    exerciseSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    exerciseDataSets.add(exerciseSet);

                    exerciseLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisValues));
                    exerciseLineChart.getXAxis().setAxisMaximum(exerciseSet.getXMax() + 0.5f);
                    exerciseLineChart.getXAxis().setAxisMinimum(exerciseSet.getXMin() - 0.5f);
                    exerciseLineChart.getAxisLeft().setSpaceTop(20f);
                    exerciseLineChart.getAxisLeft().setSpaceBottom(20f);

                    LineData data = new LineData(exerciseDataSets);
                    exerciseLineChart.setData(data);
                    exerciseLineChart.animateX(500);
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