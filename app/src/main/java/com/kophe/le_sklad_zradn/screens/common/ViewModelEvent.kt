package com.kophe.le_sklad_zradn.screens.common

sealed class ViewModelEvent {

    object Auth : ViewModelEvent()
    object Main : ViewModelEvent()
    object Done : ViewModelEvent()
    object Support : ViewModelEvent()

    object Admin : ViewModelEvent()

    object EmptyResult : ViewModelEvent()

    class InfoAvailable<T>(val item: T) : ViewModelEvent()

}
