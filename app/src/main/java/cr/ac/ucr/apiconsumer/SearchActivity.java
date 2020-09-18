package cr.ac.ucr.apiconsumer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    private TextInputEditText etChangeLocation;
    static final String KEY = "city";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar tToolbar = findViewById(R.id.t_toolbar);
        tToolbar.setTitle("Change City");
        setSupportActionBar(tToolbar);

        // TODO: boton de atras
        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        etChangeLocation = findViewById(R.id.et_change_location);

    }

    @Override
    public void onClick(View v) {
        String city = etChangeLocation.getText().toString();
        if(!city.isEmpty()){
            Intent intent = getIntent();
            intent.putExtra(KEY, city);
            setResult(RESULT_OK, intent);
            finish();
        } else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}