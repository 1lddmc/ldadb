package com.LDadb.ui.diagnostics

import androidx.lifecycle.ViewModel
import com.LDadb.diagnostics.DiagnosticFormatter
import com.LDadb.diagnostics.DiagnosticLog
import com.LDadb.diagnostics.DiagnosticLogger
import kotlinx.coroutines.flow.StateFlow

class DiagnosticLogViewModel : ViewModel() {
    val logs: StateFlow<List<DiagnosticLog>> = DiagnosticLogger.logs

    fun clear() {
        DiagnosticLogger.clear()
    }

    fun copyText(logs: List<DiagnosticLog>): String {
        return DiagnosticFormatter.format(logs)
    }
}
