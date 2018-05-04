package com.github.viktorpenelski.securebutton

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.ssh_button_row.view.*

class SSHButtonsListAdapter(private val context: Context?,
                            var buttons: MutableList<SSHButton>,
                            private val onExecute: OnExecuteClickedListener): BaseAdapter(){

    private class ViewHolder(row: View) {
        var name: TextView = row.button_name
        var shortInfo: TextView = row.button_short_info
        var lastExecuted: TextView = row.button_last_executed
        var buttonExecute: Button = row.button_execute
    }

    override fun getCount(): Int {
        return buttons.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): SSHButton {
        return buttons[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View?
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.ssh_button_row, parent, false)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        buttons[position].apply {
            viewHolder.name.text = name
            viewHolder.lastExecuted.text = lastUsed?.toString() ?: "never used before"
            viewHolder.shortInfo.text = "$username@$host"
            viewHolder.buttonExecute.text = "Execute"
            viewHolder.buttonExecute.setOnClickListener {
                onExecute.executeClicked(this)
            }
        }

        return view as View

    }

    interface OnExecuteClickedListener {
        fun executeClicked(button: SSHButton)
    }
}