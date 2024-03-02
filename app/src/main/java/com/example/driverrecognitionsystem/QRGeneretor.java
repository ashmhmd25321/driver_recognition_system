package com.example.driverrecognitionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRGeneretor extends AppCompatActivity {

    Button generateQr;
    EditText user;
    ImageView qr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgeneretor);

        generateQr = findViewById(R.id.qrGen);
        user = findViewById(R.id.qrText);
        qr = findViewById(R.id.qrCode);

        String username = getIntent().getStringExtra("qr");

        user.setText(username);
        user.setVisibility(View.GONE);

        generateQr.setOnClickListener(v -> {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(user.getText().toString(), BarcodeFormat.QR_CODE, 300, 300);

                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                qr.setImageBitmap(bitmap);

            } catch (WriterException e) {
                throw new RuntimeException(e);
            }
        });
    }
}