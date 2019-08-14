package com.example.fluttershare;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;


/**
 * FlutterSharePlugin
 */
public class FlutterSharePlugin implements MethodCallHandler {

    private Registrar mRegistrar;

    private FlutterSharePlugin(Registrar mRegistrar) {
        this.mRegistrar = mRegistrar;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_share");
        FlutterSharePlugin plugin = new FlutterSharePlugin(registrar);
        channel.setMethodCallHandler(plugin);
    }


    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("share")) {

            File f = null;
            String title = call.argument("title");
            //String message = call.argument("message");
            String fileUrl = call.argument("fileUrl");

            try {
                if (fileUrl == null || fileUrl == "") {
                    Log.println(Log.INFO, "", "FlutterShare: ShareLocalFile Warning: fileUrl null or empty");
                    return;
                }
                String authorities = mRegistrar.context().getPackageName() + ".flutter_share";
                Uri providerUri = FileProvider.getUriForFile(mRegistrar.context(), authorities, new File(Uri.parse(fileUrl).getPath()));

                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_SEND);
                if (fileUrl.toLowerCase().endsWith(".jpg") || fileUrl.toLowerCase().endsWith(".png") || fileUrl.toLowerCase().endsWith(".gif") || fileUrl.toLowerCase().endsWith(".jpeg")) {
                    intent.setType("image/*");
                } else {
                    intent.setType("*/*");
                }
                intent.putExtra(Intent.EXTRA_STREAM, providerUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent chooserIntent = Intent.createChooser(intent, title);
                chooserIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mRegistrar.context().startActivity(chooserIntent);

            } catch (Exception ex) {
                if (ex != null)
                    Log.println(Log.INFO, "", "FlutterShare: Error");

            } finally {
                if (f != null)
                    f.deleteOnExit();
            }

        } else {
            result.notImplemented();
        }
    }


    public void DeleteAllTempFiles() {
        String tempDirPath = mRegistrar.context().getExternalCacheDir()
                + File.separator + "TempFiles" + File.separator;

        File tempDir = new File(tempDirPath);

        if (tempDir.exists()) {

            String[] children = tempDir.list();
            for (int i = 0; i < children.length; i++) {
                new File(tempDir, children[i]).delete();
            }
        }
    }
}
