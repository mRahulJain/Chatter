package com.chatter.chatter.Activities

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_starred_message.*
import kotlinx.android.synthetic.main.message_item_2.view.*

class StarredMessageAct : AppCompatActivity() {

    var starredMessages : ArrayList<Message?> = arrayListOf()
    var longPressList : ArrayList<String> =  arrayListOf<String>()
    var longPressedMsgList : ArrayList<Message?> = arrayListOf()
    var roomName : String = ""
    val db: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "User.db"
        ).allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starred_message)

        setSupportActionBar(toolbarStarredMessage)
        supportActionBar?.title = "Starred messages"
        progressStarred.isVisible = true

        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        roomName = intent.getStringExtra("roomName")

        val dbUser = db.UserDao().getUser()

        val ref = FirebaseDatabase.getInstance()
            .getReference("Starred/${roomName}/${dbUser!!.uid}/starredMessages")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            @SuppressLint("WrongConstant")
            override fun onDataChange(p0: DataSnapshot) {
                starredMessages.clear()
                for(snap in p0.children) {
                    val getMessage = snap.getValue(Message::class.java) ?: break
                    starredMessages.add(getMessage)
                }

                noStarred.isVisible = starredMessages.size == 0

                progressStarred.isVisible = false
                starred.layoutManager = LinearLayoutManager(
                    this@StarredMessageAct,
                    LinearLayoutManager.VERTICAL,
                    false)
                starred.adapter = RAdapter(
                    this@StarredMessageAct,
                    starredMessages,
                    dbUser!!.uid
                )
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when(item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    inner class RAdapter(val context: Context, val nameList: ArrayList<Message?>, val uid : String) : RecyclerView.Adapter<RAdapter.NameViewHolder>() {

        var mActionMode : ActionMode? = null
        var flag = false
        var count = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
            val li = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val itemView = li.inflate(R.layout.message_item_2, parent, false)
            return NameViewHolder(itemView)
        }

        private val actionModeCallback = object : ActionMode.Callback {
            // Called when the action mode is created; startActionMode() was called
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                // Inflate a menu resource providing context menu items
                val inflater: MenuInflater = mode.menuInflater
                inflater.inflate(R.menu.menu_3, menu)
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
                    R.id.btnRemove -> {
                        val builder = AlertDialog.Builder(this@StarredMessageAct)
                        if(count == 1) {
                            builder.setMessage("Delete starred message?")
                                .setPositiveButton("Delete"){dialogInterface, which ->
                                    val reff = FirebaseDatabase
                                        .getInstance()
                                        .getReference("Starred/${roomName}/${uid}/starredMessages")
                                    for(str in longPressList) {
                                        reff.child("${str}").removeValue()
                                    }
                                    flag = false
                                    mActionMode!!.finish()
                                }
                                .setNegativeButton("Cancel") { dialogInterface, which ->
                                    null
                                }
                        } else {
                            builder.setMessage("Delete ${count} starred messages?")
                                .setPositiveButton("Delete"){dialogInterface, which ->
                                    val reff = FirebaseDatabase
                                        .getInstance()
                                        .getReference("Starred/${roomName}/${uid}/starredMessages")
                                    for(str in longPressList) {
                                        reff.child("${str}").removeValue()
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
                    R.id.btnCopyS -> {
                        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        var str = ""
                        for(s in longPressedMsgList) {
                            str = "${str}${s!!.name}: ${s!!.text}\n"
                        }
                        val clip = ClipData.newPlainText("EditText", str)
                        clipboardManager.setPrimaryClip(clip)

                        Toast.makeText(this@StarredMessageAct, "Copied to clipboard", Toast.LENGTH_SHORT).show()
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
                starred.layoutManager = LinearLayoutManager(
                    this@StarredMessageAct,
                    LinearLayoutManager.VERTICAL,
                    false)
                starred.adapter = RAdapter(this@StarredMessageAct, starredMessages, uid)
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

        override fun onBindViewHolder(holder: RAdapter.NameViewHolder, position: Int) {
            if(nameList == null) {
                return
            }
            holder.itemView.tViewMessageS.text = nameList[position]!!.text
            if(nameList[position]!!.uid == uid) {
                holder.itemView.tViewSenderS.text = "You"
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.gravity = Gravity.RIGHT
                params.setMargins(10,0,10,10)
                holder.itemView.parentLS.setLayoutParams(params)
                holder.itemView.parentLS.setBackgroundResource(R.drawable.message_background_1)
            } else {
                holder.itemView.tViewSenderS.text = nameList[position]!!.name
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.gravity = Gravity.LEFT
                params.setMargins(10,0,10,10)
                holder.itemView.parentLS.setLayoutParams(params)
                holder.itemView.parentLS.setBackgroundResource(R.drawable.message_background)
            }

            holder.itemView.prntS.setOnClickListener {
                if(flag == false) {
                    return@setOnClickListener
                }
                if(nameList[position]!!.uid == uid) {
                    if(nameList[position]!!.toggle == 1) {
                        holder.itemView.parentLS.setBackgroundResource(R.drawable.message_background_1)
                        longPressList.remove(nameList[position]!!.messageNumber)
                        longPressedMsgList.remove(nameList[position])
                        nameList[position]!!.toggle = 0
                        count--
                    } else {
                        nameList[position]!!.toggle = 1
                        holder.itemView.parentLS.setBackgroundResource(R.drawable.message_background_2)
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
                        holder.itemView.parentLS.setBackgroundResource(R.drawable.message_background)
                    } else {
                        nameList[position]!!.toggle = 1
                        count++
                        longPressList.add(nameList[position]!!.messageNumber)
                        longPressedMsgList.add(nameList[position])
                        holder.itemView.parentLS.setBackgroundResource(R.drawable.message_background_3)
                    }
                }
                if(count == 0) {
                    flag = false
                    mActionMode!!.finish()
                }
            }

            holder.itemView.prntS.setOnLongClickListener(object : View.OnLongClickListener{
                override fun onLongClick(p0: View?): Boolean {
                    longPressList.clear()
                    longPressedMsgList.clear()
                    flag = true
                    if(mActionMode != null) {
                        return false
                    }

                    mActionMode = this@StarredMessageAct?.startActionMode(actionModeCallback)
                    if(nameList[position]!!.uid == uid) {
                        holder.itemView.parentLS.setBackgroundResource(R.drawable.message_background_2)
                    } else {
                        holder.itemView.parentLS.setBackgroundResource(R.drawable.message_background_3)
                    }
                    longPressList.add(nameList[position]!!.messageNumber)
                    longPressedMsgList.add(nameList[position])
                    nameList[position]!!.toggle = 1
                    count = 1
                    return true
                }

            })
        }


        inner class NameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}
