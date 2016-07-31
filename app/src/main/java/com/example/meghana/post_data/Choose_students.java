package com.example.meghana.post_data;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Choose_students extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ProgressDialog progressDialog;
    private static final String TAG = "testing";
    final CharSequence[] items = {"A", "B", "C", "D"};
    final ArrayList seletedItems = new ArrayList();
    private long backPressedTime = 0;
    private static int RESULT_LOAD_IMG = 1;
    String item, uid, department, selectedfilepath, username;
    String type = "text";
    ImageView imageView;
    Spinner spinner, dept;
    Uri selectedfile, downloadUrl;
    EditText editText;
    TextView textView;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReferenceFromUrl("gs://test-b9492.appspot.com/");
    ArrayList<String> tokens = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_students);

        //   Intent intent = getIntent();
        uid = getSharedPreferences("login", Context.MODE_PRIVATE).getString("uid", "123456789");
        username = getSharedPreferences("login", Context.MODE_PRIVATE).getString("name", "meghana");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seletedItems.clear();
                select_sections();

            }
        });


        //  listView = (ListView) findViewById(R.id.listView);


        spinner = (Spinner) findViewById(R.id.semester);
        dept = (Spinner) findViewById(R.id.department);
        editText = (EditText) findViewById(R.id.editText2);
        imageView = (ImageView) findViewById(R.id.imageView2);
        textView = (TextView) findViewById(R.id.file_name);

        editText.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.semester, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.department, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dept.setAdapter(adapter1);
        dept.setOnItemSelectedListener(this);

    }

    private void select_sections() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select The Sections")
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {

                            // If the user checked the item, add it to the selected items
                            seletedItems.add(items[indexSelected]);

                        } else if (seletedItems.contains(items[indexSelected])) {
                            // Else, if the item is already in the array, remove it
                            seletedItems.remove(items[indexSelected]);
                        }


                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog = new ProgressDialog(Choose_students.this);
                        progressDialog.setMessage("Uploading ");
                        progressDialog.setIndeterminate(true);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        update();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // clear();


                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }

    private void update() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Teachers");
        Log.d(TAG, "update: " + uid);

        myRef.child(uid).child("Department").setValue(department);
        myRef.child(uid).child("Semester").setValue(item);
        if (type.equals("file") || type.equals("image")) {
            Log.d(TAG, "update: " + type + selectedfilepath);
            upload_to_database(selectedfilepath);

        } else
            upload_text();


    }

    private void upload_text() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference PostRef = database.getReference("Post");
        String text = editText.getText().toString();
        String url_key = PostRef.push().getKey();
        for (int i = 0; i < seletedItems.size(); i++) {

            String sec = seletedItems.get(i).toString();
            Log.d(TAG, "onSuccess: " + sec);
            PostRef.child(department).child(item).child(sec).child(url_key).child("name").setValue(username);
            PostRef.child(department).child(item).child(sec).child(url_key).child("text").setValue(text);
            PostRef.child(department).child(item).child(sec).child(url_key).child("type").setValue(type);
        }
        sendNotification();
        Toast.makeText(Choose_students.this, "UPLOAD SUCCESSFUL", Toast.LENGTH_SHORT).show();
        editText.setText("");
        progressDialog.dismiss();

    }

    private void upload_to_database(String selectedfilepath) {

        Uri file = Uri.fromFile(new File(selectedfilepath));
        final String file_name = file.getLastPathSegment();
        Log.d(TAG, "upload_to_database: filename" + file.getLastPathSegment());
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

                Toast.makeText(Choose_students.this, "upload successful", Toast.LENGTH_SHORT).show();

                downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();

                Log.d(TAG, "onSuccess: " + "https:" + downloadUrl.getEncodedSchemeSpecificPart());
                String file_url = "https:" + downloadUrl.getEncodedSchemeSpecificPart();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference PostRef = database.getReference("Post");

                String text = editText.getText().toString();
                String url_key = PostRef.push().getKey();
                for (int i = 0; i < seletedItems.size(); i++) {

                    String sec = seletedItems.get(i).toString();
                    Log.d(TAG, "onSuccess: " + sec);
                    PostRef.child(department).child(item).child(sec).child(url_key).child("Url").setValue(file_url);
                    PostRef.child(department).child(item).child(sec).child(url_key).child("filename").setValue(file_name);
                    PostRef.child(department).child(item).child(sec).child(url_key).child("text").setValue(text);
                    PostRef.child(department).child(item).child(sec).child(url_key).child("type").setValue(type);
                    PostRef.child(department).child(item).child(sec).child(url_key).child("name").setValue(username);
                    progressDialog.dismiss();
                    new AlertDialog.Builder(Choose_students.this)
                            .setTitle("Uploaded")
                            .setMessage(" Thanku! Your upload was successful.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Choose_students.this, Choose_students.class);
                                    startActivity(intent);
                                    finish();
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    sendNotification();

                    // Log.d(TAG, "onSuccess: " + url_key);
                }

            }
        });
    }

    private void sendNotification() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference students = database.getReference("Students");
        students.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());

                Map<String, Object> hm = (Map<String, Object>) dataSnapshot.getValue();

                Set<String> set = hm.keySet();

                for (String key : set) {
                    Map<String, String> m = (Map<String, String>) hm.get(key);
                    String section = m.get("Section");
                    String dept = m.get("Dept");
                    String semester = m.get("Semester");
                    String tokenID = m.get("Token");
//                    Log.d(TAG, "onDataChange: "+section + " "+semester+" "+tokenID);
                    if (dept.equals(department) && semester.equals(item)) {
                        for (int i = 0; i < seletedItems.size(); i++) {
                            if (section.equals(seletedItems.get(i)))
                                tokens.add(tokenID);
                        }
                    }
                }

                for (int i = 0; i < tokens.size(); i++) {
                    final int finalI = i;
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            BufferedReader mBufferedInputStream;
                            String Response = "";

                            try {

                                //google cloud api url
                                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                                httpURLConnection.setConnectTimeout(15000);
                                httpURLConnection.setReadTimeout(10000);
                                httpURLConnection.setDoInput(true);
                                httpURLConnection.setDoOutput(true);

                                //set the headers
                                httpURLConnection.setRequestProperty("Authorization", "key=AIzaSyCTz8gCofq2DNKkV5YaGs3YpQnGhW48s4A");
                                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                                httpURLConnection.setRequestMethod("POST");

                                // write all the parameters into JSON
                                JSONObject data = new JSONObject();

                                JSONObject main = new JSONObject();

                                // if the task is called for sending message purpose
                                try {
                                    data.put("title", "DSCE Notify");
                                    data.put("text", "You have new notification from DSCE Notify");

                                    main.put("to", tokens.get(finalI));
                                    main.put("notification", data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                //convert the JSON object to string and write it to the output stream
                                String query = main.toString();

                                Log.d(TAG, "doInBackground: " + query);
                                OutputStream os = httpURLConnection.getOutputStream();

                                BufferedWriter mBufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                                mBufferedWriter.write(query);
                                mBufferedWriter.flush();
                                mBufferedWriter.close();
                                os.close();

                                httpURLConnection.connect();

                                Log.d("GCM SENT RESPONSE", "response code " + httpURLConnection.getResponseCode());

                                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                                    mBufferedInputStream = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                                    String inline;
                                    while ((inline = mBufferedInputStream.readLine()) != null) {
                                        Response += inline;
                                    }
                                    mBufferedInputStream.close();

                                    Log.d("GCM SENT RESPONSE", Response);


                                } else {
                                    Log.d("GCM SENT RESPONSE", "something wrong");

                                }

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            return null;
                        }
                    }.execute();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Spinner spinner1 = (Spinner) parent;
        Spinner spinner2 = (Spinner) parent;
        if (spinner1.getId() == R.id.semester)
            item = parent.getItemAtPosition(position).toString();

        if (spinner2.getId() == R.id.department)
            department = parent.getItemAtPosition(position).toString();


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void attachFile(View view) {

        Intent intent = new Intent(this, FilePickerActivity.class);
        startActivityForResult(intent, 2);
    }

    public void attachImage(View view) {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                selectedfile = data.getData();

//                Log.d(TAG, "onActivityResult: " + selectedfile.toString());
                selectedfilepath = null;
                try {
                    selectedfilepath = getPath(this, selectedfile);
                    display(selectedfilepath);
                    Log.d(TAG, "onActivityResult: " + "abc" + selectedfilepath);
                    type = "image";

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onActivityResult: " + selectedfilepath);
            } else if (requestCode == 2 && resultCode == RESULT_OK) {
                imageView.setImageResource(R.drawable.ic_pdf_box);
                selectedfilepath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                textView.setText(selectedfilepath.substring(selectedfilepath.lastIndexOf("/") + 1));
                Log.d(TAG, "onActivityResult: " + "file" + selectedfilepath.substring(selectedfilepath.lastIndexOf("/") + 1));
                type = "file";

            }
            super.onActivityResult(requestCode, resultCode, data);


        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {

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

    private void display(String selectedfilepath) {


        File imgFile = new File(selectedfilepath);
        String filename = selectedfilepath.substring(selectedfilepath.lastIndexOf("/") + 1);
        Log.d(TAG, "display: " + filename);
        textView.setText(filename);

        if (imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }
    }

    public void onBackPressed() {
        long t = System.currentTimeMillis();
        if (t - backPressedTime > 2000) {
            backPressedTime = t;
            Toast.makeText(this, "Press back again to exit",
                    Toast.LENGTH_SHORT).show();
        } else {    // this guy is serious
            // clean up
            super.onBackPressed();
        }
    }

}
