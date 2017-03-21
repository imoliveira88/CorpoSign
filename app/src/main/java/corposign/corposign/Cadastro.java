package corposign.corposign;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class Cadastro extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference myRef;
    List<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

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
    }

    public boolean existeLogin(String login){
        List<String> parcial;

        for(int i=0; i<adapter.size(); i++){
            parcial = MainActivity.nomeLoginSenha(adapter.get(i));
            if(parcial.get(1).equals(login)) return true;
        }

        return false;

    }

    public void cadastrar(View view){
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("corposign");
            DatabaseReference childRef = myRef.push();

            final EditText nome = (EditText) findViewById(R.id.txtNome);
            final EditText login = (EditText) findViewById(R.id.txtLogin);
            final EditText senha = (EditText) findViewById(R.id.txtSenha);

            if(existeLogin(login.getText().toString())) Toast.makeText(this, "Já existe um usuário com este login! Por favor, escolha outro!", Toast.LENGTH_SHORT).show();
            else{
                childRef.setValue(nome.getText().toString() + ";" + login.getText().toString() + ";" + senha.getText().toString());
                Toast.makeText(this, "Cadastro realizado com sucesso! Realize login!", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(Cadastro.this,MainActivity.class);
                startActivity(it);
            }

    }
}
