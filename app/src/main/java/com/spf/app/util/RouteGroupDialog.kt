package com.spf.app.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.spf.app.R

class RouteGroupDialog : DialogFragment() {

    private val TAG = "RouteGroupDialog"
    private lateinit var groupTitle: TextInputLayout
    internal lateinit var listener: RGDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface RGDialogListener {
        fun onSave(groupTitle: String)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as RGDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(("${context.toString()} must implement RGDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_route_group, null)
        groupTitle = view.findViewById(R.id.group_title)
        return dialogBuilder.setView(view)
//            .setTitle(resources.getString())
            .setNeutralButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Continue") { dialog, _ ->
                val title = groupTitle.editText?.text.toString()
                listener.onSave(title)
                dialog.dismiss()
            }.setNegativeButton("Skip") { dialog, _ ->
                val title = groupTitle.editText?.text.toString()
                listener.onSave(title)
                dialog.dismiss()
            }.create()

    }
}