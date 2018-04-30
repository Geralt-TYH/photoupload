package com.tangyinhao.photoupload;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String APP_ID = "11093770";
    public static final String API_KEY = "GsfcG2IhbHPKHeVzH9GjgDPq";
    public static final String SECRET_KEY = "Z9NnEWi7tpG45ACBfKCIh2GdkuLzrOKm";
    HashMap<String, String> options = new HashMap<String, String>();
    AipImageClassify client=new AipImageClassify(APP_ID,API_KEY,SECRET_KEY);

    HashMap<String, Bitmap> temp=new HashMap<String, Bitmap>() ;

    private static final String TAG = "MainActivity";
    private MyDatabaseHelper dbHelper;
    public static final int TAKE_PHOTO=1;
    public static final int CHOSSE_PHOTO =2;
    private ImageView picture;
    private Bitmap picBitmap;
    private Uri imageUri;
    private TextView showText;
    private StringBuffer buffer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper=new MyDatabaseHelper(this,"DrugInfo.db",null,1);
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        initData(db);

        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");

        picture=(ImageView) findViewById(R.id.personal_info_header_img);

        Button choosePhto=(Button)findViewById(R.id.chooseFrom);
        choosePhto.setOnClickListener(this);

        Button upload=(Button)findViewById(R.id.upload);
        upload.setOnClickListener(this);

        showText=(TextView) findViewById(R.id.showText);
    }
    private void initData(SQLiteDatabase db){
        ContentValues values=new ContentValues();
        values.put("name","藏红花");
        values.put("intro","藏红花主要作用于血，具有养血、活血、补血、行血、理血等功能。此外，藏红花具有活血化瘀、凉血解毒、解郁安神、美容养颜等功效，主治月经不调、经闭、产后瘀血腹痛、不孕不育等妇科疾病，对治疗心脑血管疾病、调节肝肾功能、调三高、抗肿瘤癌症等疗效显著。");
        db.insert("Drug",null,values);
        values.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Intent intent=new Intent("android.intent.action.GET_CONTENT");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent,CHOSSE_PHOTO);
                }else{
                    Toast.makeText(this,"需要权限",Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try{
                        Bitmap bitmap=BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picBitmap=bitmap.copy(Bitmap.Config.ARGB_4444, true);
                        picture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOSSE_PHOTO:
                if (resultCode==RESULT_OK){
                    if (Build.VERSION.SDK_INT>=19){
                        handleImageOnKitkat(data);
                    }else{
                        handleImageBeforeKitkat(data);
                    }
                }
                break;
            default:
                break;
        }


    }

    private void handleImageBeforeKitkat(Intent data) {
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        displayImage(imagePath);
    }

    private void handleImageOnKitkat(Intent data) {
//        String imagePath=null;
//        Uri uri=data.getData();
//        if (DocumentsContract.isDocumentUri(this,uri)){
//            String docId=DocumentsContract.getDocumentId(uri);
//            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
//                String id = docId.split(":")[1]; //解析出数字格式的id
//                String selection = MediaStore.Images.Media._ID + "=" + id;
//                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
//                if (imagePath==null)
//                    Toast .makeText (this, "图片路径空", Toast. LENGTH_SHORT). show();
//            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
//                Uri contenturi = ContentUris.withAppendedId(
//                        uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
//                imagePath = getImagePath( contenturi, null);
//                if (imagePath==null)
//                    Toast .makeText (this, "图片路径空", Toast. LENGTH_SHORT). show();
//            }else if ("content".equalsIgnoreCase(uri.getScheme())) {
//                //如果不是document类型的uri,则使用普通方式处理
//                imagePath = getImagePath(uri, null);
//                if (imagePath==null)
//                    Toast .makeText (this, "图片路径空", Toast. LENGTH_SHORT). show();
//            }else if ("file".equalsIgnoreCase(uri.getScheme())){
//                imagePath=uri.getPath();
//                if (imagePath==null)
//                    Toast .makeText (this, "图片路径空", Toast. LENGTH_SHORT). show();
//            }
//            displayImage(imagePath);
//        }
        Uri uri=data.getData();
        Bitmap image;
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        picBitmap=image.copy(Bitmap.Config.ARGB_4444, true);
        picture.setImageBitmap(image);
        image=null;
}

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap= BitmapFactory.decodeFile(imagePath);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenWidth=dm.widthPixels;
            if(bitmap.getWidth()<=screenWidth){
                picture.setImageBitmap(bitmap);
            }else{
                Bitmap bmp=Bitmap.createScaledBitmap(bitmap, screenWidth, bitmap.getHeight()*screenWidth/bitmap.getWidth(), true);
                picture.setImageBitmap(bmp);
                bmp=null;
            }
        } else {
            Toast .makeText (this, "failed to get image", Toast. LENGTH_SHORT). show();
        }
    }

    private String getImagePath(Uri uri, String  selection) {
        String path = null;
        //通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    private void showPopueWindow(){
        View popView = View.inflate(this,R.layout.popuewindow,null);
        Button bt_album = (Button) popView.findViewById(R.id.btn_pop_album);
        Button bt_camera = (Button) popView.findViewById(R.id.btn_pop_camera);
        Button bt_cancle = (Button) popView.findViewById(R.id.btn_pop_cancel);
        //获取屏幕宽高
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels*1/3;

        final PopupWindow popupWindow = new PopupWindow(popView,weight,height);
        //popupWindow.setAnimationStyle(R.style.anim_popup_dir);
        popupWindow.setFocusable(true);
        //点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);

        bt_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i,CHOSSE_PHOTO);
                popupWindow.dismiss();
            }
        });
        bt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage=new File(getExternalCacheDir(),"output_image.jpg");
                try{
                    if (outputImage.exists())
                        outputImage.delete();
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT>=24){
                    imageUri= FileProvider.getUriForFile(MainActivity.this,"com.tangyinhao.photoupload.fileprovider",outputImage);
                }
                else {
                    imageUri=Uri.fromFile(outputImage);
                }
                Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                startActivityForResult(intent,TAKE_PHOTO);

            }
        });
        bt_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });
        //popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });
        //popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,50);

    }

    private void upImage(Bitmap bm) {
        // TODO: 2018/4/6 增加上传图片功能
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG, 60, stream);
//        byte[] bytes = stream.toByteArray();
//        String img = new String(Base64.encodeToString(bytes, Base64.DEFAULT));
//        AsyncHttpClient client = new AsyncHttpClient();
//        RequestParams params = new RequestParams();
//        params.add("img", img);
//        client.post("http://120.79.207.213/drugimg/imgupload.php", params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
//                Toast.makeText(MainActivity.this, "Upload Success!", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
//                Toast.makeText(MainActivity.this, "Upload Fail!", Toast.LENGTH_LONG).show();
//            }
//        });
        temp.put("hah",bm);
        new Thread(new Runnable() {
            String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/plant";
            Bitmap bm=temp.get("hah");
            @Override
            public void run() {
                try{
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 90, stream);
                    byte[] imgData = stream.toByteArray();
                    stream.close();
                    String imgStr = new String(Base64Util.encode(imgData));
                    String imgParam = URLEncoder.encode(imgStr, "UTF-8");
                    String param = "image=" + imgParam;
                    String accessToken = AuthService.getAuth();
                    String result = HttpUtil.post(url, accessToken, param);
                    System.out.println(result);
                    AnswerBean answerBean = new Gson().fromJson(result,AnswerBean.class);
                    final List<AnswerBean.resultBean> userBeanList = answerBean.getResult();
                    buffer=new StringBuffer();
                    for (AnswerBean.resultBean as:userBeanList){
                        buffer.append(as.getName()+" "+as.getScore()+"\n");
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            String info="查询失败";
                            SQLiteDatabase db=dbHelper.getWritableDatabase();
                            Cursor cursor=db.rawQuery("select intro from Drug where name=?",new String[]{userBeanList.get(0).getName()});
                            if (cursor.moveToFirst()) {
                                    info=cursor.getString(cursor.getColumnIndex("intro"));
                            }
                            cursor.close();
                            showText.setText(info);
                            buffer=null;
                        }
                    });
                    System.out.printf(buffer.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chooseFrom:
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String []{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    showPopueWindow();
                }
                break;
            case R.id.upload:
                upImage(picBitmap);
                break;
        }
    }
}
