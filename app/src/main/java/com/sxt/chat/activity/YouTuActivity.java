package com.sxt.chat.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.steelkiwi.cropiwa.util.ImageUriUtil;
import com.sxt.chat.App;
import com.sxt.chat.R;
import com.sxt.chat.base.HeaderActivity;
import com.sxt.chat.json.OCRObject;
import com.sxt.chat.utils.glide.GlideRoundTransform;
import com.sxt.chat.youtu.OCRListener;
import com.sxt.chat.youtu.OCRTask;
import com.sxt.chat.youtu.YouTuConfig;

import java.io.File;

/**
 * Created by 11837 on 2018/6/5.
 */

public class YouTuActivity extends HeaderActivity {

    private final int REQUEST_CODE_GALLERY = 100;
    private final int REQUEST_CODE_CAMARER = 101;
    private final int REQUEST_CROP_PHOTO = 102;
    private final int REQUEST_CODE_TAKE_PHOTO = 103;
    private final int MAX_LENGTH = 2 * 1024 * 1024;
    private TextView result;
    private RadioGroup rgCardType;
    private OCRTask ocrTask;
    private File file;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youto);
        setTitle(R.string.ocr_scan);
        result = findViewById(R.id.result);
        rgCardType = findViewById(R.id.radio_group_card_type);
    }

    private void startCardOCR(final String imgPath) {
        File file = new File(imgPath);
        if (file.exists()) {
            Glide.with(this).load(file)/*.override(320, 198)*/
                    .transform(new CenterCrop(this), new GlideRoundTransform(this, 4))
                    /*.bitmapTransform(new GlideRoundTransform(this, 8))*/
                    .into(((ImageView) findViewById(R.id.img)));

            if (ocrTask != null && !ocrTask.isCancelled()) {
                ocrTask.cancel(true);
            }
            ocrTask = new OCRTask(imgPath, getCardType(), 0, new OCRListener() {
                @Override
                public void onStart() {
                    showDialog();
                }

                @Override
                public void onFaied(final Exception e) {
                    dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            result.setText(e.getMessage());
                        }
                    });
                }

                @Override
                public void onSuccess(OCRObject ocrResult, String result) {
                    dismiss();
                    YouTuActivity.this.result.setText(result);
                }
            });
            ocrTask.execute();
        }
    }

    private void showDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loading.show();
            }
        });
    }

    public void dismiss() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loading != null && loading.isShowing()) {
                    loading.dismiss();
                }
            }
        });
    }

    public void startGallery(View view) {
        boolean b = checkPermission(REQUEST_CODE_GALLERY, Manifest.permission.WRITE_EXTERNAL_STORAGE, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        if (b) {
            startGalleryApp();
        }
    }

    public void startCamera(View view) {
        boolean b = checkPermission(REQUEST_CODE_CAMARER, Manifest.permission.CAMERA, new String[]{Manifest.permission.CAMERA});
        if (b) {
            startCameraApp();
        }
    }

    private void startCameraApp() {
        startActivityForResult(new Intent(this, TakePhotoActivity.class), REQUEST_CODE_TAKE_PHOTO);

//        调用系统相机拍照
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        file = new File(this.getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".png");
//
//        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
//        //添加权限
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        startActivityForResult(intent, REQUEST_CODE_CAMARER);
    }

    private void startGalleryApp() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }

        intent = Intent.createChooser(intent, "选择图片");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    private void startCropActivity(File file) {
        //在手机相册中显示刚拍摄的图片
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

        Intent intent = new Intent(this, CropActivity.class);
        intent.putExtra(CropActivity.CROP_IMG_URI, contentUri);
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_GALLERY) {
                if (data != null && data.getData() != null) {
                    String path = ImageUriUtil.uri2Path(this, data.getData());
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists()) {
                            if (file.length() > MAX_LENGTH) {
                                startCropActivity(file);
                            } else {
                                startCardOCR(path);
                            }
                        }
                    }
                }
            } else if (requestCode == REQUEST_CODE_CAMARER) {
                startCropActivity(file);
            } else if (requestCode == REQUEST_CROP_PHOTO) {
                if (data != null) {
                    Uri uri = data.getParcelableExtra(CropActivity.CROP_IMG_URI);
                    if (uri != null) {
                        startCardOCR(ImageUriUtil.uri2Path(this, uri));
                    }
                }
            } else if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                if (data != null) {
                    Uri uri = data.getParcelableExtra(CropActivity.CROP_IMG_URI);
                    if (uri != null) {
                        File file = new File(uri.getPath());
                        if (file.length() > MAX_LENGTH) {
                            startCropActivity(file);
                        } else {
                            startCardOCR(file.getPath());
                        }
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private boolean checkPermission(int requestCode, String permssion, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(App.getCtx(), permssion) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, requestCode);
                return false;
            }
            return true;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_CAMARER:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraApp();
                } else {
                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
                        Toast(R.string.allow_CAMERA);
                    }
                }
                break;

            case REQUEST_CODE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryApp();
                } else {
                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
                        Toast(R.string.allow_WRITE_EXTERNAL_STORAGE);
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    public String getCardType() {
        switch (rgCardType.getCheckedRadioButtonId()) {
            case R.id.radio_card_type_2:
                return YouTuConfig.TYPE_CREDIT_CARDCOR;
            case R.id.radio_card_type_3:
                return YouTuConfig.TYPE_FOOD_CARDCOR;
            case R.id.radio_card_type_4:
                return YouTuConfig.TYPE_IMAGE_CARDCOR;
            default:
                return YouTuConfig.TYPE_IMAGE_CARDCOR;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ocrTask != null && !ocrTask.isCancelled()) {
            ocrTask.cancel(true);
        }
    }
}
