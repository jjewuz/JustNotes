package com.jjewuz.justnotes.Utils

import android.content.SharedPreferences
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.TextView
import java.util.LinkedList


class TextHelper( private val mTextView: TextView) {

    private var mIsUndoOrRedo = false

    private val mEditHistory: EditHistory

    private val mChangeListener: EditTextChangeListener
    init {
        mEditHistory = EditHistory()
        mChangeListener = EditTextChangeListener()
        mTextView.addTextChangedListener(mChangeListener)
    }

    fun disconnect() {
        mTextView.removeTextChangedListener(mChangeListener)
    }


    fun setMaxHistorySize(maxHistorySize: Int) {
        mEditHistory.setMaxHistorySize(maxHistorySize)
    }

    fun clearHistory() {
        mEditHistory.clear()
    }


    val canUndo: Boolean
        get() = mEditHistory.mmPosition > 0


    fun undo() {
        val edit: EditItem = mEditHistory.previous ?: return
        val text = mTextView.editableText
        val start = edit.mmStart
        val end = start + if (edit.mmAfter != null) edit.mmAfter.length else 0
        mIsUndoOrRedo = true
        text.replace(start, end, edit.mmBefore)
        mIsUndoOrRedo = false


        for (o in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(o)
        }
        Selection.setSelection(
            text,
            if (edit.mmBefore == null) start else start + edit.mmBefore.length
        )
    }


    val canRedo: Boolean
        get() = mEditHistory.mmPosition < mEditHistory.mmHistory.size

    fun redo() {
        val edit: EditItem = mEditHistory.next ?: return
        val text = mTextView.editableText
        val start = edit.mmStart
        val end = start + if (edit.mmBefore != null) edit.mmBefore.length else 0
        mIsUndoOrRedo = true
        text.replace(start, end, edit.mmAfter)
        mIsUndoOrRedo = false

        for (o in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(o)
        }
        Selection.setSelection(
            text,
            if (edit.mmAfter == null) start else start + edit.mmAfter.length
        )
    }


    fun storePersistentState(editor: SharedPreferences.Editor, prefix: String) {
        editor.putString("$prefix.hash", mTextView.text.toString().hashCode().toString())
        editor.putInt("$prefix.maxSize", mEditHistory.mmMaxHistorySize)
        editor.putInt("$prefix.position", mEditHistory.mmPosition)
        editor.putInt("$prefix.size", mEditHistory.mmHistory.size)
        for ((i, ei) in mEditHistory.mmHistory.withIndex()) {
            val pre = "$prefix.$i"
            editor.putInt("$pre.start", ei.mmStart)
            editor.putString("$pre.before", ei.mmBefore.toString())
            editor.putString("$pre.after", ei.mmAfter.toString())
        }
    }

    @Throws(IllegalStateException::class)
    fun restorePersistentState(sp: SharedPreferences, prefix: String): Boolean {
        val ok = doRestorePersistentState(sp, prefix)
        if (!ok) {
            mEditHistory.clear()
        }
        return ok
    }

    private fun doRestorePersistentState(sp: SharedPreferences, prefix: String): Boolean {
        val hash = sp.getString("$prefix.hash", null)
            ?: // No state to be restored.
            return true
        if (Integer.valueOf(hash) != mTextView.text.toString().hashCode()) {
            return false
        }
        mEditHistory.clear()
        mEditHistory.mmMaxHistorySize = sp.getInt("$prefix.maxSize", -1)
        val count = sp.getInt("$prefix.size", -1)
        if (count == -1) {
            return false
        }
        for (i in 0 until count) {
            val pre = "$prefix.$i"
            val start = sp.getInt("$pre.start", -1)
            val before = sp.getString("$pre.before", null)
            val after = sp.getString("$pre.after", null)
            if (start == -1 || before == null || after == null) {
                return false
            }
            mEditHistory.add(EditItem(start, before, after))
        }
        mEditHistory.mmPosition = sp.getInt("$prefix.position", -1)
        return mEditHistory.mmPosition != -1
    }

    private inner class EditHistory {
        var mmPosition = 0

        var mmMaxHistorySize = -1

        val mmHistory = LinkedList<EditItem>()

        fun clear() {
            mmPosition = 0
            mmHistory.clear()
        }
        fun add(item: EditItem) {
            while (mmHistory.size > mmPosition) {
                mmHistory.removeLast()
            }
            mmHistory.add(item)
            mmPosition++
            if (mmMaxHistorySize >= 0) {
                trimHistory()
            }
        }

        fun setMaxHistorySize(maxHistorySize: Int) {
            mmMaxHistorySize = maxHistorySize
            if (mmMaxHistorySize >= 0) {
                trimHistory()
            }
        }

        private fun trimHistory() {
            while (mmHistory.size > mmMaxHistorySize) {
                mmHistory.removeFirst()
                mmPosition--
            }
            if (mmPosition < 0) {
                mmPosition = 0
            }
        }

        val previous: EditItem?
            get() {
                if (mmPosition == 0) {
                    return null
                }
                mmPosition--
                return mmHistory[mmPosition]
            }

        val next: EditItem?
            get() {
                if (mmPosition >= mmHistory.size) {
                    return null
                }
                val item = mmHistory[mmPosition]
                mmPosition++
                return item
            }
    }

    private inner class EditItem(
        val mmStart: Int,
        val mmBefore: CharSequence?,
        val mmAfter: CharSequence?
    )

    private inner class EditTextChangeListener : TextWatcher {
        private var mBeforeChange: CharSequence? = null

        private var mAfterChange: CharSequence? = null
        override fun beforeTextChanged(
            s: CharSequence, start: Int, count: Int,
            after: Int
        ) {
            if (mIsUndoOrRedo) {
                return
            }
            mBeforeChange = s.subSequence(start, start + count)
        }

        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int,
            count: Int
        ) {
            if (mIsUndoOrRedo) {
                return
            }
            mAfterChange = s.subSequence(start, start + count)
            mEditHistory.add(EditItem(start, mBeforeChange, mAfterChange))
        }

        override fun afterTextChanged(s: Editable) {}
    }
}