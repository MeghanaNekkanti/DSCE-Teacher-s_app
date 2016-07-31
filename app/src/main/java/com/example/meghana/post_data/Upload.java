package com.example.meghana.post_data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URISyntaxException;

public class Upload extends AppCompatActivity {

    private static final String TAG = "testing";
    private static int RESULT_LOAD_IMG = 1;
    ImageView imageView;
    String semester, sections, selectedfilepath;
    Uri selectedfile, downloadUrl;
   FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReferenceFromUrl("gs://test-b9492.appspot.com/");

    StorageReference spaceRef = storageReference.child("images/101.JPG");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imageView = (ImageView) findViewById(R.id.imageView);

/*
        storageReference1.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d("test", "onSuccess: " + bytes);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                imageView.setImageBitmap(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("error", "onFailure: error");

                // Handle any errors
            }
        });
*/
        Intent intent = getIntent();
        semester = intent.getStringExtra("semester");
        sections = intent.getStringExtra("sections");


    }

    public void onClick(View view) {

       /* final String[] ACCEPT_MIME_TYPES = {
                "application/pdf",
                "image*//*"
        };*/

        Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        intent.putExtra("CONTENT_TYPE", "*/*");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
       // intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);

       /* Intent intentdrive = new Intent();
        intentdrive.setType("image,application/pdf");
        intentdrive.setAction(Intent.ACTION_GET_CONTENT);
        // intent.putExtra(Intent.EXTRA_MIME_TYPES, ACCEPT_MIME_TYPES);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
*/
        startActivityForResult(Intent.createChooser(intent, "choose file to upload"), 1);
/*
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        // File or Blob
*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                selectedfile = data.getData();

                Log.d(TAG, "onActivityResult: " + selectedfile.toString());
                selectedfilepath = null;
                try {
                    selectedfilepath = getPath(this, selectedfile);
                    display(selectedfilepath);

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onActivityResult: " + selectedfilepath);
            }
            super.onActivityResult(requestCode, resultCode, data);

        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    private void display(String selectedfilepath) {


        File imgFile = new File(selectedfilepath);

        if (imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


            imageView.setImageBitmap(myBitmap);

        }


    }


        /*StorageReference storageReference1 = storageReference.child(selectedfilepath);
        storageReference1.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d("test", "onSuccess: " + bytes);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                imageView.setImageBitmap(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("error", "onFailure: error");

                // Handle any errors
            }
        });*/


    private void upload_to_database(String selectedfilepath) {

        Uri file = Uri.fromFile(new File(selectedfilepath));
        UploadTask uploadTask = storageReference.child("images/" + file.getLastPathSegment()).putFile(file);


        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "onProgress: Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onPaused: Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "onFailure: failed");
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                Log.d(TAG, "onSuccess: " + downloadUrl);
            }
        });


    }


    public static String getPath(Context context, Uri uri) throws URISyntaxException {

        /*final String id = DocumentsContract.getDocumentId(uri);
        final Uri contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));*/
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Files.FileColumns.DATA};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {

            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    public void onClickUpload(View view) {
        upload_to_database(selectedfilepath);

    }
}



       /* storageReference1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Log.d("urltest", "onCreate: " + uri);
                *//*try {

                    URL url = new URL(uri.toString());

                    //create the new connection
                    Log.d("url", "onSuccess: "+url);

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    //set up some things on the connection
                    urlConnection.setRequestMethod("GET");

                    urlConnection.setDoOutput(true);

                    //and connect!

                    urlConnection.connect();

                    //set the path where we want to save the file
                    //in this case, going to save it on the root directory of the
                    //sd card.

                    File folder = new File(Environment.getExternalStorageDirectory().toString() + "/download");
                    Log.d("folder", "onSuccess: " + folder);

                    Boolean bool = folder.mkdirs();
                    Log.d("folder", "onSuccess: "+bool);

                    //create a new file, specifying the path, and the filename
                    //which we want to save the file as.

                    String filename = "2013-05-15 18.02.51.jpg";

                    File file = new File(folder, filename);

                    if (file.createNewFile())

                    {

                        Boolean bool1 = file.createNewFile();
                        Log.d("createfile", "onSuccess: "+bool1);

                    }

                    //this will be used to write the downloaded data into the file we created
                    FileOutputStream fileOutput = new FileOutputStream(file);

                    //this will be used in reading the data from the internet
                    InputStream inputStream = urlConnection.getInputStream();

                    //this is the total size of the file
                    int totalSize = urlConnection.getContentLength();
                    //variable to store total downloaded bytes
                    int downloadedSize = 0;

                    //create a buffer...
                    byte[] buffer = new byte[2056];
                    int bufferLength = 0; //used to store a temporary size of the buffer

                    //now, read through the input buffer and write the contents to the file
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        //add the data in the buffer to the file in the file output stream (the file on the sd card
                        fileOutput.write(buffer, 0, bufferLength);
                        //add up the size so we know how much is downloaded
                        downloadedSize += bufferLength;
                        //this is where you would do something to report the prgress, like this maybe
                        Log.i("Progress:", "downloadedSize:" + downloadedSize + "totalSize:" + totalSize);
                    }
                    //close the output stream when done
                    fileOutput.close();
                    if (downloadedSize == totalSize)
                        //String filepath = file.getPath();
                        Log.i("filepath:", " " + file.getPath());


                    //catch some possible errors...
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    //filepath = null;
                    e.printStackTrace();
                }


                // return filepath;

*//*
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("urltest", "onFailure: error ");
                // Handle any errors
            }
        });
    }*/


