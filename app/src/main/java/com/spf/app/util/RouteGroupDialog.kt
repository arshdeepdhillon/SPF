package com.spf.app.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.spf.app.R

interface RGDialogListener {
    /**
     * Broadcast the group title when [MaterialAlertDialogBuilder.setPositiveButton] or
     * [MaterialAlertDialogBuilder.setNegativeButton] is clicked. */
    fun onPositiveButton(groupTitle: String)
}

/**
 * NOTE: [RGDialogListener] must be implemented to receive event callbacks.
 */
class RouteGroupDialog : DialogFragment() {
    private lateinit var groupTitle: TextInputLayout
    private lateinit var listener: RGDialogListener

    // Override the Fragment.onAttach() method to instantiate the RGDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the RGDialogListener so we can send events to the host
            listener = context as RGDialogListener
        } catch (e: ClassCastException) {
            // If the activity doesn't implement the interface, throw exception
            throw ClassCastException(("$context must implement RGDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_route_group, null)
        groupTitle = view.findViewById(R.id.group_title)
        return dialogBuilder.setView(view)
//            .setTitle(resources.getString())
            .setNeutralButton(getString(R.string.dialog_route_group_cancel)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.dialog_route_group_continue)) { dialog, _ ->
                val title = groupTitle.editText?.text.toString()
                listener.onPositiveButton(title)
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.dialog_route_group_skip)) { dialog, _ ->
                val title = groupTitle.editText?.text.toString()
                listener.onPositiveButton(title)
                dialog.dismiss()
            }.create()
    }
}