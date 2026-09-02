package com.abpi.student.finance.study.organizer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.abpi.student.finance.study.organizer.databinding.ActivityMainBinding;
import com.abpi.student.finance.study.organizer.databinding.BottomSheetAddTransactionBinding;
import com.abpi.student.finance.study.organizer.databinding.FragmentDashboardBinding;
import com.abpi.student.finance.study.organizer.databinding.FragmentFinanceBinding;
import com.abpi.student.finance.study.organizer.databinding.FragmentStudyBinding;
import com.abpi.student.finance.study.organizer.databinding.FragmentTimerBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Screen ViewBindings
    private FragmentDashboardBinding dashboardBinding;
    private FragmentFinanceBinding financeBinding;
    private FragmentStudyBinding studyBinding;
    private FragmentTimerBinding timerBinding;

    // Pomodoro Timer State
    private boolean isTimerRunning = false;
    private static final long TOTAL_TIME_IN_MILLIS = 25 * 60 * 1000L;
    private long timeLeftInMillis = TOTAL_TIME_IN_MILLIS;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize screen bindings
        LayoutInflater inflater = getLayoutInflater();
        dashboardBinding = FragmentDashboardBinding.inflate(inflater);
        financeBinding = FragmentFinanceBinding.inflate(inflater);
        studyBinding = FragmentStudyBinding.inflate(inflater);
        timerBinding = FragmentTimerBinding.inflate(inflater);

        setupInsets();
        setupNavigation();
        setupDashboardActions();
        setupFinanceScreen();
        setupStudyScreen();
        setupTimerScreen();

        // Default screen: Dashboard
        showDashboard();
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.topAppBar.setPadding(0, systemBars.top, 0, 0);
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                showDashboard();
                return true;
            } else if (itemId == R.id.nav_finance) {
                showFinance();
                return true;
            } else if (itemId == R.id.nav_study) {
                showStudy();
                return true;
            } else if (itemId == R.id.nav_timer) {
                showTimer();
                return true;
            }
            return false;
        });
    }

    private void showDashboard() {
        binding.topAppBar.setTitle(getString(R.string.nav_dashboard));
        binding.fragmentContainer.removeAllViews();
        binding.fragmentContainer.addView(dashboardBinding.getRoot());
    }

    private void showFinance() {
        binding.topAppBar.setTitle(getString(R.string.finance_title));
        binding.fragmentContainer.removeAllViews();
        binding.fragmentContainer.addView(financeBinding.getRoot());
    }

    private void showStudy() {
        binding.topAppBar.setTitle(getString(R.string.study_title));
        binding.fragmentContainer.removeAllViews();
        binding.fragmentContainer.addView(studyBinding.getRoot());
    }

    private void showTimer() {
        binding.topAppBar.setTitle(getString(R.string.timer_title));
        binding.fragmentContainer.removeAllViews();
        binding.fragmentContainer.addView(timerBinding.getRoot());
    }

    private void setupDashboardActions() {
        dashboardBinding.btnQuickExpense.setOnClickListener(v -> showAddTransactionBottomSheet());
        dashboardBinding.btnQuickTask.setOnClickListener(v -> binding.bottomNavigation.setSelectedItemId(R.id.nav_study));
        dashboardBinding.btnDashboardStartFocus.setOnClickListener(v -> binding.bottomNavigation.setSelectedItemId(R.id.nav_timer));
    }

    private void setupFinanceScreen() {
        financeBinding.fabAddTransaction.setOnClickListener(v -> showAddTransactionBottomSheet());
    }

    private void setupStudyScreen() {
        studyBinding.fabAddTask.setOnClickListener(v -> 
            Snackbar.make(binding.getRoot(), "Create Assignment or Course Dialog", Snackbar.LENGTH_SHORT).show()
        );
    }

    private void setupTimerScreen() {
        timerBinding.btnTimerToggle.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        timerBinding.btnTimerReset.setOnClickListener(v -> resetTimer());
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                timerBinding.btnTimerToggle.setText(getString(R.string.timer_btn_start));
                Snackbar.make(binding.getRoot(), "Focus session complete! Take a break.", Snackbar.LENGTH_LONG).show();
            }
        }.start();

        isTimerRunning = true;
        timerBinding.btnTimerToggle.setText(getString(R.string.timer_btn_pause));
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timerBinding.btnTimerToggle.setText(getString(R.string.timer_btn_start));
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timeLeftInMillis = TOTAL_TIME_IN_MILLIS;
        updateTimerDisplay();
        timerBinding.btnTimerToggle.setText(getString(R.string.timer_btn_start));
    }

    private void updateTimerDisplay() {
        long minutes = (timeLeftInMillis / 1000) / 60;
        long seconds = (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerBinding.tvTimerClock.setText(timeFormatted);

        int progressPercent = (int) (((double) timeLeftInMillis / (double) TOTAL_TIME_IN_MILLIS) * 100);
        timerBinding.progressTimer.setProgress(progressPercent);
    }

    private void showAddTransactionBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        BottomSheetAddTransactionBinding sheetBinding = BottomSheetAddTransactionBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.btnCancelTransaction.setOnClickListener(v -> bottomSheetDialog.dismiss());

        sheetBinding.btnSaveTransaction.setOnClickListener(v -> {
            if (sheetBinding.etAmount.getText() != null) {
                String amount = sheetBinding.etAmount.getText().toString().trim();
                if (!amount.isEmpty()) {
                    Snackbar.make(binding.getRoot(), "Transaction of $" + amount + " saved!", Snackbar.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                    return;
                }
            }
            sheetBinding.tilAmount.setError("Please enter an amount");
        });

        bottomSheetDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
