package com.chatter.chatter.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.room.Room
import com.chatter.chatter.Database.AppDatabase
import com.chatter.chatter.Database.User
import com.chatter.chatter.R
import com.google.android.gms.tasks.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_create_account_details.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception

class CreateAccountDetailsAct : AppCompatActivity() {

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "User.db"
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    val PICK_IMAGE_REQUEST = 1
    lateinit var storageReference : StorageReference
    var imageUri : Uri? = null
    var finalImage :ByteArray = ByteArray(1000)
    var flagImage = false
    var imageURLS : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account_details)

        val password = intent.getStringExtra("password")
        password1.setText(password)
        password2.setText(password)

        storageReference = FirebaseStorage.getInstance()
            .getReference("UserProfileImages/${FirebaseAuth.getInstance().currentUser!!.uid}")

        register.setOnClickListener {
            if(fullName.text.toString() == "" ||
                        username.text.toString() == "" ||
                        password1.text.toString() == "" ||
                        password2.text.toString() == ""
            ) {
                Toast.makeText(this,
                    "Fill all the * fields",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(password1.text.toString() != password2.text.toString()) {
                Toast.makeText(this,
                    "Password do not match",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val fullName = fullName.text.toString()
            val username = username.text.toString()
            val password = password1.text.toString()
            var dob : String = dob.text.toString()
            var genderId = gender.checkedRadioButtonId
            var gender = ""
            if(genderId != -1) {
                val radioButton = findViewById(genderId) as RadioButton
                gender = radioButton.text.toString()
            }
            val uid : String = FirebaseAuth.getInstance().currentUser!!.uid

            if(flagImage == true) {
                if(imageUri != null) {
                    val fileRef = storageReference
                        .child("profileImg.${getFileExtension(imageUri!!)}")
                    val uploadtTask = fileRef.putBytes(finalImage)
                    uploadtTask.addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(p0: Exception) {
                            Toast.makeText(this@CreateAccountDetailsAct, "${p0.localizedMessage.toString()}", Toast.LENGTH_SHORT).show()
                        }


                    }).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                        override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                            val uriTask = uploadtTask.continueWithTask(object :
                                Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                                override fun then(p0: Task<UploadTask.TaskSnapshot>): Task<Uri> {
                                    if(!p0.isSuccessful) {
                                        throw p0.exception!!
                                    }
                                    return fileRef.getDownloadUrl()
                                }

                            }).addOnCompleteListener(object : OnCompleteListener<Uri> {
                                override fun onComplete(p0: Task<Uri>) {
                                    if(p0.isSuccessful){
                                        imageURLS = p0.result.toString()
                                        val ref = FirebaseDatabase.getInstance()
                                            .getReference("Profiles/${uid}")
                                        val hashMap = HashMap<String, String>()
                                        hashMap.put("fullName", "${fullName}")
                                        hashMap.put("username", "${username}")
                                        hashMap.put("dob", "${dob}")
                                        hashMap.put("password", "${password}")
                                        hashMap.put("gender", "${gender}")
                                        hashMap.put("imageURL", "${imageURLS}")
                                        hashMap.put("uid", "${uid}")
                                        ref.setValue(hashMap)
                                    }
                                }

                            })
                        }
                    })
                }
            } else {
                val ref = FirebaseDatabase.getInstance()
                    .getReference("Profiles/${uid}")
                val hashMap = HashMap<String, String>()
                hashMap.put("fullName", "${fullName}")
                hashMap.put("username", "${username}")
                hashMap.put("dob", "${dob}")
                hashMap.put("password", "${password}")
                hashMap.put("gender", "${gender}")
                hashMap.put("imageURL", "")
                hashMap.put("uid", "${uid}")
                ref.setValue(hashMap)
            }

            val user = User(
                fullName = fullName,
                username = username,
                dob = dob,
                password = password,
                gender = gender,
                uid = uid
            )

            val getUserDB = db.UserDao().getUser()
            if(getUserDB == null) {
                db.UserDao().insertRow(user)
            } else {
                if(getUserDB!!.uid != FirebaseAuth.getInstance().currentUser!!.uid) {
                    db.UserDao().deleteUser()
                    db.UserDao().insertRow(user)
                }
            }

            val intent = Intent(this, ChatsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()

        }

        upload.setOnClickListener {
            flagImage = true
            openFileChooser()
        }
    }


    private fun getFileExtension(uri : Uri) : String? {
        val cr = this.getContentResolver()
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri))
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.getData() != null) {
            flagImage = true
            imageUri = data.getData()
            val intent = CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .getIntent(this)
            startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)

        }else {
            flagImage = false
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            flagImage = true
            val result = CropImage.getActivityResult(data)
            if (resultCode == AppCompatActivity.RESULT_OK) {
                flagImage = true
                val resultUri = result.getUri()
                val file = File(resultUri.path.toString())

                try {
                    val compressedFileImage = Compressor(this)
                        .setMaxHeight(300)
                        .setMaxWidth(300)
                        .setQuality(75)
                        .compressToBitmap(file)

                    val baos = ByteArrayOutputStream()
                    compressedFileImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    finalImage = baos.toByteArray()
                    userImg.setImageURI(imageUri)
                    userImg.setPadding(0)

                } catch (e : IOException) {
                    flagImage = false
                    Toast.makeText(this, "${e.localizedMessage.toString()}", Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                flagImage = false
            }
        }
    }
}
