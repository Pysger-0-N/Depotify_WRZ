package com.radityaf.depotify_wrz.ui.vendor.po

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VendorPoViewModel @Inject constructor() : ViewModel() {
    // ViewModel untuk manajemen PO sisi vendor (update status konfirmasi, tolak, shipped)
}
