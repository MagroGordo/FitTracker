package com.example.fittracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fittracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextView profileBtn;
    private TextView btnConfirmar;
    private EditText inputNovaPassword, inputConfirmarPassword;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        auth = FirebaseAuth.getInstance();

        profileBtn = findViewById(R.id.profileBtn);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        inputNovaPassword = findViewById(R.id.inputNovaPassword);
        inputConfirmarPassword = findViewById(R.id.inputConfirmarPassword);

        // Voltar para o perfil
        profileBtn.setOnClickListener(v -> {
            startActivity(new Intent(ChangePasswordActivity.this, ProfileActivity.class));
            finish();
        });

        btnConfirmar.setOnClickListener(v -> attemptChange());
    }

    private void attemptChange() {
        String newPwd = safe(inputNovaPassword);
        String confirmPwd = safe(inputConfirmarPassword);

        // Validações
        if (TextUtils.isEmpty(newPwd)) {
            inputNovaPassword.setError("Nova palavra-passe obrigatória");
            inputNovaPassword.requestFocus();
            return;
        }
        if (newPwd.length() < 6) {
            inputNovaPassword.setError("A palavra-passe deve ter pelo menos 6 caracteres");
            inputNovaPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPwd)) {
            inputConfirmarPassword.setError("Confirme a palavra-passe");
            inputConfirmarPassword.requestFocus();
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            inputConfirmarPassword.setError("As palavras-passe não coincidem");
            inputConfirmarPassword.requestFocus();
            Toast.makeText(this, "As palavras-passe não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser current = auth.getCurrentUser();
        if (current == null || current.getEmail() == null) {
            Toast.makeText(this, "Sessão inválida.", Toast.LENGTH_LONG).show();
            return;
        }

        // Se sign-in com a nova password funcionar, então já é a atual
        auth.signInWithEmailAndPassword(current.getEmail(), newPwd)
                .addOnSuccessListener(res -> {
                    auth.signOut();
                    Toast.makeText(this, "A nova palavra‑passe é igual à atual.", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    // Atualizar diretamente sem reencaminhar para login
                    current.updatePassword(newPwd)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Palavra‑passe alterada com sucesso.", Toast.LENGTH_LONG).show();
                                // Mantém na mesma tela ou volta ao perfil, como preferires:
                                startActivity(new Intent(this, ProfileActivity.class));
                                finish();
                            })
                            .addOnFailureListener(err -> {
                                // Não redireciona para login; apenas mensagem curta
                                Toast.makeText(this, "Não foi possível alterar agora. Tenta novamente mais tarde.", Toast.LENGTH_LONG).show();
                            });
                });
    }

    private String safe(EditText et) {
        CharSequence cs = et.getText();
        return cs == null ? "" : cs.toString().trim();
    }
}