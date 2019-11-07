package com.chatter.chatter.Fragments


import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.chatter.chatter.Activities.ImageActivity
import com.chatter.chatter.Database.AppDatabase
import com.chatter.chatter.Objects_Classes.Profiles
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
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception

class ProfileFrag : Fragment() {

    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "User.db"
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
    val PICK_IMAGE_REQUEST = 2
    var imageUri : Uri? = null
    lateinit var storageReference : StorageReference
    lateinit var databaseReference : DatabaseReference
    var finalImage :ByteArray = ByteArray(1000)
    var flagImage = false
    var imageURLS : String = ""
    var beforeImageURL : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        view!!.shimmer.startShimmerAnimation()
        val dbUser = db.UserDao().getUser()

        storageReference = FirebaseStorage.getInstance()
            .getReference("ProfileImgs/${dbUser!!.uid}")
        databaseReference = FirebaseDatabase.getInstance()
            .getReference("Profiles/${dbUser!!.uid}/imageURL")

        val ref = FirebaseDatabase.getInstance()
            .getReference("Profiles/${dbUser!!.uid}")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    val getUser = p0.getValue(Profiles::class.java)
                    beforeImageURL = getUser!!.imageURL
                    if(getUser!!.imageURL != "") {
                        Picasso.with(view!!.context)
                            .load(getUser!!.imageURL)
                            .into(view!!.profilePicture)
                    }
                    view!!.name2.setText(getUser!!.fullName)
                    view!!.bio.setText(getUser!!.bio)
                    view!!.username.setText(getUser!!.username)
                    view!!.password.setText(getUser!!.password)
                    view!!.prq.setText(getUser!!.prq)
                    view!!.prqA.setText(getUser!!.prqA)
                    view!!.gender1.setText(getUser!!.gender)
                    view!!.dob1.setText(getUser!!.dob)
                    view!!.shimmer.stopShimmerAnimation()

                    view!!.profilePicture.setOnClickListener {
                        if(getUser!!.imageURL == "") {
                            return@setOnClickListener
                        }
                        val intent = Intent(context, ImageActivity::class.java)
                        intent.putExtra("purpose", "Profile Picture")
                        intent.putExtra("url", "${getUser!!.imageURL}")
                        startActivity(intent)
                    }
                } else {
                    view.shimmer.stopShimmerAnimation()
                }
            }

        })

        view.changeProfilePicture1.setOnClickListener {
            flagImage = true
            openFileChooser()
        }

        view!!.btnSave.setOnClickListener {
            val ref = FirebaseDatabase.getInstance()
                .getReference("Profiles/${dbUser!!.uid}")
            val hashMap = HashMap<String, String>()
            hashMap.put("fullName", "${view!!.name2.text}")
            hashMap.put("username", "${view!!.username.text}")
            hashMap.put("dob", "${view!!.dob1.text}")
            hashMap.put("password", "${view!!.password.text}")
            hashMap.put("gender", "${view!!.gender1.text}")
            hashMap.put("imageURL", "${beforeImageURL}")
            hashMap.put("uid", "${dbUser!!.uid}")
            hashMap.put("prq", "${view!!.prq.text}")
            hashMap.put("prqA", "${view!!.prqA.text}")
            hashMap.put("bio", "${view!!.bio.text}")
            ref.setValue(hashMap)
            Toast.makeText(view!!.context,
                "Successful",
                Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun getFileExtension(uri : Uri) : String? {
        val cr = context!!.getContentResolver()
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
                .getIntent(context!!)
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
                    val compressedFileImage = Compressor(context!!)
                        .setMaxHeight(300)
                        .setMaxWidth(300)
                        .setQuality(75)
                        .compressToBitmap(file)

                    val baos = ByteArrayOutputStream()
                    compressedFileImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    finalImage = baos.toByteArray()

                    if(flagImage == true) {
                        if(imageUri != null) {
                            Toast.makeText(view!!.context, "Please wait...", Toast.LENGTH_LONG).show()
                            val fileRef = storageReference
                                .child("profileImg.${getFileExtension(imageUri!!)}")
                            val uploadtTask = fileRef.putBytes(finalImage)
                            uploadtTask.addOnFailureListener(object : OnFailureListener {
                                override fun onFailure(p0: Exception) {
                                    Toast.makeText(view!!.context, "${p0.localizedMessage}", Toast.LENGTH_LONG).show()
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
                                                databaseReference.setValue(imageURLS)
                                                Toast.makeText(view!!.context, "Profile photo updated", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                    })
                                }
                            })
                        }
                    }
                } catch (e : IOException) {
                    flagImage = false
                    Toast.makeText(view!!.context, "${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                flagImage = false
            }
        }
    }

}
