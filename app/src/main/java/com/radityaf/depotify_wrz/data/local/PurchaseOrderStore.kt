package com.radityaf.depotify_wrz.data.local

import com.radityaf.depotify_wrz.ui.owner.po.PurchaseOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderStore @Inject constructor() {
    private val _poList = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val poList: StateFlow<List<PurchaseOrder>> = _poList.asStateFlow()

    fun setPos(list: List<PurchaseOrder>) {
        val currentMap = _poList.value.associateBy { it.id }
        val merged = list.map { serverPo ->
            val localPo = currentMap[serverPo.id]
            if (localPo != null && localPo.status == "DITERIMA" && serverPo.status != "DITERIMA") {
                serverPo.copy(status = "DITERIMA")
            } else {
                serverPo
            }
        }
        _poList.value = merged
    }

    fun addPo(po: PurchaseOrder) {
        _poList.update { it + po }
    }

    fun clearAll() {
        _poList.value = emptyList()
    }

    fun updateStatus(id: Int, newStatus: String) {
        _poList.update { list ->
            list.map { if (it.id == id) it.copy(status = newStatus) else it }
        }
    }

    fun getById(id: Int): PurchaseOrder? = _poList.value.find { it.id == id }
}
