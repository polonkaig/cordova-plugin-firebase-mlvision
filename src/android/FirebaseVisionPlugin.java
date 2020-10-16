package by.alon22.cordova.firebase;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * This class echoes a string called from JavaScript.
 */
public class FirebaseVisionPlugin extends CordovaPlugin {

    protected static Context applicationContext = null;
    private static Activity cordovaActivity = null;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        cordovaActivity = this.cordova.getActivity();
        applicationContext = cordovaActivity.getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("onDeviceTextRecognizer")) {
            String message = args.getString(0);
            this.onDeviceTextRecognizer(message, callbackContext);
            return true;
        } else if (action.equals("barcodeDetector")) {
            String message = args.getString(0);
            this.barcodeDetector(message, callbackContext);
            return true;
        }
        return false;
    }

    private void onDeviceTextRecognizer(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            try {
                Uri uri = Uri.parse(message);
                InputImage image = InputImage.fromFilePath(applicationContext, uri);
                TextRecognizer recognizer = TextRecognition.getClient();
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text firebaseVisionText) {
                                try {
                                    JSONObject text = FirebaseUtils.parseText(firebaseVisionText);
                                    callbackContext.success(text);
                                } catch (Exception e) {
                                    callbackContext.error(e.getLocalizedMessage());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                callbackContext.error(e.getLocalizedMessage());
                            }
                        });
            } catch (Exception e) {
                callbackContext.error(e.getLocalizedMessage());
            }
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void barcodeDetector(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            try {
                //Uri uri = Uri.parse(message);
                //InputImage image = InputImage.fromFilePath(applicationContext, uri);
                byte[] decodedString = Base64.decode(message, Base64.DEFAULT);
                Bitmap bitMap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                InputImage image = InputImage.fromBitmap(bitMap, 0);

                BarcodeScanner detector = BarcodeScanning.getClient();
                detector.process(image)
                        .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                            @Override
                            public void onSuccess(List<Barcode> firebaseVisionBarcodes) {
                                try {
                                    JSONArray barcodes = FirebaseUtils.parseBarcode(firebaseVisionBarcodes);
                                    callbackContext.success(barcodes);
                                } catch (Exception e) {
                                    callbackContext.error(e.getLocalizedMessage());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                callbackContext.error(e.getLocalizedMessage());
                            }
                        });
            } catch (Exception e) {
                callbackContext.error(e.getLocalizedMessage());
            }
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
