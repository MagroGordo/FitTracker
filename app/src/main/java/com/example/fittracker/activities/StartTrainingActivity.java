package com.example.fittracker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fittracker.R;

public class StartTrainingActivity extends AppCompatActivity {

    private LinearLayout optionCorrida, optionCiclismo;
    private LinearLayout selectedOption = null;

    // Cores
    private final int colorOrange = 0xFFF97316;
    private final int colorWhite = 0xFFFFFFFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_training);

        optionCorrida = findViewById(R.id.optionCorrida);
        optionCiclismo = findViewById(R.id.optionCiclismo);

        setSelectableOption(optionCorrida, R.id.txtCorrida, R.id.radioCorrida);
        setSelectableOption(optionCiclismo, R.id.txtCiclismo, R.id.radioCiclismo);
    }

    private void setSelectableOption(LinearLayout option, int titleId, int radioId) {
        option.setOnClickListener(v -> {

            // 1️⃣ Remove seleção anterior
            if (selectedOption != null) {
                selectedOption.setBackgroundResource(R.drawable.box_unselected);

                // Volta o texto anterior para branco
                TextView previousTitle = selectedOption.findViewById(getTitleId(selectedOption));
                if (previousTitle != null) previousTitle.setTextColor(colorWhite);

                // Esconde o radio anterior
                View previousRadio = selectedOption.findViewById(getRadioId(selectedOption));
                if (previousRadio != null) previousRadio.setVisibility(View.GONE);
            }

            // 2️⃣ Marca nova seleção
            selectedOption = option;
            selectedOption.setBackgroundResource(R.drawable.box_selected);

            // Mostra o radio da nova seleção
            View currentRadio = option.findViewById(radioId);
            if (currentRadio != null) currentRadio.setVisibility(View.VISIBLE);

            // Muda o texto para laranja
            TextView currentTitle = option.findViewById(titleId);
            if (currentTitle != null) currentTitle.setTextColor(colorOrange);
        });
    }

    // Funções auxiliares
    private int getTitleId(LinearLayout option) {
        if (option.getId() == R.id.optionCorrida) return R.id.txtCorrida;
        if (option.getId() == R.id.optionCiclismo) return R.id.txtCiclismo;
        return -1;
    }

    private int getRadioId(LinearLayout option) {
        if (option.getId() == R.id.optionCorrida) return R.id.radioCorrida;
        if (option.getId() == R.id.optionCiclismo) return R.id.radioCiclismo;
        return -1;
    }
}
