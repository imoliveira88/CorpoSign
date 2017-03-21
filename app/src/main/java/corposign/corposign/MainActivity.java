package corposign.corposign;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference myRef;
    List<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.corposign);

        adapter = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("corposign");

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String value = dataSnapshot.getValue(String.class);
                adapter.add(value);
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            public void onCancelled(DatabaseError error) {
            }

        });

        /*TextView dataatual = (TextView) findViewById(R.id.dataatual);
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
        dataatual.setText(currentDateTimeString);*/
    }

    public static List<String> nomeLoginSenha(String banco){
        List<String> lista = new ArrayList<>();
        String parcial = "";
        for(int i=0; i<banco.length(); i++){
            if(banco.charAt(i) != ';') parcial += banco.charAt(i);
            else{
                lista.add(parcial);
                parcial = "";
            }
        }
        lista.add(parcial);

        return lista;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.sobre:
                sobre();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    private void sobre() {
        Intent it = new Intent(MainActivity.this, Sobre.class);
        startActivity(it);
    }

    public void fazLogin(View view){
        String login = ((EditText) findViewById(R.id.txtLogin)).getText().toString();
        String senha = ((EditText) findViewById(R.id.txtSenha)).getText().toString();
        List<String> parcial;
        boolean logou = false;

        for(int i=0; i<adapter.size(); i++){
            parcial = MainActivity.nomeLoginSenha(adapter.get(i));
            if(parcial.get(1).equals(login)){
                if(parcial.get(2).equals(senha)){
                    logou = true;
                    Intent it = new Intent(MainActivity.this, Mapa.class);

                    Bundle bundle = new Bundle();
                    bundle.putString("login",login);
                    it.putExtras(bundle);

                    startActivity(it);
                }
            }
        }

        if(!logou) Toast.makeText(this, "Usuário ou senha inválido!", Toast.LENGTH_SHORT).show();

    }

    public void novoCadastro(View view){
        Intent it = new Intent(MainActivity.this, Cadastro.class);
        startActivity(it);
    }
}
