package pl.edu.agh.sm.magneto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import pl.edu.agh.sm.magneto.R;

public class ConnectActivity extends AppCompatActivity {
    public static final String HOST_MESSAGE = "pl.edu.agh.sm.magneto.HOST_MESSAGE";
    public static final String PORT_MESSAGE = "pl.edu.agh.sm.magneto.PORT_MESSAGE";
    public static final int DEFAULT_PORT = 8192;

    private static final String HOST_PREFERENCE = "hostPeference";
    private static final String PORT_PREFERENCE = "portPeference";

    private EditText hostView;
    private EditText portView;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        hostView = (EditText) findViewById(R.id.host);
        portView = (EditText) findViewById(R.id.port);

        sharedPreferences = getPreferences(MODE_PRIVATE);

        loadPreferences();

        Button connectButton = (Button) findViewById(R.id.connect_button);
        if (connectButton != null) {
            connectButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    connect();
                }
            });
        }
    }

    private void connect() {
        hostView.setError(null);
        portView.setError(null);

        String host = hostView.getText().toString();
        String port = portView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(port)) {
            portView.setError(getString(R.string.error_field_required));
            focusView = portView;
            cancel = true;
        }

        if (TextUtils.isEmpty(host)) {
            hostView.setError(getString(R.string.error_field_required));
            focusView = hostView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            savePreferences(host, port);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(HOST_MESSAGE, host);
            intent.putExtra(PORT_MESSAGE, Integer.valueOf(port));
            startActivity(intent);
        }
    }

    private void loadPreferences() {
        hostView.setText(sharedPreferences.getString(HOST_PREFERENCE, ""));
        portView.setText(sharedPreferences.getString(PORT_PREFERENCE, String.valueOf(DEFAULT_PORT)));
    }

    private void savePreferences(String host, String port) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HOST_PREFERENCE, host);
        editor.putString(PORT_PREFERENCE, port);
        editor.apply();
    }
}

