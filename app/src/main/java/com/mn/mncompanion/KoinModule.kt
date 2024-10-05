package com.mn.mncompanion

import com.mn.mncompanion.bluetooth.BtConnectionManager
import com.mn.mncompanion.nfc.UltralightHolder
import com.mn.mncompanion.ui.main.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val koinModule = module {
    single { BtConnectionManager(get()) }
    viewModel { MainViewModel(get(), get(), get()) }
    single { UltralightHolder() }
}