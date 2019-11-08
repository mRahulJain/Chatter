package com.chatter.chatter.Activities

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.chatter.chatter.Database.AppDatabase
import com.chatter.chatter.Objects_Classes.Message
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
import kotlinx.android.synthetic.main.activity_convo.*
import kotlinx.android.synthetic.main.message_item_1.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ConvoActivity : AppCompatActivity() {

    val roomMessages : ArrayList<Message?> = arrayListOf<Message?>()
    val PICK_IMAGE_REQUEST = 3
    var itr = 0
    var f = false
    var msg = ""
    var roomName : String = ""
    private var mAdapter: RoomAdapter? = null
    lateinit var storageReference : StorageReference
    lateinit var databaseReference : DatabaseReference
    var maxx : Long = 0
    var longPressList : ArrayList<String> =  arrayListOf<String>()
    var longPressedMsgList : ArrayList<Message?> = arrayListOf<Message?>()
    var imageUri : Uri? = null
    var finalImage :ByteArray = ByteArray(1000)
    var flagImage = false
    var date : String = ""
    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "User.db"
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
    lateinit var userID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convo)
        progressMessage.isVisible = true

        roomName = intent.getStringExtra("roomName")
        setSupportActionBar(toolbarConvo)
        supportActionBar?.title = roomName

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val dbUser = db.UserDao().getUser()
        userID = dbUser!!.uid

        storageReference = FirebaseStorage.getInstance()
            .getReference("Files/${roomName}")
        databaseReference = FirebaseDatabase.getInstance()
            .getReference("Chats/${roomName}")

        val reference = FirebaseDatabase.getInstance()
            .getReference("Chats/${roomName}")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for(snap in p0.children) {
                    maxx = snap.key!!.toLong()
                }
            }

        })

        val reff = FirebaseDatabase
            .getInstance()
            .getReference("Chats/${roomName}")
            .orderByKey()
        reff.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("WrongConstant")
            override fun onDataChange(p0: DataSnapshot) {
                roomMessages.clear()
                for(snap in p0.children) {
                    val getMessage = snap.getValue(Message::class.java) ?: break
                    roomMessages.add(getMessage)
                }
                if(roomMessages.size != 0) {
                    init2.isVisible = false
                }
                if(f == false) {
                    if(itr == 0) {
                        date = ""
                        messages.layoutManager = LinearLayoutManager(
                            this@ConvoActivity,
                            LinearLayoutManager.VERTICAL,
                            false)
                        mAdapter = RoomAdapter(this@ConvoActivity, roomMessages, userID)
                        messages.adapter = mAdapter
                        itr = 1
                    } else {
                        date = ""
                        mAdapter!!.notifyDataSetChanged()
                    }
                    messages.smoothScrollToPosition((messages.adapter as RoomAdapter).itemCount)
                } else {
                    date = ""
                    messages.layoutManager = LinearLayoutManager(
                        this@ConvoActivity,
                        LinearLayoutManager.VERTICAL,
                        false)
                    mAdapter = RoomAdapter(this@ConvoActivity, roomMessages, userID)
                    messages.adapter = mAdapter
                    f = false
                }
                progressMessage.isVisible = false
            }

        })

        addFile.setOnClickListener {
            val options = arrayOf (
                "Gallery", "Documents", "Contacts", "Audio"
            )
            val builder = AlertDialog.Builder(this@ConvoActivity)
            builder.setTitle("Select the file")
            builder.setItems(options, object : DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, i: Int) {
                    if(i == 0) {
                        flagImage = true
                        openImageChooser()
                    } else if(i == 1) {

                    } else if(i == 2) {

                    } else if( i == 3) {

                    }
                }

            })
            builder.create()
            builder.show()
        }

        sendMessage.setOnClickListener {
            msg = eTMessage.text.toString()
            if(msg == "") {
                return@setOnClickListener
            }
            eTMessage.setText("")
            val messageRef = FirebaseDatabase.getInstance()
                .getReference("Chats/${roomName}")
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            val date = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val hashMap1 = HashMap<String, String>()
            hashMap1.put("messageNumber", "${maxx+1}")
            hashMap1.put("text", "${msg}")
            hashMap1.put("name", "${dbUser.username}")
            hashMap1.put("uid", "${userID}")
            hashMap1.put("type", "text")
            hashMap1.put("time", "${hour}:${minute}")
            hashMap1.put("date", "${date}/${month}/${year}")
            Log.d("myCHECK", "${hashMap1}")
            messageRef.child("${maxx+1}").setValue(hashMap1).addOnCompleteListener {
                Log.d("myCHECK", "${it.exception}")
            }.addOnFailureListener {
                Log.d("myCHECK", "${it.localizedMessage}")
            }
        }
    }

    private fun getFileExtension(uri : Uri) : String? {
        val cr = getContentResolver()
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri))
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_1, menu)
        return true
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
                    compressedFileImage.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    finalImage = baos.toByteArray()

                    if(imageUri != null) {

                        Toast.makeText(this@ConvoActivity, "Sending...", Toast.LENGTH_LONG).show()
                        val fileRef = storageReference
                            .child("${System.currentTimeMillis()}.${getFileExtension(imageUri!!)}")
                        val uploadtTask = fileRef.putBytes(finalImage)
                        uploadtTask.addOnFailureListener(object : OnFailureListener {
                            override fun onFailure(p0: Exception) {
                                Toast.makeText(this@ConvoActivity, "${p0.localizedMessage.toString()}", Toast.LENGTH_SHORT).show()
                            }


                        }).addOnSuccessListener(object :
                            OnSuccessListener<UploadTask.TaskSnapshot> {
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
                                            val calendar = Calendar.getInstance()
                                            calendar.timeInMillis = System.currentTimeMillis()
                                            val date = calendar.get(Calendar.DAY_OF_MONTH)
                                            val month = calendar.get(Calendar.MONTH)
                                            val year = calendar.get(Calendar.YEAR)
                                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                                            val minute = calendar.get(Calendar.MINUTE)
                                            val hashMap1 = HashMap<String, String>()
                                            hashMap1.put("messageNumber", "${maxx+1}")
                                            hashMap1.put("text", "${p0.result.toString()}")
                                            hashMap1.put("name", "${db.UserDao().getUser()!!.username}")
                                            hashMap1.put("uid", "${userID}")
                                            hashMap1.put("type", "Image")
                                            hashMap1.put("time", "${hour}:${minute}")
                                            hashMap1.put("date", "${date}/${month}/${year}")
                                            databaseReference.child("${maxx+1}").setValue(hashMap1)
                                            Toast.makeText(this@ConvoActivity, "Sent", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                })
                            }
                        })
                    } else {
                        Toast.makeText(this@ConvoActivity, "No file selected", Toast.LENGTH_SHORT).show()
                    }
                } catch (e : IOException) {
                    flagImage = false
                    Toast.makeText(this@ConvoActivity, "${e.localizedMessage.toString()}", Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                flagImage = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        R.id.btnInfo -> {
            val intent = Intent(this@ConvoActivity, GroupInfoAct::class.java)
            intent.putExtra("roomName", "${roomName}")
            startActivity(intent)
            true
        }
        android.R.id.home -> {
            finish()
            true
        }
        R.id.btnStarred -> {
            val intent = Intent(this@ConvoActivity, StarredMessageAct::class.java)
            intent.putExtra("roomName", "${roomName}")
            startActivity(intent)
            true
        }
        R.id.btnExit -> {
            val builder = AlertDialog.Builder(this@ConvoActivity)
            builder.setMessage("Are you sure?")
                .setPositiveButton("Leave"){dialogInterface, which ->
                    val ref = FirebaseDatabase
                        .getInstance()
                        .getReference("InitialChats/${userID}/${roomName}")
                    ref.removeValue()
                    super.onBackPressed()
                }
                .setNegativeButton("No") { dialogInterface, which ->
                    Log.d("myCHECK", "Check3")
                    null
                }
            val alertDialog = builder.create()
            alertDialog.show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    inner class RoomAdapter(val context: Context, var nameList: ArrayList<Message?>, val uid : String) :
        RecyclerView.Adapter<RoomAdapter.NameViewHolder>() {

        var mActionMode : ActionMode? = null
        var flag = false
        var count = 0


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
            val li = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val itemView = li.inflate(R.layout.message_item_1, parent, false)
            return NameViewHolder(itemView)
        }

        private val actionModeCallback = object : ActionMode.Callback {
            // Called when the action mode is created; startActionMode() was called
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                // Inflate a menu resource providing context menu items
                val inflater: MenuInflater = mode.menuInflater
                inflater.inflate(R.menu.menu_2, menu)
                return true
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @SuppressLint("WrongConstant")
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.btnDelete -> {
                        val builder = AlertDialog.Builder(this@ConvoActivity)
                        if(count == 1) {
                            builder.setMessage("Delete message?")
                                .setPositiveButton("Delete"){dialogInterface, which ->
                                    f = true
                                    val ref = FirebaseDatabase.getInstance().getReference("Chats/${roomName}")
                                    for(msg in longPressedMsgList) {
                                        if(msg!!.uid == uid) {
                                            ref.child("${msg!!.messageNumber}").removeValue()
                                        }
                                    }
                                    val reff = FirebaseDatabase
                                        .getInstance()
                                        .getReference("Starred/${roomName}/${userID}/starredMessages")
                                    for(msg in longPressedMsgList) {
                                        if(msg!!.uid == userID) {
                                            reff.child("${msg!!.messageNumber}").removeValue()
                                        }
                                    }
                                    flag = false
                                    mActionMode!!.finish()
                                }
                                .setNegativeButton("Cancel") { dialogInterface, which ->
                                    null
                                }
                        } else {
                            builder.setMessage("Delete ${count} messages?")
                                .setPositiveButton("Delete"){dialogInterface, which ->
                                    f = true
                                    val ref = FirebaseDatabase.getInstance().getReference("Chats/${roomName}")
                                    for(msg in longPressedMsgList) {
                                        if(msg!!.uid == userID) {
                                            ref.child("${msg!!.messageNumber}").removeValue()
                                        }
                                    }
                                    val reff = FirebaseDatabase
                                        .getInstance()
                                        .getReference("Starred/${roomName}/${userID}/starredMessages")
                                    for(msg in longPressedMsgList) {
                                        if(msg!!.uid == userID) {
                                            reff.child("${msg!!.messageNumber}").removeValue()
                                        }
                                    }
                                    flag = false
                                    mActionMode!!.finish()
                                }
                                .setNegativeButton("Cancel") { dialogInterface, which ->
                                    null
                                }
                        }
                        val alertDialog = builder.create()
                        alertDialog.show()
                        true
                    }
                    R.id.btnStar -> {
                        val ref = FirebaseDatabase
                            .getInstance()
                            .getReference("Starred/${roomName}/${userID}/starredMessages")
                        for(getMessageStarred in longPressedMsgList) {
                            val hashMap1 = HashMap<String, String>()
                            hashMap1.put("messageNumber", "${getMessageStarred!!.messageNumber}")
                            hashMap1.put("text", "${getMessageStarred!!.text}")
                            hashMap1.put("name", "${getMessageStarred!!.name}")
                            hashMap1.put("uid", "${getMessageStarred!!.uid}")
                            ref.child("${getMessageStarred!!.messageNumber}").setValue(hashMap1)
                        }
                        Toast.makeText(this@ConvoActivity, "Starred", Toast.LENGTH_SHORT).show()
                        flag = false
                        mActionMode!!.finish()
                        true
                    }
                    R.id.btnCopy -> {
                        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        var str = ""
                        for(s in longPressedMsgList) {
                            str = "${str}${s!!.name}: ${s!!.text}\n"
                        }
                        val clip = ClipData.newPlainText("EditText", str)
                        clipboardManager.setPrimaryClip(clip)

                        Toast.makeText(this@ConvoActivity, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        flag = false
                        mActionMode!!.finish()
                        true
                    }
                    else -> false
                }
            }

            // Called when the user exits the action mode
            @SuppressLint("WrongConstant")
            override fun onDestroyActionMode(mode: ActionMode) {
                messages.layoutManager = LinearLayoutManager(
                    this@ConvoActivity,
                    LinearLayoutManager.VERTICAL,
                    false)
                messages.adapter = RoomAdapter(this@ConvoActivity, roomMessages, userID)
                messages.smoothScrollToPosition((messages.adapter as RoomAdapter).itemCount)
                flag = false
                mActionMode!!.finish()
            }
        }

        override fun getItemCount(): Int {
            if(nameList == null) {
                return 0
            }
            return nameList.size
        }

        override fun onBindViewHolder(holder: RoomAdapter.NameViewHolder, position: Int) {
            if(nameList == null) {
                return
            }

            if(nameList[position]!!.type == "text") {
                holder.itemView.tViewMessage.text = nameList[position]!!.text
                holder.itemView.imageChat.isVisible = false
                holder.itemView.tViewMessage.isVisible = true
                holder.itemView.tViewMessage.text = nameList[position]!!.text
            } else if(nameList[position]!!.type == "Image") {
                holder.itemView.tViewMessage.isVisible = false
                holder.itemView.imageChat.isVisible = true
                Picasso.with(context)
                    .load(nameList[position]!!.text)
                    .fit()
                    .into(holder.itemView.imageChat)
            }
            holder.itemView.time.text = nameList[position]!!.time
            if(nameList[position]!!.uid == userID) {
                holder.itemView.tViewSender.text = "You"
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.gravity = Gravity.RIGHT
                params.setMargins(10,0,10,10)
                holder.itemView.parentL.setLayoutParams(params)
                holder.itemView.parentL.setBackgroundResource(R.drawable.message_background_1)
            } else {
                holder.itemView.tViewSender.text = nameList[position]!!.name
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.gravity = Gravity.LEFT
                params.setMargins(10,0,10,10)
                holder.itemView.parentL.setLayoutParams(params)
                holder.itemView.parentL.setBackgroundResource(R.drawable.message_background)
            }
            holder.itemView.imageChat.setOnClickListener {
                val intent = Intent(context, ImageActivity::class.java)
                intent.putExtra("purpose", "${nameList[position]!!.name}")
                intent.putExtra("url", "${nameList[position]!!.text}")
                startActivity(intent)
            }
            holder.itemView.prnt.setOnClickListener {
                if(flag == false) {
                    return@setOnClickListener
                }
                if(nameList[position]!!.uid == uid) {
                    if(nameList[position]!!.toggle == 1) {
                        holder.itemView.parentL.setBackgroundResource(R.drawable.message_background_1)
                        longPressList.remove(nameList[position]!!.messageNumber)
                        longPressedMsgList.remove(nameList[position])
                        nameList[position]!!.toggle = 0
                        count--
                    } else {
                        nameList[position]!!.toggle = 1
                        holder.itemView.parentL.setBackgroundResource(R.drawable.message_background_2)
                        longPressList.add(nameList[position]!!.messageNumber)
                        longPressedMsgList.add(nameList[position])
                        count++
                    }
                } else {
                    if(nameList[position]!!.toggle == 1) {
                        nameList[position]!!.toggle = 0
                        count--
                        longPressList.remove(nameList[position]!!.messageNumber)
                        longPressedMsgList.remove(nameList[position])
                        holder.itemView.parentL.setBackgroundResource(R.drawable.message_background)
                    } else {
                        nameList[position]!!.toggle = 1
                        count++
                        longPressList.add(nameList[position]!!.messageNumber)
                        longPressedMsgList.add(nameList[position])
                        holder.itemView.parentL.setBackgroundResource(R.drawable.message_background_3)
                    }
                }
                if(count == 0) {
                    flag = false
                    mActionMode!!.finish()
                }
                Log.d("myCHECK", "${longPressList}")
            }

            holder.itemView.prnt.setOnLongClickListener(object : View.OnLongClickListener{
                override fun onLongClick(p0: View?): Boolean {
                    longPressList.clear()
                    longPressedMsgList.clear()
                    flag = true
                    if(mActionMode != null) {
                        return false
                    }

                    mActionMode = this@ConvoActivity?.startActionMode(actionModeCallback)
                    if(nameList[position]!!.uid == uid) {
                        holder.itemView.parentL.setBackgroundResource(R.drawable.message_background_2)
                    } else {
                        holder.itemView.parentL.setBackgroundResource(R.drawable.message_background_3)
                    }
                    longPressList.add(nameList[position]!!.messageNumber)
                    longPressedMsgList.add(nameList[position])
                    Log.d("myCHECK", "${longPressList}")
                    nameList[position]!!.toggle = 1
                    count = 1
                    return true
                }
            })
        }
        inner class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}