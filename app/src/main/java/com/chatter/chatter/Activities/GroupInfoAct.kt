package com.chatter.chatter.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.GridLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.chatter.chatter.Adapters.InfoAdapter
import com.chatter.chatter.Adapters.LoadingInfoAdapter
import com.chatter.chatter.Objects_Classes.Profiles
import com.chatter.chatter.Objects_Classes.Rooms
import com.chatter.chatter.R
import com.google.android.gms.tasks.*
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_group_info.*

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception

class GroupInfoAct : AppCompatActivity() {

    var users : ArrayList<Profiles?> = arrayListOf()
    val usersUID : ArrayList<String> = arrayListOf()
    val PICK_IMAGE_REQUEST = 2
    var imageUri : Uri? = null
    lateinit var storageReference : StorageReference
    lateinit var databaseReference : DatabaseReference
    var finalImage :ByteArray = ByteArray(1000)
    var flagImage = false
    var image = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)

        val roomName = intent.getStringExtra("roomName")
        progressGroupInfo.isVisible = true

        groupMembersG.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL,
            false)
        groupMembersG.adapter = LoadingInfoAdapter(this)

        setSupportActionBar(toolbarGroupInfo)
        supportActionBar?.title = roomName

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        storageReference = FirebaseStorage.getInstance()
            .getReference("roomImgUpload/${roomName}")
        databaseReference = FirebaseDatabase.getInstance()
            .getReference("RoomInfo/${roomName}/roomImg")

        val ref = FirebaseDatabase.getInstance()
            .getReference("RoomInfo/${roomName}")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    val getInfo = p0.getValue(Rooms::class.java)
                    groupNameG.setText(getInfo!!.roomName)
                    groupPasskeyG.setText(getInfo!!.roomCode)
                    if(getInfo!!.roomImg != "") {
                        Picasso.with(this@GroupInfoAct)
                            .load(getInfo!!.roomImg)
                            .into(roomPhoto)
                    }
                    groupAdminG.text = "Group made by "+getInfo!!.roomAdmin
                    progressGroupInfo.isVisible = false
                }
            }

        })

        val reff = FirebaseDatabase.getInstance()
            .getReference("InitialChats")
        reff.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    for(snap in p0.children) {
                        for(snapp in snap.children) {
                            if(snapp.key.toString() == roomName) {
                                usersUID.add(snap.key.toString())
                                break
                            }
                        }
                    }

                    val reference = FirebaseDatabase.getInstance()
                        .getReference("Profiles")
                    reference.addValueEventListener(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(p0.exists()) {
                                for(snap in p0.children) {
                                    if(usersUID.contains(snap.key.toString())) {
                                        users.add(snap.getValue(Profiles::class.java))
                                    }
                                }
                                groupMembersG.layoutManager = GridLayoutManager(this@GroupInfoAct, 2, GridLayoutManager.VERTICAL,
                                    false)
                                groupMembersG.adapter = InfoAdapter(this@GroupInfoAct, users)
                            }
                        }

                    })
                }
            }

        })

        changeProfilePicture.setOnClickListener {
            openFileChooser()
        }

        save.setOnClickListener {
            Toast.makeText(this,
                "Please wait",
                Toast.LENGTH_LONG).show()
            uploadFile()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun getFileExtension(uri : Uri) : String? {
        val cr = getContentResolver()
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri))
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun uploadFile() {
        if(flagImage == true) {
            roomPhoto.setImageURI(imageUri)
            if(imageUri != null) {
                val fileRef = storageReference
                    .child("profileImg.${getFileExtension(imageUri!!)}")
                val uploadtTask = fileRef.putBytes(finalImage)
                uploadtTask.addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(p0: Exception) {
                        Toast.makeText(this@GroupInfoAct, "${p0.localizedMessage.toString()}", Toast.LENGTH_SHORT).show()
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
                                    Log.d("myCHECK", "${p0.result.toString()}")
                                    databaseReference.setValue("${p0.result.toString()}")
                                    Toast.makeText(this@GroupInfoAct, "Room updated", Toast.LENGTH_SHORT).show()
                                }
                            }

                        })
                    }
                })
            } else {
                Toast.makeText(this@GroupInfoAct, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            flagImage = true
            imageUri = data.getData()
            CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)

        } else {
            flagImage = false
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            flagImage = true
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
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
                    roomPhoto.setImageURI(imageUri)
                    save.isVisible = true
                } catch (e : IOException) {
                    flagImage = false
                    Toast.makeText(this@GroupInfoAct, "${e.localizedMessage.toString()}", Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                flagImage = false
            }
        }
    }
}
