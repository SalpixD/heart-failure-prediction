package com.example.biopredict;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText inputFieldAge;
    EditText inputFieldEjectFraction;
    EditText inputFieldSerumCreatinine;
    Button predictBtn;
    TextView resultTV;
    Interpreter interpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (IOException e) {
            Log.e("Error: ", "Failed to create interpreter", e);
            throw new RuntimeException(e);
        }

        inputFieldAge = findViewById(R.id.editTextNumber);
        inputFieldEjectFraction = findViewById(R.id.editTextNumber3);
        inputFieldSerumCreatinine = findViewById(R.id.editTextNumber2);

        predictBtn = findViewById(R.id.button);
        resultTV = findViewById(R.id.textView);

        predictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ageInput = inputFieldAge.getText().toString();
                String ejectFractionInput = inputFieldEjectFraction.getText().toString();
                String serumCreatinineInput = inputFieldSerumCreatinine.getText().toString();

                float ageValue = Float.parseFloat(ageInput);
                float ejectFractionValue = Float.parseFloat(ejectFractionInput);
                float serumCreatinineValue = Float.parseFloat(serumCreatinineInput);

                float[][] inputs = {{ageValue, ejectFractionValue, serumCreatinineValue}};

                Map<String, Object> result = doInference(inputs);

                resultTV.setText(
                    String.format("%s \n Probability: %f", result.get("prediction"), result.get("probability"))
                );
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public Map<String, Object> doInference(float[][] input) {
        float[][] output = new float[1][1];
        interpreter.run(input, output);

        float probability = output[0][0];
        return  Map.of(
            "prediction",  probability > 0.5 ? "Death Event 💀" : "Survival 😁👌" ,
            "probability", probability
        );
    }


    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor assetFileDescriptor = this.getAssets().openFd("modelo_regresion_logistica1.tflite");
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long length = assetFileDescriptor.getLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length);
    }
}