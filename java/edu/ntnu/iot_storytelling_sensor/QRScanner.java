package edu.ntnu.iot_storytelling_sensor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mScannerView = new ZXingScannerView(this);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        setContentView(mScannerView);
    }
    @Override
    protected void onPause(){
        super.onPause();
        mScannerView.stopCamera();
    }
    @Override
    public void handleResult(Result result) {
        Intent intent = new Intent();
        intent.putExtra("scanCode",result.getText());
        setResult(RESULT_OK, intent);
        finish();
    }
}
