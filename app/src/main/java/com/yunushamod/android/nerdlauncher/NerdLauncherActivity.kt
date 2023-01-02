package com.yunushamod.android.nerdlauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NerdLauncherActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        setUpAdapter()
    }

    private fun setUpAdapter(){
        val startupIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val activities = packageManager.queryIntentActivities(startupIntent, 0)
        activities.sortWith(Comparator{
            a, b  ->
                String.CASE_INSENSITIVE_ORDER.compare(a.loadLabel(packageManager).toString(),
                    b.loadLabel(packageManager).toString())
        })
        Log.i(TAG, "Found ${activities.size} launcher activities")
        recyclerView.adapter = ActivityAdapter(activities.filter {
            it.activityInfo.packageName != packageName
        })
    }

    private class ActivityViewHolder(private val view: View): RecyclerView.ViewHolder(view), View.OnClickListener{
        private val nameView = itemView.findViewById<TextView>(R.id.activity_name)
        private val imageView = itemView.findViewById<ImageView>(R.id.activityIcon)
        private lateinit var resolveInfo: ResolveInfo
        init {
            itemView.setOnClickListener(this)
        }
        fun bindActivity(resolveInfo: ResolveInfo){
            this.resolveInfo = resolveInfo
            val packageManager = itemView.context.packageManager
            val appName = resolveInfo.loadLabel(packageManager).toString()
            nameView.text = appName
            try {
                val drawable = resolveInfo.loadIcon(packageManager);
                imageView.setImageDrawable(drawable)
            }
            catch (e: Exception)
            {
                imageView.setImageDrawable(null)
            }
        }

        override fun onClick(p0: View?) {
            val activityInfo = resolveInfo.activityInfo
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName(activityInfo.applicationInfo.packageName,
                activityInfo.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val context = view.context
            context.startActivity(intent)
        }
    }

    private class ActivityAdapter(val resolveInfo: List<ResolveInfo>) : RecyclerView.Adapter<ActivityViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_activity, parent, false)
            return ActivityViewHolder(view)
        }

        override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
            val resolvedInfo = resolveInfo[position]
            holder.bindActivity(resolvedInfo)
        }

        override fun getItemCount(): Int = resolveInfo.size

    }
    companion object{
        private const val TAG = "NerdLauncherActivity"
    }
}