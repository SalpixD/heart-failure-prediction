package com.example.biopredict;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
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
import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {


    EditText inputFieldAge;
    EditText inputFieldWeight;

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
            throw new RuntimeException(e);
        }


        inputFieldAge = findViewById(R.id.editTextNumber);
        inputFieldWeight = findViewById(R.id.editTextNumber2);

        predictBtn=findViewById(R.id.button);
        resultTV= findViewById(R.id.textView);

        predictBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ageInput = inputFieldAge.getText().toString();
                String weightInput = inputFieldWeight.getText().toString();

                float ageValue = Float.parseFloat(ageInput);
                float weightValue = Float.parseFloat(weightInput);

                float[][] inputs = new float[1][2];
                inputs[0][0] = ageValue;
                inputs[0][1] = weightValue;

                float result = doInference(inputs);

                resultTV.setText("Result: "+result);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public float doInference(float[][] input)
    {
        float[][] output = new float[1][1];
        interpreter.run(input,output);

        return output[0][0];


    }


    private MappedByteBuffer loadModelFile() throws IOException
    {
        AssetFileDescriptor assetFileDescriptor =    			this.getAssets().openFd("linear.tflite");
        FileInputStream fileInputStream = new 		FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = assetFileDescriptor.getStartOffset();
        long length = assetFileDescriptor.getLength();
        return         fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,length);
    }



}